package dev.danielkindl.ocho.data.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dev.danielkindl.ocho.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayUpdateCoordinatorTest {
    private fun context(): Context = mockk<Context>(relaxed = true) {
        every { getString(R.string.update_play_failed) } returns "Play update failed"
        every { getString(R.string.update_play_check_failed) } returns "Play update check failed"
    }

    @Test fun `reports Available only for an allowed flexible update`() {
        val client = FakePlayUpdateClient()
        val coordinator = PlayUpdateCoordinator(context(), client)

        coordinator.checkForUpdates()
        client.succeed(info(UpdateAvailability.UPDATE_AVAILABLE, flexibleAllowed = true))

        assertEquals(PlayUpdateState.Available, coordinator.state.value)
    }

    @Test fun `reports UpToDate when no update or flexible flow is available`() {
        val unavailableClient = FakePlayUpdateClient()
        val unavailable = PlayUpdateCoordinator(context(), unavailableClient)
        unavailable.checkForUpdates()
        unavailableClient.succeed(info(UpdateAvailability.UPDATE_NOT_AVAILABLE, flexibleAllowed = true))
        assertEquals(PlayUpdateState.UpToDate, unavailable.state.value)

        val disallowedClient = FakePlayUpdateClient()
        val disallowed = PlayUpdateCoordinator(context(), disallowedClient)
        disallowed.checkForUpdates()
        disallowedClient.succeed(info(UpdateAvailability.UPDATE_AVAILABLE, flexibleAllowed = false))
        assertEquals(PlayUpdateState.UpToDate, disallowed.state.value)
    }

    @Test fun `reports an error when Play update lookup fails`() {
        val client = FakePlayUpdateClient()
        val coordinator = PlayUpdateCoordinator(context(), client)
        coordinator.checkForUpdates()
        client.fail(IllegalStateException("offline"))

        assertEquals(PlayUpdateState.Error("offline"), coordinator.state.value)
    }

    @Test fun `maps install listener progress and terminal states`() {
        val client = FakePlayUpdateClient()
        val coordinator = PlayUpdateCoordinator(context(), client)

        client.emit(installState(InstallStatus.DOWNLOADING, downloaded = 50, total = 100))
        assertEquals(PlayUpdateState.Downloading(50), coordinator.state.value)

        client.emit(installState(InstallStatus.DOWNLOADING, downloaded = 1, total = 0))
        assertEquals(PlayUpdateState.Downloading(0), coordinator.state.value)

        client.emit(installState(InstallStatus.DOWNLOADED))
        assertEquals(PlayUpdateState.Downloaded, coordinator.state.value)
        client.emit(installState(InstallStatus.INSTALLED))
        assertEquals(PlayUpdateState.UpToDate, coordinator.state.value)
        client.emit(installState(InstallStatus.CANCELED))
        assertEquals(PlayUpdateState.Idle, coordinator.state.value)
        client.emit(installState(InstallStatus.FAILED))
        assertEquals(PlayUpdateState.Error("Play update failed"), coordinator.state.value)
    }

    @Test fun `delegates flexible start and completion only when available`() {
        val client = FakePlayUpdateClient()
        val coordinator = PlayUpdateCoordinator(context(), client)
        val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>()

        coordinator.startFlexibleUpdate(launcher)
        assertTrue(client.startedInfo == null)

        coordinator.checkForUpdates()
        val updateInfo = info(UpdateAvailability.UPDATE_AVAILABLE, flexibleAllowed = true)
        client.succeed(updateInfo)
        coordinator.startFlexibleUpdate(launcher)
        coordinator.completeUpdate()

        assertEquals(updateInfo, client.startedInfo)
        assertEquals(launcher, client.startedLauncher)
        assertTrue(client.completed)
    }

    private fun info(availability: Int, flexibleAllowed: Boolean): AppUpdateInfo = mockk {
        every { updateAvailability() } returns availability
        every { isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) } returns flexibleAllowed
        every { installStatus() } returns InstallStatus.UNKNOWN
    }

    private fun installState(status: Int, downloaded: Long = 0, total: Long = 0): InstallState = mockk {
        every { installStatus() } returns status
        every { bytesDownloaded() } returns downloaded
        every { totalBytesToDownload() } returns total
    }
}

private class FakePlayUpdateClient : PlayUpdateClient {
    private var source = TaskCompletionSource<AppUpdateInfo>()
    private var listener: InstallStateUpdatedListener? = null
    var startedInfo: AppUpdateInfo? = null
    var startedLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    var completed = false

    override val appUpdateInfo = source.task

    override fun registerListener(listener: InstallStateUpdatedListener) {
        this.listener = listener
    }

    override fun startUpdateFlowForResult(
        info: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        options: AppUpdateOptions,
    ) {
        startedInfo = info
        startedLauncher = launcher
    }

    override fun completeUpdate() {
        completed = true
    }

    fun succeed(info: AppUpdateInfo) {
        source.setResult(info)
        source = TaskCompletionSource()
    }

    fun fail(error: Exception) {
        source.setException(error)
    }

    fun emit(state: InstallState) {
        listener?.onStateUpdate(state)
    }
}
