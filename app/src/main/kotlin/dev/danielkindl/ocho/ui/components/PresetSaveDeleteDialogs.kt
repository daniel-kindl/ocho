package dev.danielkindl.ocho.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.danielkindl.ocho.R
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
        title = { Text(stringResource(R.string.preset_save_title)) },
        text = {
            OutlinedTextField(
                value = displayedName,
                onValueChange = { onNameChange(it.limitPresetName()) },
                label = { Text(stringResource(R.string.preset_name_label)) },
                singleLine = true,
                supportingText = {
                    Text(
                        stringResource(
                            R.string.preset_name_counter,
                            displayedName.codePointCount(0, displayedName.length),
                            MAX_PRESET_NAME_LENGTH,
                        ),
                    )
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
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
        title = { Text(stringResource(R.string.preset_delete_title)) },
        text = { Text(stringResource(R.string.preset_delete_message, presetName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
