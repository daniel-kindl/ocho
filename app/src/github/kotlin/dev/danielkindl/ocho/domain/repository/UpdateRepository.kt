package dev.danielkindl.ocho.domain.repository

import dev.danielkindl.ocho.domain.model.AppUpdate

/** Looks up the newest release eligible for this GitHub build. */
interface UpdateRepository {
    /** Returns the newest release eligible for the configured channel. */
    suspend fun fetchLatestRelease(): Result<AppUpdate>
}
