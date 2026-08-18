package dev.danielkindl.ocho.data.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Small seam around Play Core so update policy can be tested without Play Services. */
interface PlayUpdateClient {
    /** Resolves the current Play listing/update state. */
    val appUpdateInfo: Task<AppUpdateInfo>

    /** Starts receiving install progress callbacks. */
    fun registerListener(listener: InstallStateUpdatedListener)

    /** Starts Play's user-approved flexible update flow. */
    fun startUpdateFlowForResult(
        info: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        options: AppUpdateOptions,
    )

    /** Requests installation of a downloaded flexible update. */
    fun completeUpdate()
}

/** Production adapter for the Play Core update manager. */
class PlayUpdateManagerClient @Inject constructor(
    @ApplicationContext context: Context,
) : PlayUpdateClient {
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    override val appUpdateInfo: Task<AppUpdateInfo>
        get() = manager.appUpdateInfo

    override fun registerListener(listener: InstallStateUpdatedListener) {
        manager.registerListener(listener)
    }

    override fun startUpdateFlowForResult(
        info: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        options: AppUpdateOptions,
    ) {
        manager.startUpdateFlowForResult(info, launcher, options)
    }

    override fun completeUpdate() {
        manager.completeUpdate()
    }
}
