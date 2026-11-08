package com.example.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.*

class StudyTimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_SECONDS = "EXTRA_SECONDS"
        const val EXTRA_SESSION_TYPE = "EXTRA_SESSION_TYPE"

        var remainingSecondsState = 25 * 60
        var isRunningState = false
        var currentSessionType = "جلسة تركيز"

        fun startTimer(context: Context, totalSeconds: Int, sessionType: String) {
            val intent = Intent(context, StudyTimerForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, totalSeconds)
                putExtra(EXTRA_SESSION_TYPE, sessionType)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTimer(context: Context) {
            val intent = Intent(context, StudyTimerForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, StudyTimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sec = intent.getIntExtra(EXTRA_SECONDS, 25 * 60)
                val type = intent.getStringExtra(EXTRA_SESSION_TYPE) ?: "جلسة تركيز"
                currentSessionType = type
                remainingSecondsState = sec
                isRunningState = true
                startForeground(NotificationHelper.NOTIFICATION_ID, buildNotification(sec, isRunning = true))
                runTimerLoop()
            }
            ACTION_PAUSE -> {
                isRunningState = false
                timerJob?.cancel()
                updateNotification(remainingSecondsState, isRunning = false)
            }
            ACTION_STOP -> {
                isRunningState = false
                timerJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && remainingSecondsState > 0 && isRunningState) {
                delay(1000L)
                remainingSecondsState--
                updateNotification(remainingSecondsState, isRunning = true)
            }
            if (remainingSecondsState <= 0) {
                isRunningState = false
                updateNotification(0, isRunning = false, isFinished = true)
            }
        }
    }

    private fun updateNotification(seconds: Int, isRunning: Boolean, isFinished: Boolean = false) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationHelper.NOTIFICATION_ID, buildNotification(seconds, isRunning, isFinished))
    }

    private fun buildNotification(seconds: Int, isRunning: Boolean, isFinished: Boolean = false): Notification {
        val m = seconds / 60
        val s = seconds % 60
        val timeFormatted = String.format("%02d:%02d", m, s)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isFinished) "🎉 اكتملت جلسة المذاكرة بنجاح!" else "مؤقت المذاكرة - $currentSessionType"
        val text = if (isFinished) "أحسنت! حان وقت استراحة مستحقة." else if (isRunning) "⏱️ الوقت المتبقي: $timeFormatted" else "⏸️ المؤقت متوقف مؤقتاً: $timeFormatted"

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
