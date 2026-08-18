package dev.danielkindl.ocho.ui.settings

import android.content.Context
import dev.danielkindl.ocho.R
import dev.danielkindl.ocho.data.update.ApkInstaller
import dev.danielkindl.ocho.data.update.DownloadStatus
import dev.danielkindl.ocho.data.update.PendingDownload
import dev.danielkindl.ocho.data.update.PendingDownloadStore
import dev.danielkindl.ocho.data.update.UpdateCheckCache
import dev.danielkindl.ocho.data.update.UpdateDownloader
import dev.danielkindl.ocho.domain.model.AppUpdate
import dev.danielkindl.ocho.domain.model.SemVer
import dev.danielkindl.ocho.domain.model.UpdateChannel
import dev.danielkindl.ocho.domain.model.UpdateConfig
import dev.danielkindl.ocho.domain.repository.UpdateRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val updateRepository = mockk<UpdateRepository>()
    private val updateDownloader = mockk<UpdateDownloader>()
    private val apkInstaller = mockk<ApkInstaller>()
    private val pendingDownloadStore = mockk<PendingDownloadStore>()
    private val updateCheckCache = UpdateCheckCache()
    private val updateConfig = UpdateConfig("daniel-kindl/ocho", UpdateChannel.Stable, SemVer.parse("2.2.0"))
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { context.getString(R.string.update_unofficial_asset) } returns "Update asset is not an official Ocho release"
        every { context.getString(R.string.update_invalid_apk) } returns "Downloaded APK is not a valid Ocho update"
        coEvery { pendingDownloadStore.read() } returns null
        coEvery { pendingDownloadStore.write(any()) } just Runs
        coEvery { pendingDownloadStore.clear() } just Runs
        every { updateDownloader.cleanupAppOwnedApks(any()) } just Runs
        every { updateDownloader.remove(any()) } returns true
    }
    @After fun tearDown() = Dispatchers.resetMain()
    private fun viewModel() = UpdateViewModel(
        context,
        updateConfig,
        updateRepository,
        updateDownloader,
        apkInstaller,
        updateCheckCache,
        pendingDownloadStore,
    )
    private fun update(version: String) = AppUpdate(
        checkNotNull(SemVer.parse(version)),
        "v$version",
        "https://github.com/daniel-kindl/ocho/releases/download/v$version/app-github-release.apk",
        "notes",
    )

    @Test fun `checkForUpdates surfaces Available when release is newer`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.success(update("3.0.0"))
        val viewModel = viewModel(); viewModel.checkForUpdates()
        assertEquals(UpdateUiState.Available(update("3.0.0")), viewModel.uiState.value)
    }
    @Test fun `checkForUpdates surfaces UpToDate when release is not newer`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.success(update("2.2.0"))
        val viewModel = viewModel(); viewModel.checkForUpdates()
        assertEquals(UpdateUiState.UpToDate, viewModel.uiState.value)
    }
    @Test fun `checkForUpdates surfaces Error on failure`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.failure(IllegalStateException("boom"))
        val viewModel = viewModel(); viewModel.checkForUpdates()
        assertEquals(UpdateUiState.Error("boom"), viewModel.uiState.value)
    }
    @Test fun `startDownload polls until success`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.success(update("3.0.0"))
        val viewModel = viewModel(); viewModel.checkForUpdates()
        every { updateDownloader.enqueue(any()) } returns 42L
        val apkFile = File("ocho-3.0.0.apk")
        every { updateDownloader.queryStatus(42L) } returnsMany listOf(DownloadStatus.InProgress(50), DownloadStatus.Successful(apkFile))
        viewModel.startDownload(); advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue("Expected ReadyToInstall but was $state", state is UpdateUiState.ReadyToInstall)
        assertEquals(apkFile, (state as UpdateUiState.ReadyToInstall).apkFile)
    }
    @Test fun `startDownload surfaces Error when download fails`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.success(update("3.0.0"))
        val viewModel = viewModel(); viewModel.checkForUpdates()
        every { updateDownloader.enqueue(any()) } returns 42L
        every { updateDownloader.queryStatus(42L) } returns DownloadStatus.Failed("network error")
        viewModel.startDownload()
        assertEquals(UpdateUiState.Error("network error"), viewModel.uiState.value)
    }
    @Test fun `startInstall delegates to ApkInstaller once ready`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.success(update("3.0.0"))
        val viewModel = viewModel(); viewModel.checkForUpdates()
        val apkFile = File("ocho-3.0.0.apk")
        every { updateDownloader.enqueue(any()) } returns 42L
        every { updateDownloader.queryStatus(42L) } returns DownloadStatus.Successful(apkFile)
        every { apkInstaller.install(apkFile) } returns true
        viewModel.startDownload(); viewModel.startInstall()
        io.mockk.verify(exactly = 1) { apkInstaller.install(apkFile) }
    }

    @Test fun `restores a completed download after process recreation`() = runTest(dispatcher) {
        val restoredUpdate = update("3.0.0")
        val apkFile = File("ocho-3.0.0.apk")
        coEvery { pendingDownloadStore.read() } returns PendingDownload(
            downloadId = 42L,
            update = restoredUpdate,
            fileName = apkFile.name,
        )
        every { updateDownloader.queryStatus(42L) } returns DownloadStatus.Successful(apkFile)

        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(UpdateUiState.ReadyToInstall(restoredUpdate, apkFile), viewModel.uiState.value)
    }

    @Test fun `does not install when the installer rejects the downloaded apk`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.success(update("3.0.0"))
        val viewModel = viewModel()
        viewModel.checkForUpdates()
        val apkFile = File("ocho-3.0.0.apk")
        every { updateDownloader.enqueue(any()) } returns 42L
        every { updateDownloader.queryStatus(42L) } returns DownloadStatus.Successful(apkFile)
        every { apkInstaller.install(apkFile) } returns false

        viewModel.startDownload()
        viewModel.startInstall()

        assertEquals(UpdateUiState.Error("Downloaded APK is not a valid Ocho update"), viewModel.uiState.value)
    }

    @Test fun `does not enqueue an untrusted update asset`() = runTest(dispatcher) {
        coEvery { updateRepository.fetchLatestRelease() } returns Result.success(
            update("3.0.0").copy(downloadUrl = "https://example.com/update.apk")
        )
        val viewModel = viewModel()
        viewModel.checkForUpdates()
        viewModel.startDownload()

        assertEquals(UpdateUiState.Error("Update asset is not an official Ocho release"), viewModel.uiState.value)
        io.mockk.verify(exactly = 0) { updateDownloader.enqueue(any()) }
    }
}
