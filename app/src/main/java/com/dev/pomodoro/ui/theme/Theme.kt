package com.dev.pomodoro.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = compositionLocalOf { true }

private val DarkColorScheme = darkColorScheme(
    primary = AccentFocusDark,
    secondary = AccentBreakDark,
    tertiary = AccentNeutral,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    surfaceVariant = Color(0xFF2E2E2E),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = AccentFocus,
    secondary = AccentBreak,
    tertiary = AccentNeutral,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFCCCCCC)
)

@Composable
fun PomodoroTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val targetScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val background by animateColorAsState(targetScheme.background, tween(500), label = "bg")
    val surface by animateColorAsState(targetScheme.surface, tween(500), label = "surface")
    val onBackground by animateColorAsState(targetScheme.onBackground, tween(500), label = "onBg")
    val onSurface by animateColorAsState(targetScheme.onSurface, tween(500), label = "onSurf")
    val primary by animateColorAsState(targetScheme.primary, tween(500), label = "primary")
    val secondary by animateColorAsState(targetScheme.secondary, tween(500), label = "secondary")
    val surfaceVariant by animateColorAsState(targetScheme.surfaceVariant, tween(500), label = "surfVar")
    val onSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, tween(500), label = "onSurfVar")
    val outline by animateColorAsState(targetScheme.outline, tween(500), label = "outline")

    val animatedScheme = if (darkTheme) {
        DarkColorScheme.copy(
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface,
            primary = primary,
            secondary = secondary,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline
        )
    } else {
        LightColorScheme.copy(
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface,
            primary = primary,
            secondary = secondary,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline
        )
    }

    MaterialTheme(
        colorScheme = animatedScheme,
        typography = Typography,
        content = content
    )
}