package dev.danielkindl.ocho.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.danielkindl.ocho.data.update.ApkInstaller
import dev.danielkindl.ocho.data.update.DownloadStatus
import dev.danielkindl.ocho.data.update.UpdateCheckCache
import dev.danielkindl.ocho.data.update.UpdateDownloader
import dev.danielkindl.ocho.domain.model.AppUpdate
import dev.danielkindl.ocho.domain.model.SemVer
import dev.danielkindl.ocho.domain.model.UpdateConfig
import dev.danielkindl.ocho.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/** States exposed by the GitHub update flow. */
sealed interface UpdateUiState {
    /** No check has run, or the previous result was cleared. */
    data object Idle : UpdateUiState
    /** A release lookup is in progress. */
    data object Checking : UpdateUiState
    /** The installed version is current for its channel. */
    data object UpToDate : UpdateUiState
    /** A newer release is available to download. */
    data class Available(
        /** Release selected for download. */
        val update: AppUpdate,
    ) : UpdateUiState
    /** Android is downloading the selected release. */
    data class Downloading(
        /** Release being downloaded. */
        val update: AppUpdate,
        /** Current download percentage from Android's download manager. */
        val progressPercent: Int,
    ) : UpdateUiState
    /** The APK is ready for the user to install. */
    data class ReadyToInstall(
        /** Release represented by [apkFile]. */
        val update: AppUpdate,
        /** Downloaded APK handed to the installer. */
        val apkFile: File,
    ) : UpdateUiState
    /** The update flow failed with a user-facing message. */
    data class Error(
        /** User-facing explanation of the failure. */
        val message: String,
    ) : UpdateUiState
}

/** Drives the GitHub build's check, download, and install flow in Settings. */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    updateConfig: UpdateConfig,
    private val updateRepository: UpdateRepository,
    private val updateDownloader: UpdateDownloader,
    private val apkInstaller: ApkInstaller,
    updateCheckCache: UpdateCheckCache,
) : ViewModel() {
    private val installedVersion: SemVer? = updateConfig.installedVersion
    private val _uiState = MutableStateFlow<UpdateUiState>(
        updateCheckCache.latestUpdate.value?.let { UpdateUiState.Available(it) } ?: UpdateUiState.Idle
    )
    /** Current state of the check, download, and install flow. */
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    /** Checks GitHub for a newer eligible release. */
    fun checkForUpdates() {
        _uiState.value = UpdateUiState.Checking
        viewModelScope.launch {
            _uiState.value = updateRepository.fetchLatestRelease().fold(
                onSuccess = ::toUiState,
                onFailure = { UpdateUiState.Error(it.message ?: "Update check failed") },
            )
        }
    }

    private fun toUiState(update: AppUpdate): UpdateUiState =
        if (installedVersion != null && update.version > installedVersion) {
            UpdateUiState.Available(update)
        } else {
            UpdateUiState.UpToDate
        }

    /** Enqueues the selected release APK and polls until it completes. */
    fun startDownload() {
        val update = (_uiState.value as? UpdateUiState.Available)?.update ?: return
        _uiState.value = UpdateUiState.Downloading(update, 0)
        viewModelScope.launch {
            pollDownload(update, updateDownloader.enqueue(update))
        }
    }

    private suspend fun pollDownload(update: AppUpdate, downloadId: Long) {
        while (true) {
            when (val status = updateDownloader.queryStatus(downloadId)) {
                is DownloadStatus.InProgress -> {
                    _uiState.value = UpdateUiState.Downloading(update, status.percent)
                    delay(POLL_INTERVAL_MS)
                }
                is DownloadStatus.Successful -> {
                    _uiState.value = UpdateUiState.ReadyToInstall(update, status.file)
                    return
                }
                is DownloadStatus.Failed -> {
                    _uiState.value = UpdateUiState.Error(status.reason)
                    return
                }
            }
        }
    }

    /** Starts installation when the current state contains a completed download. */
    fun startInstall() {
        (_uiState.value as? UpdateUiState.ReadyToInstall)?.let { apkInstaller.install(it.apkFile) }
    }

    /** Returns whether Android package-install permission is already granted. */
    fun canInstallPackages(): Boolean = apkInstaller.canInstallPackages()

    /** Returns the system settings intent used to grant package-install permission. */
    fun unknownSourcesSettingsIntent(): Intent = apkInstaller.unknownSourcesSettingsIntent()

    private companion object {
        const val POLL_INTERVAL_MS = 500L
    }
}
