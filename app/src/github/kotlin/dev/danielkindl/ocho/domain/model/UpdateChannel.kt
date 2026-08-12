package dev.danielkindl.ocho.domain.model

/** Which stream of GitHub releases a build follows. */
enum class UpdateChannel(
    /** Stable identifier used when selecting the GitHub endpoint. */
    val id: String,
) {
    /** Follows GitHub's latest stable release. */
    Stable("stable"),
    /** Follows the newest eligible GitHub prerelease. */
    Dev("dev"),
    ;

    /** Conversion helper used by generated build metadata. */
    companion object {
        /** Returns the matching channel, defaulting safely to stable. */
        fun fromId(id: String): UpdateChannel = entries.firstOrNull { it.id == id } ?: Stable
    }
}
