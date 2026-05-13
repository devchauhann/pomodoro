package com.dev.pomodoro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dev.pomodoro.R
import com.dev.pomodoro.ui.theme.PoppinsFamily

@Composable
fun SettingsDialog(
    focusMinutes: Int,
    breakMinutes: Int,
    onFocusChange: (Int) -> Unit,
    onBreakChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            DurationRow(
                label = "Focus",
                value = focusMinutes,
                minValue = 1,
                maxValue = 120,
                onValueChange = onFocusChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            DurationRow(
                label = "Break",
                value = breakMinutes,
                minValue = 1,
                maxValue = 60,
                onValueChange = onBreakChange
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Done",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val uriHandler = LocalUriHandler.current
            Text(
                text = "Check out the source code",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://github.com/devchauhann/pomodoro")
                }
            )
        }
    }
}

@Composable
private fun DurationRow(
    label: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    val hours = value / 60
    val mins = value % 60
    var hoursText by remember(hours) { mutableStateOf(hours.toString()) }
    var minsText by remember(mins) { mutableStateOf(mins.toString().padStart(2, '0')) }
    val focusManager = LocalFocusManager.current
    val maxHours = maxValue / 60

    fun commitFields() {
        val h = hoursText.toIntOrNull() ?: 0
        val m = minsText.toIntOrNull() ?: 0
        val total = (h * 60 + m).coerceIn(minValue, maxValue)
        onValueChange(total)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(
                iconRes = R.drawable.ic_minus,
                contentDescription = "Decrease $label",
                onClick = { onValueChange((value - 1).coerceAtLeast(minValue)) }
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Hours field
            TimeField(
                value = hoursText,
                onValueChange = { newText ->
                    if (newText.length <= 2 && newText.all { it.isDigit() }) {
                        hoursText = newText
                    }
                },
                onCommit = { commitFields(); focusManager.clearFocus() },
                onFocusLost = { commitFields() }
            )

            Text(
                text = "h",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, end = 8.dp)
            )

            // Minutes field
            TimeField(
                value = minsText,
                onValueChange = { newText ->
                    if (newText.length <= 2 && newText.all { it.isDigit() }) {
                        minsText = newText
                    }
                },
                onCommit = { commitFields(); focusManager.clearFocus() },
                onFocusLost = { commitFields() }
            )

            Text(
                text = "m",
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            CircleIconButton(
                iconRes = R.drawable.ic_plus,
                contentDescription = "Increase $label",
                onClick = { onValueChange((value + 1).coerceAtMost(maxValue)) }
            )
        }
    }
}

@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    onCommit: () -> Unit,
    onFocusLost: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onCommit() }),
            modifier = Modifier
                .width(40.dp)
                .onFocusChanged { state ->
                    if (!state.isFocused) onFocusLost()
                }
        )
    }
}

@Composable
private fun CircleIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
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
            modifier = Modifier.size(16.dp)
        )
    }
}
