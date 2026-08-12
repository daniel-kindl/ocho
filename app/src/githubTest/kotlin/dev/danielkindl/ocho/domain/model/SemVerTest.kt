package dev.danielkindl.ocho.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemVerTest {
    @Test fun `parses a plain semver string`() = assertEquals(SemVer(2, 0, 1), SemVer.parse("2.0.1"))
    @Test fun `strips a leading v prefix`() = assertEquals(SemVer(2, 0, 1), SemVer.parse("v2.0.1"))
    @Test fun `compares numerically not lexically`() = assertTrue(SemVer.parse("2.10.0")!! > SemVer.parse("2.9.0")!!)
    @Test fun `equal versions compare as equal`() = assertEquals(0, SemVer(1, 2, 3).compareTo(SemVer(1, 2, 3)))
    @Test fun `a newer major version is greater`() = assertTrue(SemVer(3, 0, 0) > SemVer(2, 9, 9))
    @Test fun `equal versions are not strictly newer`() = assertFalse(SemVer(2, 2, 0) > SemVer(2, 2, 0))
    @Test fun `malformed input returns null`() {
        assertNull(SemVer.parse("abc")); assertNull(SemVer.parse("1.2")); assertNull(SemVer.parse("1.2.3.4")); assertNull(SemVer.parse(""))
    }
    @Test fun `parses a dev channel prerelease version`() = assertEquals(SemVer(3, 0, 0, listOf("dev", "7")), SemVer.parse("v3.0.0-dev.7"))
    @Test fun `prerelease identifiers compare numerically`() = assertTrue(SemVer.parse("3.0.0-dev.12")!! > SemVer.parse("3.0.0-dev.7")!!)
    @Test fun `a prerelease ranks below its release`() = assertTrue(SemVer.parse("3.0.0-dev.7")!! < SemVer.parse("3.0.0")!!)
    @Test fun `a prerelease of a later patch outranks current release`() = assertTrue(SemVer.parse("3.0.1-dev.1")!! > SemVer.parse("3.0.0")!!)
    @Test fun `numeric identifiers rank below alphanumeric ones`() = assertTrue(SemVer.parse("3.0.0-1")!! < SemVer.parse("3.0.0-alpha")!!)
    @Test fun `a longer identifier list outranks a shorter prefix`() = assertTrue(SemVer.parse("3.0.0-dev.1")!! > SemVer.parse("3.0.0-dev")!!)
    @Test fun `build metadata is ignored`() {
        assertEquals(SemVer(3, 0, 0), SemVer.parse("3.0.0+build.5"))
        assertEquals(SemVer(3, 0, 0, listOf("dev", "7")), SemVer.parse("3.0.0-dev.7+abc123"))
    }
    @Test fun `malformed prerelease identifiers return null`() {
        assertNull(SemVer.parse("3.0.0-")); assertNull(SemVer.parse("3.0.0-dev..1")); assertNull(SemVer.parse("3.0.0-dev.01"))
    }
}
