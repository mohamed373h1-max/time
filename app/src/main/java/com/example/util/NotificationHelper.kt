package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "study_timer_channel"
    private const val CHANNEL_NAME = "مؤقت المذاكرة الدراسي"
    const val NOTIFICATION_ID = 1001

    const val CHANNEL_QURAN_AUDIO_ID = "quran_audio_channel"
    private const val CHANNEL_QURAN_NAME = "تلاوة القرآن الكريم"
    const val QURAN_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val timerChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات جلسات المذاكرة والمؤقت الدراسي المستمر"
                setShowBadge(true)
            }
            manager.createNotificationChannel(timerChannel)

            val quranChannel = NotificationChannel(
                CHANNEL_QURAN_AUDIO_ID,
                CHANNEL_QURAN_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "مشغل تلاوة القرآن الكريم بأصوات كبار المشايخ"
                setShowBadge(false)
            }
            manager.createNotificationChannel(quranChannel)
        }
    }

    fun buildQuranAudioNotification(
        context: Context,
        surahName: String,
        reciterName: String,
        isPlaying: Boolean
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_QURAN_AUDIO_ID)
            .setContentTitle("سورة $surahName")
            .setContentText(reciterName)
            .setSubText(if (isPlaying) "جاري الاستماع الآن" else "متوقف مؤقتاً")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showTimerNotification(
        context: Context,
        timeFormatted: String,
        sessionType: String,
        isRunning: Boolean
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isRunning) "⏱️ جاري المذاكرة: $timeFormatted" else "⏸️ المؤقت متوقف مؤقتاً: $timeFormatted"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("مؤقت المذاكرة - $sessionType")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    fun dismissTimerNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}
