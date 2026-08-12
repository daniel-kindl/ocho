package dev.danielkindl.ocho.domain.model

/** A version number as defined by Semantic Versioning 2.0.0. */
data class SemVer(
    /** Major compatibility version. */
    val major: Int,
    /** Backwards-compatible feature version. */
    val minor: Int,
    /** Backwards-compatible fix version. */
    val patch: Int,
    /** Ordered prerelease identifiers, empty for a stable release. */
    val preRelease: List<String> = emptyList(),
) : Comparable<SemVer> {

    override fun compareTo(other: SemVer): Int {
        val core = compareValuesBy(this, other, SemVer::major, SemVer::minor, SemVer::patch)
        return if (core != 0) core else comparePreRelease(preRelease, other.preRelease)
    }

    /** Parsing and validation entry points for semantic versions. */
    companion object {
        private const val CORE_PART_COUNT = 3

        /** Parses a strict SemVer string, accepting an optional leading `v`. */
        fun parse(raw: String): SemVer? {
            val withoutMetadata = raw.removePrefix("v").substringBefore('+')
            val hyphen = withoutMetadata.indexOf('-')
            val hasPreRelease = hyphen >= 0
            val coreRaw = if (hasPreRelease) withoutMetadata.substring(0, hyphen) else withoutMetadata
            val core = parseCore(coreRaw) ?: return null
            val identifiers =
                if (hasPreRelease) withoutMetadata.substring(hyphen + 1).split(".") else emptyList()
            if (identifiers.any { !isValidIdentifier(it) }) return null
            return SemVer(core[0], core[1], core[2], identifiers)
        }

        private fun parseCore(core: String): List<Int>? {
            val parts = core.split(".")
            if (parts.size != CORE_PART_COUNT) return null
            return parts.map { part ->
                val value = part.toIntOrNull() ?: return null
                if (value < 0 || hasLeadingZero(part)) return null
                value
            }
        }

        private fun comparePreRelease(left: List<String>, right: List<String>): Int {
            if (left.isEmpty() || right.isEmpty()) {
                return when {
                    left.isEmpty() && right.isEmpty() -> 0
                    left.isEmpty() -> 1
                    else -> -1
                }
            }
            for (index in 0 until minOf(left.size, right.size)) {
                val result = compareIdentifier(left[index], right[index])
                if (result != 0) return result
            }
            return left.size.compareTo(right.size)
        }

        private fun compareIdentifier(left: String, right: String): Int {
            val leftNumeric = left.toLongOrNull()
            val rightNumeric = right.toLongOrNull()
            return when {
                leftNumeric != null && rightNumeric != null -> leftNumeric.compareTo(rightNumeric)
                leftNumeric != null -> -1
                rightNumeric != null -> 1
                else -> left.compareTo(right)
            }
        }

        private fun isValidIdentifier(identifier: String): Boolean {
            if (identifier.isEmpty()) return false
            val allowed = identifier.all { it in '0'..'9' || it in 'a'..'z' || it in 'A'..'Z' || it == '-' }
            if (!allowed) return false
            return !(identifier.all { it in '0'..'9' } && hasLeadingZero(identifier))
        }

        private fun hasLeadingZero(part: String): Boolean = part.length > 1 && part.startsWith('0')
    }
}
