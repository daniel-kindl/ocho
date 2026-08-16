package dev.danielkindl.ocho.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.danielkindl.ocho.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the one-time setup shown before the main navigation graph. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository,
) : ViewModel() {
    /** Whether the introduction has already been completed. */
    val isCompleted: StateFlow<Boolean> = repository.isCompleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Stores completion and lets the app continue into its normal first-run flow. */
    fun complete() {
        viewModelScope.launch { repository.markCompleted() }
    }
}
