package dev.danielkindl.ocho.data.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.danielkindl.ocho.domain.model.AppUpdate
import dev.danielkindl.ocho.domain.model.SemVer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PendingDownloadStoreTest {
    private val store = DataStorePendingDownloadStore(InMemoryDataStore())
    private val pending = PendingDownload(
        downloadId = 17L,
        update = AppUpdate(
            version = SemVer(3, 6, 0),
            tagName = "v3.6.0",
            downloadUrl = "https://github.com/daniel-kindl/ocho/releases/download/v3.6.0/app-github-release.apk",
            releaseNotes = "notes",
        ),
        fileName = "ocho-v3.6.0.apk",
    )

    @Test fun `round trips a pending download`() = runTest {
        store.write(pending)
        assertEquals(pending, store.read())
    }

    @Test fun `clear removes the pending download`() = runTest {
        store.write(pending)
        store.clear()
        assertNull(store.read())
    }

    @Test fun `invalid persisted version is ignored`() = runTest {
        val dataStore = InMemoryDataStore()
        dataStore.updateData { preferences ->
            androidx.datastore.preferences.core.mutablePreferencesOf(
                stringPreferencesKey("github_update_tag_name") to "not-a-version",
            )
        }
        assertNull(DataStorePendingDownloadStore(dataStore).read())
    }
}

private class InMemoryDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    private val mutex = Mutex()

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
        mutex.withLock {
            transform(state.value).also { state.value = it }
        }
}
