package dev.danielkindl.ocho.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielkindl.ocho.ui.components.NotificationPermissionOnboardingStep

/** Shows the two-step first-run setup once, then renders the normal app content. */
@Composable
fun OnboardingGate(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinishedInSession: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val completed by viewModel.isCompleted.collectAsStateWithLifecycle()
    var showNotificationStep by rememberSaveable { mutableStateOf(false) }
    val setupStep = when {
        completed -> 3
        showNotificationStep -> 2
        else -> 1
    }

    AnimatedContent(
        targetState = setupStep,
        transitionSpec = {
            val movingForward = targetState > initialState
            val enterOffset: (Int) -> Int = { fullWidth ->
                if (movingForward) fullWidth else -fullWidth
            }
            val exitOffset: (Int) -> Int = { fullWidth ->
                if (movingForward) -fullWidth else fullWidth
            }
            (
                slideInHorizontally(
                    animationSpec = tween(360),
                    initialOffsetX = enterOffset,
                ) + fadeIn(tween(220))
            )
                .togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(360),
                        targetOffsetX = exitOffset,
                    ) + fadeOut(tween(180)),
                )
                .using(SizeTransform(clip = false))
        },
        label = "onboarding_step_transition",
    ) { step ->
        when (step) {
            1 -> OnboardingScreen(onContinue = { showNotificationStep = true })
            2 -> NotificationPermissionOnboardingStep(
                onComplete = {
                    viewModel.complete()
                    onFinishedInSession()
                },
            )
            else -> content()
        }
    }
}
