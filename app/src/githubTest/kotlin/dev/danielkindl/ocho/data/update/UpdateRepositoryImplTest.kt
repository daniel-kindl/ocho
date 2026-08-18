package dev.danielkindl.ocho.data.update

import dev.danielkindl.ocho.domain.model.SemVer
import dev.danielkindl.ocho.domain.model.UpdateChannel
import dev.danielkindl.ocho.domain.model.UpdateConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UpdateRepositoryImplTest {
    private fun repositoryFor(channel: UpdateChannel) = UpdateRepositoryImpl(
        UpdateConfig("daniel-kindl/ocho", channel, SemVer.parse("3.0.0"))
    )
    private val repository = repositoryFor(UpdateChannel.Stable)

    private fun releaseJson(
        tagName: String = "v2.3.0",
        assets: String? = null,
        body: String? = "Release notes",
        preRelease: Boolean? = null,
        assetName: String = GithubAssetPolicy.APK_ASSET_NAME,
        assetUrl: String? = null,
    ): String {
        val bodyField = body?.let { "\"body\":\"$it\"," }.orEmpty()
        val preReleaseField = preRelease?.let { "\"prerelease\":$it," }.orEmpty()
        val defaultAssets = "[{\"name\":\"$assetName\",\"browser_download_url\":\"${assetUrl ?: "https://github.com/daniel-kindl/ocho/releases/download/$tagName/${GithubAssetPolicy.APK_ASSET_NAME}"}\"}]"
        return "{\"tag_name\":\"$tagName\",$preReleaseField$bodyField\"assets\":${assets ?: defaultAssets}}"
    }

    @Test fun `parses a valid release`() {
        val update = repository.parseResponse(releaseJson())
        assertEquals(SemVer(2, 3, 0), update.version)
        assertEquals("v2.3.0", update.tagName)
        assertEquals("https://github.com/daniel-kindl/ocho/releases/download/v2.3.0/app-github-release.apk", update.downloadUrl)
        assertEquals("Release notes", update.releaseNotes)
    }
    @Test fun `defaults releaseNotes to empty string when body is missing`() =
        assertEquals("", repository.parseResponse(releaseJson(body = null)).releaseNotes)
    @Test fun `throws when the release tag is malformed semver`() = assertThrows(IllegalStateException::class.java) {
        repository.parseResponse(releaseJson(tagName = "not-a-version"))
    }
    @Test fun `throws when no apk asset is present`() = assertThrows(IllegalStateException::class.java) {
        repository.parseResponse(releaseJson(assets = "[{\"name\":\"README.md\",\"browser_download_url\":\"https://example.com/readme\"}]"))
    }
    @Test fun `rejects an apk with an unexpected asset name`() = assertThrows(IllegalStateException::class.java) {
        repository.parseResponse(releaseJson(assetName = "ocho.apk"))
    }
    @Test fun `rejects an apk hosted outside the official release path`() = assertThrows(IllegalStateException::class.java) {
        repository.parseResponse(releaseJson(assetUrl = "https://example.com/app-github-release.apk"))
    }
    @Test fun `throws when tag_name is missing`() = assertThrows(org.json.JSONException::class.java) {
        repository.parseResponse("{\"assets\":[]}")
    }
    @Test fun `dev channel picks the highest versioned prerelease`() {
        val releases = "[${releaseJson(tagName = "v3.0.0", preRelease = false)},${releaseJson(tagName = "v3.1.0-dev.7", preRelease = true)},${releaseJson(tagName = "v3.1.0-dev.12", preRelease = true)}]"
        assertEquals("v3.1.0-dev.12", repositoryFor(UpdateChannel.Dev).parseResponse(releases).tagName)
    }
    @Test fun `dev channel ignores stable releases entirely`() {
        val releases = "[${releaseJson(tagName = "v9.9.9", preRelease = false)},${releaseJson(tagName = "v3.1.0-dev.1", preRelease = true)}]"
        assertEquals("v3.1.0-dev.1", repositoryFor(UpdateChannel.Dev).parseResponse(releases).tagName)
    }
    @Test fun `dev channel throws when the list holds no prereleases`() = assertThrows(IllegalStateException::class.java) {
        repositoryFor(UpdateChannel.Dev).parseResponse("[${releaseJson(tagName = "v3.0.0", preRelease = false)}]")
    }
    @Test fun `dev channel skips prereleases whose tag is not valid semver`() {
        val releases = "[${releaseJson(tagName = "nightly", preRelease = true)},${releaseJson(tagName = "v3.1.0-dev.4", preRelease = true)}]"
        assertEquals("v3.1.0-dev.4", repositoryFor(UpdateChannel.Dev).parseResponse(releases).tagName)
    }
    @Test fun `a release with no prerelease flag is treated as stable`() = assertThrows(IllegalStateException::class.java) {
        repositoryFor(UpdateChannel.Dev).parseResponse("[${releaseJson(tagName = "v3.0.0", preRelease = null)}]")
    }
}
