package dev.danielkindl.ocho.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielkindl.ocho.R
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
            headlineContent = { Text(stringResource(R.string.update_check_for_updates)) },
            supportingContent = {
                Text(
                    if (currentState is PlayUpdateState.UpToDate) {
                        stringResource(R.string.update_up_to_date)
                    } else {
                        stringResource(R.string.update_play_store)
                    }
                )
            },
            trailingContent = {
                androidx.compose.material3.TextButton(onClick = viewModel::checkForUpdates) {
                    Text(stringResource(R.string.update_check))
                }
            },
        )

        PlayUpdateState.Checking -> ListItem(
            headlineContent = { Text(stringResource(R.string.update_checking_play_store)) },
        )

        PlayUpdateState.Available -> ListItem(
            headlineContent = { Text(stringResource(R.string.update_available)) },
            trailingContent = {
                Button(onClick = { viewModel.startUpdate(launcher) }) {
                    Text(stringResource(R.string.update_action))
                }
            },
        )

        is PlayUpdateState.Downloading -> ListItem(
            headlineContent = {
                Text(stringResource(R.string.update_downloading_play, currentState.progressPercent))
            },
        )

        PlayUpdateState.Downloaded -> ListItem(
            headlineContent = { Text(stringResource(R.string.update_ready)) },
            trailingContent = {
                Button(onClick = viewModel::completeUpdate) {
                    Text(stringResource(R.string.update_restart))
                }
            },
        )
    }
}
