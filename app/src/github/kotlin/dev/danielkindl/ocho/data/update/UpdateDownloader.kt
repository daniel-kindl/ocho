package dev.danielkindl.ocho.data.update

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import androidx.core.net.toUri
import android.os.Environment
import dev.danielkindl.ocho.domain.model.AppUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Current state reported by Android's download manager. */
sealed interface DownloadStatus {
    /** A download is active and has reached [percent]. */
    data class InProgress(val percent: Int) : DownloadStatus

    /** A download completed and produced [file]. */
    data class Successful(val file: File) : DownloadStatus

    /** A download failed and supplies [reason] for the UI. */
    data class Failed(val reason: String) : DownloadStatus
}

private const val PERCENT_MAX = 100

internal fun computeDownloadPercent(downloadedBytes: Long, totalBytes: Long): Int =
    if (totalBytes > 0) {
        ((downloadedBytes * PERCENT_MAX) / totalBytes).toInt().coerceIn(0, PERCENT_MAX)
    } else {
        0
    }

/** Returns whether a filename belongs to the updater's private APK namespace. */
internal fun isAppOwnedApkName(name: String): Boolean =
    name.startsWith("ocho-") && name.endsWith(".apk")

/** Downloads release APKs through Android's system DownloadManager. */
@Singleton
class UpdateDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /** Starts downloading the release APK and returns Android's download ID. */
    fun enqueue(update: AppUpdate): Long {
        val request = DownloadManager.Request(update.downloadUrl.toUri())
            .setTitle("Ocho ${update.tagName}")
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "ocho-${update.tagName}.apk",
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType(APK_MIME_TYPE)
        return downloadManager.enqueue(request)
    }

    /** Removes a previously enqueued job, if Android still knows about it. */
    fun remove(downloadId: Long): Boolean = downloadManager.remove(downloadId) > 0

    /** Deletes only stale APKs created by this updater. */
    fun cleanupAppOwnedApks(keepFile: File? = null) {
        val downloadDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
        downloadDirectory.listFiles()
            ?.filter { isAppOwnedApkName(it.name) && it.absolutePath != keepFile?.absolutePath }
            ?.forEach { it.delete() }
    }

    /** Reads the current state of a previously enqueued download. */
    fun queryStatus(downloadId: Long): DownloadStatus {
        downloadManager.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
            if (!cursor.moveToFirst()) return DownloadStatus.Failed("Download not found")
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            return when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> successfulStatus(cursor)
                DownloadManager.STATUS_FAILED -> failedStatus(cursor)
                else -> inProgressStatus(cursor)
            }
        }
    }

    private fun successfulStatus(cursor: Cursor): DownloadStatus {
        val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
        val path = localUri.toUri().path ?: return DownloadStatus.Failed("Missing local file path")
        return DownloadStatus.Successful(File(path))
    }

    private fun failedStatus(cursor: Cursor): DownloadStatus {
        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
        return DownloadStatus.Failed("Download failed (reason $reason)")
    }

    private fun inProgressStatus(cursor: Cursor): DownloadStatus {
        val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        return DownloadStatus.InProgress(computeDownloadPercent(downloaded, total))
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
