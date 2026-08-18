package dev.danielkindl.ocho.data.update

import dev.danielkindl.ocho.domain.model.AppUpdate
import dev.danielkindl.ocho.domain.model.SemVer
import dev.danielkindl.ocho.domain.model.UpdateChannel
import dev.danielkindl.ocho.domain.model.UpdateConfig
import dev.danielkindl.ocho.domain.repository.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/** Reads eligible releases from the GitHub Releases API. */
class UpdateRepositoryImpl @Inject constructor(
    private val config: UpdateConfig,
) : UpdateRepository {
    override suspend fun fetchLatestRelease(): Result<AppUpdate> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(endpoint()).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "ocho-android")
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("GitHub API returned ${connection.responseCode}")
                }
                parseResponse(connection.inputStream.bufferedReader().use { it.readText() })
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun endpoint(): String = when (config.channel) {
        UpdateChannel.Stable -> "$API_BASE/${config.repoSlug}/releases/latest"
        UpdateChannel.Dev -> "$API_BASE/${config.repoSlug}/releases?per_page=$DEV_PAGE_SIZE"
    }

    internal fun parseResponse(body: String): AppUpdate = when (config.channel) {
        UpdateChannel.Stable -> parseRelease(JSONObject(body))
        UpdateChannel.Dev -> parseNewestPreRelease(body)
    }

    private fun parseNewestPreRelease(body: String): AppUpdate {
        val releases = JSONArray(body)
        val newest = (0 until releases.length())
            .map { releases.getJSONObject(it) }
            .filter { it.optBoolean("prerelease", false) }
            .mapNotNull { release ->
                SemVer.parse(release.optString("tag_name"))?.let { version -> version to release }
            }
            .maxByOrNull { (version, _) -> version }
            ?: error("No pre-release with a valid version tag was found")
        return parseRelease(newest.second)
    }

    internal fun parseRelease(json: JSONObject): AppUpdate {
        val tagName = json.getString("tag_name")
        val version = SemVer.parse(tagName) ?: error("Malformed release tag: $tagName")
        val assets = json.getJSONArray("assets")
        val downloadUrl = (0 until assets.length())
            .map { assets.getJSONObject(it) }
            .firstOrNull { asset ->
                GithubAssetPolicy.accepts(
                    assetName = asset.optString("name"),
                    downloadUrl = asset.optString("browser_download_url"),
                    repoSlug = config.repoSlug,
                    tagName = tagName,
                )
            }
            ?.getString("browser_download_url")
            ?: error("No APK asset found in release $tagName")
        return AppUpdate(version, tagName, downloadUrl, json.optString("body", ""))
    }

    private companion object {
        const val API_BASE = "https://api.github.com/repos"
        const val TIMEOUT_MS = 10_000
        const val DEV_PAGE_SIZE = 20
    }
}
