package dev.danielkindl.ocho.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.danielkindl.ocho.R

/**
 * Guards against losing a session in progress, whether by the STOP button or a
 * stray back gesture — easy to trigger with sweaty hands mid-workout.
 */
@Composable
fun ExitConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_workout_title)) },
        text = { Text(stringResource(R.string.exit_workout_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_stop), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
