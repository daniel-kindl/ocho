package dev.danielkindl.ocho.domain.model

/** Where the GitHub build looks for updates and which releases it accepts. */
data class UpdateConfig(
    /** GitHub repository in `owner/name` form. */
    val repoSlug: String,
    /** Release stream accepted by this build. */
    val channel: UpdateChannel,
    /** Installed version used to decide whether a release is newer. */
    val installedVersion: SemVer?,
)
