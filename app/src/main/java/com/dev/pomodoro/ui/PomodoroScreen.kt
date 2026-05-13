package com.dev.pomodoro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.pomodoro.PomodoroViewModel
import com.dev.pomodoro.R
import com.dev.pomodoro.SessionType
import com.dev.pomodoro.TimerState
import com.dev.pomodoro.ui.theme.PoppinsFamily

// Normal view sizes
private val NORMAL_DIGIT_FONT = 120.sp
private val NORMAL_COLON_FONT = 100.sp
private val NORMAL_DIGIT_WIDTH = 76.dp

// Focus mode (fullscreen) sizes
private val FOCUS_DIGIT_FONT = 160.sp
private val FOCUS_COLON_FONT = 130.sp
private val FOCUS_DIGIT_WIDTH = 100.dp

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    onSettingsClick: () -> Unit
) {
    val accentColor by animateColorAsState(
        targetValue = if (viewModel.sessionType == SessionType.FOCUS) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        },
        animationSpec = tween(400),
        label = "accent"
    )

    // Focus mode: auto-enter when running, user can expand back
    var isFocusMode by remember { mutableStateOf(false) }

    // Auto-enter focus mode when timer starts running
    LaunchedEffect(viewModel.timerState) {
        if (viewModel.timerState == TimerState.RUNNING) {
            isFocusMode = true
        } else if (viewModel.timerState == TimerState.IDLE) {
            isFocusMode = false
        }
    }

    val showChrome = !isFocusMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Top bar: session label + theme toggle + settings
        AnimatedVisibility(
            visible = showChrome,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 32.dp, end = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = viewModel.sessionType,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { -it / 2 }) togetherWith
                                (fadeOut(tween(200)) + slideOutVertically { it / 2 })
                    },
                    label = "sessionLabel"
                ) { session ->
                    Text(
                        text = if (session == SessionType.FOCUS) "FOCUS" else "BREAK",
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor,
                        letterSpacing = 4.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewModel.skip() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { viewModel.toggleTheme() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_theme_toggle),
                        contentDescription = "Toggle theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(3.dp))

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Center: timer + controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer digits — GPU-scaled for butter-smooth transitions
            val timerScale by animateFloatAsState(
                targetValue = if (isFocusMode) 1.35f else 1f,
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                label = "timerScale"
            )
            val digitFont = NORMAL_DIGIT_FONT
            val colonFont = NORMAL_COLON_FONT
            val digitWidth = NORMAL_DIGIT_WIDTH
            val showHours = viewModel.hours > 0
            val textColor = MaterialTheme.colorScheme.onBackground

            val colonAlpha by animateFloatAsState(
                targetValue = if (viewModel.timerState == TimerState.RUNNING) {
                    if ((viewModel.seconds % 2) == 0) 1f else 0.3f
                } else 1f,
                animationSpec = tween(400),
                label = "colon"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    scaleX = timerScale
                    scaleY = timerScale
                }
            ) {
                // Hours (only when > 0)
                AnimatedVisibility(
                    visible = showHours,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedDigit(value = viewModel.hours / 10, color = textColor, fontSize = digitFont, width = digitWidth)
                        AnimatedDigit(value = viewModel.hours % 10, color = textColor, fontSize = digitFont, width = digitWidth)
                        Text(
                            text = ":",
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = colonFont,
                            color = textColor.copy(alpha = colonAlpha),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // Minutes
                AnimatedDigit(value = viewModel.minutes / 10, color = textColor, fontSize = digitFont, width = digitWidth)
                AnimatedDigit(value = viewModel.minutes % 10, color = textColor, fontSize = digitFont, width = digitWidth)

                Text(
                    text = ":",
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = colonFont,
                    color = textColor.copy(alpha = colonAlpha),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Seconds
                AnimatedDigit(value = viewModel.seconds / 10, color = textColor, fontSize = digitFont, width = digitWidth)
                AnimatedDigit(value = viewModel.seconds % 10, color = textColor, fontSize = digitFont, width = digitWidth)
            }

            // Controls — hidden in focus mode
            AnimatedVisibility(
                visible = showChrome,
                enter = fadeIn(tween(300)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(200)) + slideOutVertically { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset
                        AnimatedVisibility(
                            visible = viewModel.timerState != TimerState.IDLE,
                            enter = fadeIn(tween(300)) + slideInVertically { it },
                            exit = fadeOut(tween(200)) + slideOutVertically { it }
                        ) {
                            ControlButton(
                                iconRes = R.drawable.ic_reset,
                                contentDescription = "Reset",
                                onClick = { viewModel.reset() },
                                small = true
                            )
                        }

                        // Play / Pause
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (viewModel.timerState == TimerState.RUNNING) {
                                        viewModel.pause()
                                    } else {
                                        viewModel.start()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = viewModel.timerState == TimerState.RUNNING,
                                transitionSpec = {
                                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                                },
                                label = "playPause"
                            ) { isRunning ->
                                Icon(
                                    painter = painterResource(
                                        if (isRunning) R.drawable.ic_pause else R.drawable.ic_play
                                    ),
                                    contentDescription = if (isRunning) "Pause" else "Start",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Skip
                        AnimatedVisibility(
                            visible = viewModel.timerState != TimerState.IDLE,
                            enter = fadeIn(tween(300)) + slideInVertically { it },
                            exit = fadeOut(tween(200)) + slideOutVertically { it }
                        ) {
                            ControlButton(
                                iconRes = R.drawable.ic_skip,
                                contentDescription = "Skip",
                                onClick = { viewModel.skip() },
                                small = true
                            )
                        }
                    }
                }
            }
        }

        // Expand button — only visible in focus mode (top-right)
        AnimatedVisibility(
            visible = isFocusMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 24.dp),
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200))
        ) {
            IconButton(onClick = { isFocusMode = false }) {
                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = "Show controls",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedDigit(
    value: Int,
    color: androidx.compose.ui.graphics.Color,
    fontSize: androidx.compose.ui.unit.TextUnit = NORMAL_DIGIT_FONT,
    width: androidx.compose.ui.unit.Dp = NORMAL_DIGIT_WIDTH
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                val enterDuration = 350
                val exitDuration = 250
                (slideInVertically(tween(enterDuration)) { height -> -height / 3 }
                        + fadeIn(tween(enterDuration)))
                    .togetherWith(
                        slideOutVertically(tween(exitDuration)) { height -> height / 3 }
                                + fadeOut(tween(exitDuration))
                    )
            },
            label = "digit"
        ) { digit ->
            Text(
                text = digit.toString(),
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(width)
                    .graphicsLayer {
                        renderEffect = BlurEffect(1f, 1f)
                    }
            )
        }
    }
}

@Composable
private fun ControlButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    small: Boolean = false
) {
    val size = if (small) 48.dp else 56.dp
    val iconSize = if (small) 20.dp else 24.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(iconSize)
        )
    }
}
