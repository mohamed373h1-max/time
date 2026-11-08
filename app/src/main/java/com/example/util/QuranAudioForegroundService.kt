package com.example.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuranAudioState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val surahNumber: Int = 1,
    val surahName: String = "",
    val reciterName: String = "",
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val errorMessage: String? = null
)

class QuranAudioForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String = ""

    companion object {
        const val ACTION_PLAY = "com.example.action.QURAN_PLAY"
        const val ACTION_PAUSE = "com.example.action.QURAN_PAUSE"
        const val ACTION_RESUME = "com.example.action.QURAN_RESUME"
        const val ACTION_STOP = "com.example.action.QURAN_STOP"

        const val EXTRA_URL = "extra_audio_url"
        const val EXTRA_SURAH_NUMBER = "extra_surah_number"
        const val EXTRA_SURAH_NAME = "extra_surah_name"
        const val EXTRA_RECITER_NAME = "extra_reciter_name"

        private val _audioState = MutableStateFlow(QuranAudioState())
        val audioState: StateFlow<QuranAudioState> = _audioState.asStateFlow()

        fun play(
            context: Context,
            audioUrl: String,
            surahNumber: Int,
            surahName: String,
            reciterName: String
        ) {
            val intent = Intent(context, QuranAudioForegroundService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_URL, audioUrl)
                putExtra(EXTRA_SURAH_NUMBER, surahNumber)
                putExtra(EXTRA_SURAH_NAME, surahName)
                putExtra(EXTRA_RECITER_NAME, reciterName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, QuranAudioForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, QuranAudioForegroundService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, QuranAudioForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: ""
                val surahNumber = intent.getIntExtra(EXTRA_SURAH_NUMBER, 1)
                val surahName = intent.getStringExtra(EXTRA_SURAH_NAME) ?: ""
                val reciterName = intent.getStringExtra(EXTRA_RECITER_NAME) ?: ""

                startPlaying(url, surahNumber, surahName, reciterName)
            }
            ACTION_PAUSE -> pauseAudio()
            ACTION_RESUME -> resumeAudio()
            ACTION_STOP -> stopAudio()
        }
        return START_NOT_STICKY
    }

    private fun startPlaying(
        url: String,
        surahNumber: Int,
        surahName: String,
        reciterName: String
    ) {
        currentUrl = url
        _audioState.value = QuranAudioState(
            isPlaying = false,
            isBuffering = true,
            surahNumber = surahNumber,
            surahName = surahName,
            reciterName = reciterName
        )

        // Show foreground notification
        val notification = NotificationHelper.buildQuranAudioNotification(
            context = this,
            surahName = surahName,
            reciterName = reciterName,
            isPlaying = true
        )
        startForeground(NotificationHelper.QURAN_NOTIFICATION_ID, notification)

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    _audioState.value = _audioState.value.copy(
                        isPlaying = true,
                        isBuffering = false,
                        duration = mp.duration,
                        currentPosition = mp.currentPosition
                    )
                }
                setOnCompletionListener {
                    _audioState.value = _audioState.value.copy(isPlaying = false, isBuffering = false)
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
                setOnErrorListener { _, _, _ ->
                    _audioState.value = _audioState.value.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "تعذر تشغيل التلاوة. يرجى التحقق من الاتصال بالإنترنت."
                    )
                    stopForeground(STOP_FOREGROUND_DETACH)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _audioState.value = _audioState.value.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = e.localizedMessage
            )
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _audioState.value = _audioState.value.copy(isPlaying = false)
                val notification = NotificationHelper.buildQuranAudioNotification(
                    context = this,
                    surahName = _audioState.value.surahName,
                    reciterName = _audioState.value.reciterName,
                    isPlaying = false
                )
                startForeground(NotificationHelper.QURAN_NOTIFICATION_ID, notification)
            }
        }
    }

    private fun resumeAudio() {
        mediaPlayer?.let {
            it.start()
            _audioState.value = _audioState.value.copy(isPlaying = true)
            val notification = NotificationHelper.buildQuranAudioNotification(
                context = this,
                surahName = _audioState.value.surahName,
                reciterName = _audioState.value.reciterName,
                isPlaying = true
            )
            startForeground(NotificationHelper.QURAN_NOTIFICATION_ID, notification)
        }
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _audioState.value = QuranAudioState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
