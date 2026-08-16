package dev.danielkindl.ocho.ui.settings

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import dev.danielkindl.ocho.data.update.PlayUpdateCoordinator
import dev.danielkindl.ocho.data.update.PlayUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Bridges Play's update coordinator to the Settings and Home composables. */
@HiltViewModel
class PlayUpdateViewModel @Inject constructor(
    private val coordinator: PlayUpdateCoordinator,
) : ViewModel() {
    /** Current Play update state. */
    val state: StateFlow<PlayUpdateState> = coordinator.state

    /** Rechecks Play on explicit user request. */
    fun checkForUpdates() = coordinator.checkForUpdates()

    /** Starts the flexible update flow after the user taps Update. */
    fun startUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) =
        coordinator.startFlexibleUpdate(launcher)

    /** Completes a flexible update after the user confirms the restart. */
    fun completeUpdate() = coordinator.completeUpdate()
}
