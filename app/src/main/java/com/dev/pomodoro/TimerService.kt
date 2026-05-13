package com.dev.pomodoro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val ACTION_START = "com.dev.pomodoro.action.START"
        const val ACTION_STOP = "com.dev.pomodoro.action.STOP"
        const val EXTRA_END_TIME = "end_time"
        const val EXTRA_SESSION_TYPE = "session_type"

        const val CHANNEL_TIMER = "timer_running"
        const val CHANNEL_COMPLETE = "timer_complete"
        const val NOTIFICATION_ID = 1
        const val COMPLETE_NOTIFICATION_ID = 2

        fun start(context: Context, endTimeMs: Long, sessionType: SessionType) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_END_TIME, endTimeMs)
                putExtra(EXTRA_SESSION_TYPE, sessionType.name)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimerService::class.java))
        }
    }

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val endTime = intent.getLongExtra(EXTRA_END_TIME, 0L)
                val sessionType = intent.getStringExtra(EXTRA_SESSION_TYPE) ?: "FOCUS"
                startTimer(endTime, sessionType)
            }
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            "Timer Running",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the running Pomodoro timer"
            setShowBadge(false)
        }

        val completeChannel = NotificationChannel(
            CHANNEL_COMPLETE,
            "Timer Complete",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when a Pomodoro session ends"
        }

        nm.createNotificationChannels(listOf(timerChannel, completeChannel))
    }

    private fun startTimer(endTimeMs: Long, sessionType: String) {
        timerJob?.cancel()

        val remaining = endTimeMs - System.currentTimeMillis()

        // Must call startForeground immediately — required by startForegroundService contract
        startForeground(NOTIFICATION_ID, buildTimerNotification(remaining.coerceAtLeast(0), sessionType))

        if (remaining <= 0) {
            onTimerComplete(sessionType)
            return
        }

        timerJob = scope.launch {
            while (true) {
                val rem = endTimeMs - System.currentTimeMillis()
                if (rem <= 0) {
                    onTimerComplete(sessionType)
                    break
                }
                updateNotification(rem, sessionType)
                delay(1000L)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerComplete(sessionType: String) {
        timerJob?.cancel()

        vibrate()

        val nm = getSystemService(NotificationManager::class.java)

        val title = if (sessionType == "FOCUS") "Focus Complete!" else "Break Over!"
        val text = if (sessionType == "FOCUS") "Time for a break." else "Ready to focus?"

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_COMPLETE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(COMPLETE_NOTIFICATION_ID, notification)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildTimerNotification(remainingMs: Long, sessionType: String): Notification {
        val totalSec = (remainingMs / 1000).coerceAtLeast(0)
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60

        val timeText = if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)

        val label = if (sessionType == "FOCUS") "Focus" else "Break"

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$label — $timeText")
            .setContentText("Pomodoro timer running")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(remainingMs: Long, sessionType: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildTimerNotification(remainingMs, sessionType))
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 400), -1)
        )
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }
}
