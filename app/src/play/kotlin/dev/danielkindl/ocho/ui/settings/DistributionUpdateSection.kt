package dev.danielkindl.ocho.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielkindl.ocho.data.update.PlayUpdateState

/** Settings controls for Play-managed flexible updates. */
@Composable
fun DistributionUpdateSection(
    viewModel: PlayUpdateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) viewModel.checkForUpdates()
    }

    when (val currentState = state) {
        PlayUpdateState.Idle,
        PlayUpdateState.UpToDate,
        is PlayUpdateState.Error,
        -> ListItem(
            headlineContent = { Text("Check for updates") },
            supportingContent = {
                Text(
                    if (currentState is PlayUpdateState.UpToDate) {
                        "You're up to date"
                    } else {
                        "Google Play"
                    }
                )
            },
            trailingContent = {
                androidx.compose.material3.TextButton(onClick = viewModel::checkForUpdates) {
                    Text("Check")
                }
            },
        )

        PlayUpdateState.Checking -> ListItem(
            headlineContent = { Text("Checking Play Store…") },
        )

        PlayUpdateState.Available -> ListItem(
            headlineContent = { Text("Update available") },
            trailingContent = {
                Button(onClick = { viewModel.startUpdate(launcher) }) { Text("Update") }
            },
        )

        is PlayUpdateState.Downloading -> ListItem(
            headlineContent = {
                Text("Downloading update… ${currentState.progressPercent}%")
            },
        )

        PlayUpdateState.Downloaded -> ListItem(
            headlineContent = { Text("Update ready") },
            trailingContent = {
                Button(onClick = viewModel::completeUpdate) { Text("Restart") }
            },
        )
    }
}
