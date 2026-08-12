package dev.danielkindl.ocho.data.update

import dev.danielkindl.ocho.domain.model.AppUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Holds a successful launch-time update result until Settings is opened. */
@Singleton
class UpdateCheckCache @Inject constructor() {
    private val _latestUpdate = MutableStateFlow<AppUpdate?>(null)
    /** The newer release found during the application launch check, if any. */
    val latestUpdate: StateFlow<AppUpdate?> = _latestUpdate.asStateFlow()

    /** Replaces the cached launch-check result. */
    fun set(update: AppUpdate?) {
        _latestUpdate.value = update
    }
}
