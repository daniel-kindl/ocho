package dev.danielkindl.ocho.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GithubAssetPolicyTest {
    private val url = "https://github.com/daniel-kindl/ocho/releases/download/v3.6.0/${GithubAssetPolicy.APK_ASSET_NAME}"

    @Test fun `accepts the official release asset`() {
        assertTrue(GithubAssetPolicy.accepts(GithubAssetPolicy.APK_ASSET_NAME, url, "daniel-kindl/ocho", "v3.6.0"))
    }

    @Test fun `rejects non HTTPS URLs`() {
        assertFalse(GithubAssetPolicy.accepts(GithubAssetPolicy.APK_ASSET_NAME, url.replace("https", "http"), "daniel-kindl/ocho", "v3.6.0"))
    }

    @Test fun `rejects a different host`() {
        assertFalse(GithubAssetPolicy.accepts(GithubAssetPolicy.APK_ASSET_NAME, url.replace("github.com", "example.com"), "daniel-kindl/ocho", "v3.6.0"))
    }

    @Test fun `rejects query strings and unexpected paths`() {
        assertFalse(GithubAssetPolicy.accepts(GithubAssetPolicy.APK_ASSET_NAME, "$url?download=1", "daniel-kindl/ocho", "v3.6.0"))
        assertFalse(GithubAssetPolicy.accepts(GithubAssetPolicy.APK_ASSET_NAME, url.replace("v3.6.0", "v3.5.0"), "daniel-kindl/ocho", "v3.6.0"))
    }
}
