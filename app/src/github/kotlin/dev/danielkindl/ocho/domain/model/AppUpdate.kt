package dev.danielkindl.ocho.domain.model

/** A release available to install, as read from the GitHub Releases API. */
data class AppUpdate(
    /** Parsed semantic version used for update comparison. */
    val version: SemVer,
    /** Original GitHub release tag, retained for display. */
    val tagName: String,
    /** Public APK asset URL supplied by GitHub. */
    val downloadUrl: String,
    /** Release notes supplied by GitHub, possibly empty. */
    val releaseNotes: String,
)
