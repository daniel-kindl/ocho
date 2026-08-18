package dev.danielkindl.ocho.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.danielkindl.ocho.R

/**
 * Reusable preset section: header with a save button and a readable list of presets.
 *
 * Generic so it works with any preset type — callers supply [getLabel] and
 * [getSummary] to extract display information without coupling to a specific model.
 *
 * @param canDelete whether a given preset offers its delete control. Defaults to all of
 *   them; a build's own presets say no, since deleting one would only bring it back on
 *   the next launch and an X that does nothing reads as a bug.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PresetsSection(
    presets: List<T>,
    getLabel: (T) -> String,
    getSummary: @Composable (T) -> String,
    onPresetClick: (T) -> Unit,
    onDeleteClick: (T) -> Unit,
    onSavePreset: () -> Unit,
    saveEnabled: Boolean,
    modifier: Modifier = Modifier,
    canDelete: (T) -> Boolean = { true },
    showEmptyMessage: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.presets_title), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onSavePreset, enabled = saveEnabled) {
                Icon(
                    painterResource(R.drawable.ic_bookmark_plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.action_save))
            }
        }
        if (presets.isEmpty() && showEmptyMessage) {
            PresetsEmptyState()
        } else {
            Column {
                presets.forEachIndexed { index, preset ->
                    PresetRow(
                        label = getLabel(preset),
                        summary = getSummary(preset),
                        onClick = { onPresetClick(preset) },
                        onDelete = if (canDelete(preset)) {
                            { onDeleteClick(preset) }
                        } else {
                            null
                        },
                    )
                    if (index < presets.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetsEmptyState() {
    Text(
        stringResource(R.string.presets_empty),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PresetRow(
    label: String,
    summary: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val loadDescription = stringResource(R.string.preset_load_accessibility, label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = loadDescription
            }
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onDelete != null) {
            DeletePresetIcon(label = label, onClick = onDelete)
        }
    }
}

@Composable
private fun DeletePresetIcon(label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(
            painterResource(R.drawable.ic_x),
            contentDescription = stringResource(R.string.preset_delete_accessibility, label),
            modifier = Modifier.size(14.dp),
        )
    }
}
