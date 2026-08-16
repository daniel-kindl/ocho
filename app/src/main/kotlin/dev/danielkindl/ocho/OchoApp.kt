package dev.danielkindl.ocho

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point and Hilt root.
 *
 * Distribution-specific startup work is injected through [DistributionStartup].
 * The Play implementation performs a non-blocking Play-managed update check; the
 * GitHub implementation performs the existing launch-time release check.
 */
@HiltAndroidApp
class OchoApp : Application() {

    /** Distribution-specific startup implementation supplied by the flavor module. */
    @javax.inject.Inject
    lateinit var distributionStartup: DistributionStartup

    override fun onCreate() {
        super.onCreate()
        distributionStartup.start()
    }
}
