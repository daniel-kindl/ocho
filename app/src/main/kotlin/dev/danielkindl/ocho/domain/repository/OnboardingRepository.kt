package dev.danielkindl.ocho.domain.repository

import kotlinx.coroutines.flow.Flow

/** Persists whether the one-time introduction has been completed. */
interface OnboardingRepository {
    /** Emits the current completion state. */
    fun isCompleted(): Flow<Boolean>

    /** Marks the introduction as completed. */
    suspend fun markCompleted()
}
