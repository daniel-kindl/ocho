package dev.danielkindl.ocho.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielkindl.ocho.ui.components.ErrorPlate
import dev.danielkindl.ocho.ui.components.SessionProgressBar

/** Settings controls for checking, downloading, and installing GitHub releases. */
@Composable
fun DistributionUpdateSection(
    updateViewModel: UpdateViewModel = hiltViewModel(),
) {
    val state by updateViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    when (val currentState = state) {
        is UpdateUiState.Idle, is UpdateUiState.UpToDate -> ListItem(
            headlineContent = { Text("Check for updates") },
            supportingContent = if (state is UpdateUiState.UpToDate) {
                { Text("You're up to date", style = MaterialTheme.typography.bodyMedium) }
            } else null,
            trailingContent = { TextButton(onClick = updateViewModel::checkForUpdates) { Text("Check") } },
        )
        is UpdateUiState.Checking -> ListItem(
            headlineContent = { Text("Checking for updates…") },
            trailingContent = { CircularProgressIndicator() },
        )
        is UpdateUiState.Available -> ListItem(
            headlineContent = { Text("Update available: ${currentState.update.tagName}") },
            supportingContent = { Text(currentState.update.releaseNotes, style = MaterialTheme.typography.bodyMedium) },
            trailingContent = { Button(onClick = updateViewModel::startDownload) { Text("Download") } },
        )
        is UpdateUiState.Downloading -> ListItem(
            headlineContent = { Text("Downloading update… ${currentState.progressPercent}%") },
            supportingContent = { SessionProgressBar(progress = currentState.progressPercent / 100f) },
        )
        is UpdateUiState.ReadyToInstall -> ListItem(
            headlineContent = { Text("Update ${currentState.update.tagName} ready to install") },
            trailingContent = {
                Button(onClick = {
                    if (updateViewModel.canInstallPackages()) updateViewModel.startInstall()
                    else context.startActivity(updateViewModel.unknownSourcesSettingsIntent())
                }) { Text("Install") }
            },
        )
        is UpdateUiState.Error -> ErrorPlate(
            message = currentState.message,
            actionLabel = "Try again",
            onAction = updateViewModel::checkForUpdates,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
