package com.dev.pomodoro

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationCompat
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TimerState {
    IDLE, RUNNING, PAUSED
}

enum class SessionType {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    var focusDurationMin by mutableIntStateOf(25)
        private set
    var breakDurationMin by mutableIntStateOf(5)
        private set

    var timerState by mutableStateOf(TimerState.IDLE)
        private set
    var sessionType by mutableStateOf(SessionType.FOCUS)
        private set
    var timeRemainingMs by mutableLongStateOf(25 * 60 * 1000L)
        private set
    var totalDurationMs by mutableLongStateOf(25 * 60 * 1000L)
        private set

    var isDarkTheme by mutableStateOf(true)
        private set

    private var timerJob: Job? = null

    val hours: Int
        get() = ((timeRemainingMs / 1000) / 3600).toInt()
    val minutes: Int
        get() = (((timeRemainingMs / 1000) % 3600) / 60).toInt()
    val seconds: Int
        get() = ((timeRemainingMs / 1000) % 60).toInt()
    val progress: Float
        get() = if (totalDurationMs > 0) timeRemainingMs.toFloat() / totalDurationMs.toFloat() else 1f

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }

    fun start() {
        if (timerState == TimerState.IDLE) {
            resetTimerToCurrentSession()
        }
        timerState = TimerState.RUNNING
        timerJob?.cancel()
        val startTime = System.currentTimeMillis()
        val startRemaining = timeRemainingMs
        val endTimeMs = startTime + startRemaining

        TimerService.start(getApplication(), endTimeMs, sessionType)

        timerJob = viewModelScope.launch {
            while (timeRemainingMs > 0) {
                delay(50L)
                val elapsed = System.currentTimeMillis() - startTime
                timeRemainingMs = (startRemaining - elapsed).coerceAtLeast(0L)
            }
            onTimerFinished()
        }
    }

    fun pause() {
        timerState = TimerState.PAUSED
        timerJob?.cancel()
        TimerService.stop(getApplication())
    }

    fun reset() {
        timerJob?.cancel()
        timerState = TimerState.IDLE
        sessionType = SessionType.FOCUS
        resetTimerToCurrentSession()
        TimerService.stop(getApplication())
    }

    fun skip() {
        timerJob?.cancel()
        timerState = TimerState.IDLE
        sessionType = if (sessionType == SessionType.FOCUS) SessionType.BREAK else SessionType.FOCUS
        resetTimerToCurrentSession()
        TimerService.stop(getApplication())
    }

    fun updateFocusDuration(minutes: Int) {
        focusDurationMin = minutes.coerceIn(1, 120)
        if (timerState == TimerState.IDLE) {
            resetTimerToCurrentSession()
        }
    }

    fun updateBreakDuration(minutes: Int) {
        breakDurationMin = minutes.coerceIn(1, 60)
        if (timerState == TimerState.IDLE) {
            resetTimerToCurrentSession()
        }
    }

    private fun resetTimerToCurrentSession() {
        val durationMin = if (sessionType == SessionType.FOCUS) focusDurationMin else breakDurationMin
        totalDurationMs = durationMin.toLong() * 60L * 1000L
        timeRemainingMs = totalDurationMs
    }

    private fun onTimerFinished() {
        val finishedSession = sessionType
        TimerService.stop(getApplication())
        vibrate()
        showCompletionNotification(finishedSession)
        timerState = TimerState.IDLE
        sessionType = if (sessionType == SessionType.FOCUS) SessionType.BREAK else SessionType.FOCUS
        resetTimerToCurrentSession()
    }

    private fun vibrate() {
        val app = getApplication<Application>()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            app.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 400), -1)
        )
    }

    private fun showCompletionNotification(finishedSession: SessionType) {
        val app = getApplication<Application>()
        val nm = app.getSystemService(NotificationManager::class.java)

        val title = if (finishedSession == SessionType.FOCUS) "Focus Complete!" else "Break Over!"
        val text = if (finishedSession == SessionType.FOCUS) "Time for a break." else "Ready to focus?"

        val openIntent = Intent(app, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            app, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(app, TimerService.CHANNEL_COMPLETE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(TimerService.COMPLETE_NOTIFICATION_ID, notification)
    }
}
