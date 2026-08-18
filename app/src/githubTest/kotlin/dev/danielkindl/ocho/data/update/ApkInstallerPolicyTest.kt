package dev.danielkindl.ocho.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkInstallerPolicyTest {
    @Test fun `accepts the installed package`() {
        assertTrue(isMatchingPackageName("dev.danielkindl.ocho", "dev.danielkindl.ocho"))
    }

    @Test fun `rejects a different package`() {
        assertFalse(isMatchingPackageName("com.example.other", "dev.danielkindl.ocho"))
        assertFalse(isMatchingPackageName(null, "dev.danielkindl.ocho"))
    }
    @Test fun `only PackageInstaller sessions delete the source immediately`() {
        assertTrue(isSourceSafeToDeleteImmediately(android.os.Build.VERSION_CODES.S))
        assertFalse(isSourceSafeToDeleteImmediately(android.os.Build.VERSION_CODES.R))
    }
}
