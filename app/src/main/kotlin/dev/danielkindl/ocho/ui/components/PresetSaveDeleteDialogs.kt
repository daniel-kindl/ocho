package dev.danielkindl.ocho.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.danielkindl.ocho.domain.model.MAX_PRESET_NAME_LENGTH
import dev.danielkindl.ocho.domain.model.limitPresetName

/**
 * Names and saves the current configuration. Shared by both setup screens.
 *
 * @param name the editable preset name, pre-filled with a default derived from the
 *   durations so saving is a single tap for anyone who does not care about naming.
 */
@Composable
fun SavePresetDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val displayedName = name.limitPresetName()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Preset") },
        text = {
            OutlinedTextField(
                value = displayedName,
                onValueChange = { onNameChange(it.limitPresetName()) },
                label = { Text("Preset name") },
                singleLine = true,
                supportingText = {
                    Text(
                        "${displayedName.codePointCount(0, displayedName.length)} " +
                            "/ $MAX_PRESET_NAME_LENGTH",
                    )
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/** Confirms deleting a preset, naming it so the wrong chip is not removed by accident. */
@Composable
fun DeletePresetDialog(
    presetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Preset") },
        text = { Text("Delete \"$presetName\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
