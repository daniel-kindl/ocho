package dev.danielkindl.ocho.data.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

/** Installs a downloaded GitHub APK over the running app. */
class ApkInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Returns whether Android will allow this app to request package installs. */
    fun canInstallPackages(): Boolean = context.packageManager.canRequestPackageInstalls()

    /** Builds the system settings intent used to grant package-install permission. */
    fun unknownSourcesSettingsIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData("package:${context.packageName}".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Hands the downloaded APK to the appropriate Android installer flow. */
    fun install(apkFile: File): Boolean {
        if (!apkFile.isFile || !apkFile.canRead()) return false
        val packageName = context.packageManager
            .getPackageArchiveInfo(apkFile.absolutePath, 0)
            ?.packageName
        if (!isMatchingPackageName(packageName, context.packageName)) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installViaPackageInstaller(apkFile)
            // PackageInstaller has copied the bytes into its session before commit.
            // Older intent-based installation keeps the source until the next safe
            // updater cleanup because the receiving installer owns the read timing.
            if (isSourceSafeToDeleteImmediately(Build.VERSION.SDK_INT)) apkFile.delete()
        } else {
            installViaIntent(apkFile)
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun installViaPackageInstaller(apkFile: File) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        val sessionId = packageInstaller.createSession(params)
        packageInstaller.openSession(sessionId).use { session ->
            FileInputStream(apkFile).use { input ->
                session.openWrite(SESSION_NAME, 0L, apkFile.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }
            session.commit(installResultPendingIntent(sessionId).intentSender)
        }
    }

    private fun installViaIntent(apkFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, APK_MIME_TYPE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun installResultPendingIntent(sessionId: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(context, InstallResultReceiver::class.java),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val SESSION_NAME = "package"
    }
}

/** Kept separate so the package-identity rule is unit-testable without Android. */
internal fun isMatchingPackageName(actual: String?, expected: String): Boolean = actual == expected

/** Android S+ copies the APK into a PackageInstaller session before commit. */
internal fun isSourceSafeToDeleteImmediately(apiLevel: Int): Boolean =
    apiLevel >= Build.VERSION_CODES.S
