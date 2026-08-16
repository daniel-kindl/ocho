package dev.danielkindl.ocho.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * MM:SS duration picker using a drum-roll scroll wheel.
 * Minutes: 0–99, Seconds: 0–59. Both wrap around.
 */
@Composable
fun DurationPicker(
    label: String,
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = if (compact) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(if (compact) 4.dp else 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WheelPicker(
                    count = 100,
                    selected = minutes,
                    onSelect = onMinutesChange,
                    itemHeight = if (compact) 32.dp else 44.dp,
                    pickerWidth = if (compact) 54.dp else 80.dp,
                    contentDescription = "$label minutes",
                )
                Spacer(Modifier.height(if (compact) 2.dp else 4.dp))
                Text(
                    text = "min",
                    style = if (compact) MaterialTheme.typography.labelMedium
                    else MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = ":",
                style = if (compact) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = if (compact) 4.dp else 8.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WheelPicker(
                    count = 60,
                    selected = seconds,
                    onSelect = onSecondsChange,
                    itemHeight = if (compact) 32.dp else 44.dp,
                    pickerWidth = if (compact) 54.dp else 80.dp,
                    contentDescription = "$label seconds",
                )
                Spacer(Modifier.height(if (compact) 2.dp else 4.dp))
                Text(
                    text = "sec",
                    style = if (compact) MaterialTheme.typography.labelMedium
                    else MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
