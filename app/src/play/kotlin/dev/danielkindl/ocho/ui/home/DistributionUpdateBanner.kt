package dev.danielkindl.ocho.ui.home

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielkindl.ocho.R
import dev.danielkindl.ocho.data.update.PlayUpdateState
import dev.danielkindl.ocho.ui.settings.PlayUpdateViewModel

/** Non-blocking Home action for a Play update found during the launch check. */
@Composable
fun DistributionUpdateBanner(
    viewModel: PlayUpdateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) viewModel.checkForUpdates()
    }

    when (val currentState = state) {
        PlayUpdateState.Available -> Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.update_banner_available)) },
                supportingContent = { Text(stringResource(R.string.update_banner_downloading_info)) },
                trailingContent = {
                    Button(onClick = { viewModel.startUpdate(launcher) }) {
                        Text(stringResource(R.string.update_action))
                    }
                },
            )
        }

        is PlayUpdateState.Downloading -> Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.update_downloading_play, currentState.progressPercent))
                },
            )
        }

        PlayUpdateState.Downloaded -> Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.update_ready)) },
                trailingContent = {
                    Button(onClick = viewModel::completeUpdate) {
                        Text(stringResource(R.string.update_restart))
                    }
                },
            )
        }

        else -> Unit
    }
}
