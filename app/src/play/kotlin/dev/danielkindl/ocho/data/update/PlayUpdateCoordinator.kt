package dev.danielkindl.ocho.data.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.danielkindl.ocho.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** State exposed by the Play-managed update flow. */
sealed interface PlayUpdateState {
    /** No Play update check has completed yet. */
    data object Idle : PlayUpdateState

    /** Play is checking whether a newer listing version is available. */
    data object Checking : PlayUpdateState

    /** The installed Play version is current. */
    data object UpToDate : PlayUpdateState

    /** A flexible Play update can be started by the user. */
    data object Available : PlayUpdateState

    /** Play is downloading the update in the background. */
    data class Downloading(
        /** Download completion percentage from 0 through 100. */
        val progressPercent: Int,
    ) : PlayUpdateState

    /** Play has downloaded the update and is waiting for a restart. */
    data object Downloaded : PlayUpdateState

    /** The Play update check or flow failed without blocking the app. */
    data class Error(
        /** Human-readable failure detail for diagnostics and UI. */
        val message: String,
    ) : PlayUpdateState
}

/**
 * Owns the Play Store update client for the Play flavor.
 *
 * Play remains responsible for retrieving and installing the app. This coordinator
 * only checks availability, starts the user-approved flexible flow, and reports its
 * progress to the UI.
 */
@Singleton
class PlayUpdateCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateClient: PlayUpdateClient,
) {
    private val _state = MutableStateFlow<PlayUpdateState>(PlayUpdateState.Idle)
    private var availableInfo: AppUpdateInfo? = null

    /** Current Play update state. */
    val state: StateFlow<PlayUpdateState> = _state.asStateFlow()

    @Suppress("DEPRECATION")
    private val installListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val total = installState.totalBytesToDownload()
                val downloaded = installState.bytesDownloaded()
                val progress = if (total > 0) {
                    (downloaded * 100 / total).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                _state.value = PlayUpdateState.Downloading(progress)
            }

            InstallStatus.DOWNLOADED -> _state.value = PlayUpdateState.Downloaded
            InstallStatus.INSTALLED -> _state.value = PlayUpdateState.UpToDate
            InstallStatus.CANCELED -> _state.value = PlayUpdateState.Idle
            InstallStatus.FAILED -> _state.value = PlayUpdateState.Error(
                context.getString(R.string.update_play_failed),
            )
            InstallStatus.PENDING,
            InstallStatus.INSTALLING,
            InstallStatus.REQUIRES_UI_INTENT,
            InstallStatus.UNKNOWN -> Unit
        }
    }

    init {
        updateClient.registerListener(installListener)
    }

    /** Checks Play once without showing UI or interrupting the current screen. */
    fun checkForUpdates() {
        _state.value = PlayUpdateState.Checking
        updateClient.appUpdateInfo
            .addOnSuccessListener { info ->
                availableInfo = info.takeIf {
                    it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                }
                _state.value = when {
                    availableInfo != null -> PlayUpdateState.Available
                    info.installStatus() == InstallStatus.DOWNLOADED -> PlayUpdateState.Downloaded
                    else -> PlayUpdateState.UpToDate
                }
            }
            .addOnFailureListener { error ->
                _state.value = PlayUpdateState.Error(
                    error.message ?: context.getString(R.string.update_play_check_failed),
                )
            }
    }

    /** Starts the user-approved flexible flow through the supplied Activity Result launcher. */
    fun startFlexibleUpdate(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        val info = availableInfo ?: return
        updateClient.startUpdateFlowForResult(
            info,
            launcher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
    }

    /** Asks Play to install a downloaded flexible update and restart the app. */
    fun completeUpdate() {
        updateClient.completeUpdate()
    }
}
