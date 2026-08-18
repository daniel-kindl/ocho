package dev.danielkindl.ocho.data.update

import java.net.URI

/** Trust policy for the APK asset published by this repository. */
internal object GithubAssetPolicy {
    const val APK_ASSET_NAME = "app-github-release.apk"
    private const val TRUSTED_HOST = "github.com"

    /** Accepts only the exact HTTPS release asset for the configured repository/tag. */
    fun accepts(
        assetName: String,
        downloadUrl: String,
        repoSlug: String,
        tagName: String,
    ): Boolean = runCatching {
        val uri = URI(downloadUrl)
        uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals(TRUSTED_HOST, ignoreCase = true) &&
            uri.query == null &&
            uri.fragment == null &&
            assetName == APK_ASSET_NAME &&
            uri.path == "/$repoSlug/releases/download/$tagName/$APK_ASSET_NAME"
    }.getOrDefault(false)
}
