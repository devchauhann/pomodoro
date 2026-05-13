package com.dev.pomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.pomodoro.ui.PomodoroScreen
import com.dev.pomodoro.ui.SettingsDialog
import com.dev.pomodoro.ui.theme.LocalIsDarkTheme
import com.dev.pomodoro.ui.theme.PomodoroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        requestNotificationPermission()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val viewModel: PomodoroViewModel = viewModel()
            var showSettings by remember { mutableStateOf(false) }

            CompositionLocalProvider(LocalIsDarkTheme provides viewModel.isDarkTheme) {
                PomodoroTheme(darkTheme = viewModel.isDarkTheme) {
                    PomodoroScreen(
                        viewModel = viewModel,
                        onSettingsClick = { showSettings = true }
                    )

                    if (showSettings) {
                        SettingsDialog(
                            focusMinutes = viewModel.focusDurationMin,
                            breakMinutes = viewModel.breakDurationMin,
                            onFocusChange = { viewModel.updateFocusDuration(it) },
                            onBreakChange = { viewModel.updateBreakDuration(it) },
                            onDismiss = { showSettings = false }
                        )
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}