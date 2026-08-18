package dev.danielkindl.ocho.data.update

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateDownloaderStatusTest {
    @Test fun `computes percent for a partial download`() = assertEquals(50, computeDownloadPercent(50, 100))
    @Test fun `rounds down fractional percent`() = assertEquals(33, computeDownloadPercent(1, 3))
    @Test fun `returns 100 when the download is complete`() = assertEquals(100, computeDownloadPercent(100, 100))
    @Test fun `guards against division by zero when total is zero`() = assertEquals(0, computeDownloadPercent(0, 0))
    @Test fun `returns 0 when nothing has downloaded yet`() = assertEquals(0, computeDownloadPercent(0, 500))
    @Test fun `clamps a value above the reported total`() = assertEquals(100, computeDownloadPercent(150, 100))
    @Test fun `recognizes only updater owned apk names`() {
        org.junit.Assert.assertTrue(isAppOwnedApkName("ocho-v3.6.0.apk"))
        org.junit.Assert.assertFalse(isAppOwnedApkName("other-app.apk"))
        org.junit.Assert.assertFalse(isAppOwnedApkName("ocho-not-an-apk.txt"))
    }
}
