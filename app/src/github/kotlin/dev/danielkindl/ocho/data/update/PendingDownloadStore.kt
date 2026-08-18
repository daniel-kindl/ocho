package dev.danielkindl.ocho.data.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.danielkindl.ocho.domain.model.AppUpdate
import dev.danielkindl.ocho.domain.model.SemVer
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** A DownloadManager job that must survive an updater process restart. */
data class PendingDownload(
    /** Android DownloadManager identifier. */
    val downloadId: Long,
    /** Release represented by the download. */
    val update: AppUpdate,
    /** Filename selected in the app-private downloads directory. */
    val fileName: String,
)

/** Persistence boundary for the one updater job owned by this application. */
interface PendingDownloadStore {
    /** Reads the persisted job, or null when no valid job is pending. */
    suspend fun read(): PendingDownload?
    /** Persists the job immediately after it is enqueued. */
    suspend fun write(download: PendingDownload)
    /** Removes the persisted job after completion or failure. */
    suspend fun clear()
}

/** Stores updater state alongside the existing application preferences DataStore. */
@Singleton
class DataStorePendingDownloadStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PendingDownloadStore {
    override suspend fun read(): PendingDownload? {
        val preferences = dataStore.data.first()
        val tagName = preferences[TAG_NAME]
        val version = tagName?.let(SemVer::parse)
        val downloadUrl = preferences[DOWNLOAD_URL]
        val downloadId = preferences[DOWNLOAD_ID]
        val fileName = preferences[FILE_NAME]
        if (tagName == null || version == null || downloadUrl == null) return null
        if (downloadId == null || fileName == null) return null
        return PendingDownload(
            downloadId = downloadId,
            update = AppUpdate(
                version = version,
                tagName = tagName,
                downloadUrl = downloadUrl,
                releaseNotes = preferences[RELEASE_NOTES].orEmpty(),
            ),
            fileName = fileName,
        )
    }

    override suspend fun write(download: PendingDownload) {
        dataStore.edit { preferences ->
            preferences[DOWNLOAD_ID] = download.downloadId
            preferences[TAG_NAME] = download.update.tagName
            preferences[DOWNLOAD_URL] = download.update.downloadUrl
            preferences[RELEASE_NOTES] = download.update.releaseNotes
            preferences[FILE_NAME] = download.fileName
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(DOWNLOAD_ID)
            preferences.remove(TAG_NAME)
            preferences.remove(DOWNLOAD_URL)
            preferences.remove(RELEASE_NOTES)
            preferences.remove(FILE_NAME)
        }
    }

    private companion object {
        val DOWNLOAD_ID = longPreferencesKey("github_update_download_id")
        val TAG_NAME = stringPreferencesKey("github_update_tag_name")
        val DOWNLOAD_URL = stringPreferencesKey("github_update_download_url")
        val RELEASE_NOTES = stringPreferencesKey("github_update_release_notes")
        val FILE_NAME = stringPreferencesKey("github_update_file_name")
    }
}
