package dev.danielkindl.ocho.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielkindl.ocho.R
import dev.danielkindl.ocho.core.format.formatCountdown
import dev.danielkindl.ocho.core.format.formatElapsed
import dev.danielkindl.ocho.domain.model.Phase
import dev.danielkindl.ocho.domain.model.SessionSnapshot
import dev.danielkindl.ocho.domain.model.SessionStatus
import dev.danielkindl.ocho.domain.model.WorkoutMode
import dev.danielkindl.ocho.ui.components.PhaseClock
import dev.danielkindl.ocho.ui.components.PhaseLabel
import dev.danielkindl.ocho.ui.components.PhaseScaffold
import dev.danielkindl.ocho.ui.components.PrimarySessionControl
import dev.danielkindl.ocho.ui.components.SUBDUED_ON_PLATE
import dev.danielkindl.ocho.ui.components.SecondarySessionControl
import dev.danielkindl.ocho.ui.components.SessionColumn
import dev.danielkindl.ocho.ui.components.SessionLifecycleScaffold

/**
 * A running session, of any mode.
 *
 * Replaces the separate EMOM and Tabata session screens. Nothing here is
 * mode-specific: everything the screen draws comes from [SessionSnapshot], and the
 * phase colour, label and clock are already derived from it. The two screens had
 * become copies of each other once v3.1.0 moved session ownership into the
 * controller.
 *
 * The full-bleed phase colour is the primary information channel. Work and rest
 * differ by lightness as well as hue, so the distinction survives with no colour
 * vision at all, and the uppercase label carries the same information redundantly.
 *
 * @param onSessionFinished invoked on an explicit stop, not on completion, which
 *   shows its own summary first.
 */
@Composable
fun SessionScreen(
    onSessionFinished: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SessionLifecycleScaffold(
        status = state.status,
        onSessionFinished = onSessionFinished,
        onStopSession = viewModel::stopSession,
    ) { onRequestExit ->
        PhaseScaffold(phase = state.phase) { theme ->
            when (state.status) {
                SessionStatus.CountingDown -> PrepareContent(
                    secondsRemaining = state.countdownSecondsRemaining,
                    onPlate = theme.onPlate,
                    onStop = onRequestExit,
                )

                SessionStatus.Completed -> CompleteContent(
                    state = state,
                    onPlate = theme.onPlate,
                    onDone = onSessionFinished,
                )

                else -> RunningContent(
                    state = state,
                    onPlate = theme.onPlate,
                    onPauseResume = {
                        if (state.status == SessionStatus.Paused) {
                            viewModel.resumeSession()
                        } else {
                            viewModel.pauseSession()
                        }
                    },
                    onStop = onRequestExit,
                )
            }
        }
    }
}

@Composable
private fun PrepareContent(secondsRemaining: Int, onPlate: Color, onStop: () -> Unit) {
    SessionColumn {
        PhaseLabel(stringResource(R.string.phase_prepare), onPlate)
        PhaseClock(secondsRemaining.toString(), onPlate)
        SecondarySessionControl(
            label = stringResource(R.string.action_stop),
            onPlate = onPlate,
            onClick = onStop,
            icon = painterResource(R.drawable.ic_square),
        )
    }
}

@Composable
private fun CompleteContent(state: SessionSnapshot, onPlate: Color, onDone: () -> Unit) {
    SessionColumn {
        PhaseLabel(stringResource(R.string.phase_complete), onPlate)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PhaseClock(state.elapsedMillis.formatElapsed(), onPlate)
            // AMRAP reports no round count, since its rounds are whatever the athlete
            // managed. Omitted rather than shown as zero.
            if (state.totalRounds > 0) {
                Spacer(Modifier.height(8.dp))
                SubduedLine(
                    pluralStringResource(
                        if (state.mode == WorkoutMode.CUSTOM) {
                            R.plurals.session_set_count
                        } else {
                            R.plurals.session_round_count
                        },
                        state.totalRounds,
                        state.totalRounds,
                    ),
                    onPlate,
                )
            }
        }
        SecondarySessionControl(
            label = stringResource(R.string.action_done),
            onPlate = onPlate,
            onClick = onDone,
        )
    }
}

@Composable
private fun RunningContent(
    state: SessionSnapshot,
    onPlate: Color,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
) {
    val isPaused = state.status == SessionStatus.Paused
    val phaseName = stringResource(
        if (state.phase == Phase.REST) R.string.phase_rest else R.string.phase_work,
    )

    SessionColumn {
        PhaseLabel(
            if (isPaused) stringResource(R.string.phase_paused, phaseName) else phaseName,
            onPlate,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PhaseClock(state.remainingInPhaseMillis.formatCountdown(), onPlate)
            if (state.totalRounds > 0) {
                Spacer(Modifier.height(12.dp))
                SubduedLine(
                    text = stringResource(
                        R.string.session_progress,
                        stringResource(state.mode.progressUnitRes()),
                        state.currentRound,
                        state.totalRounds,
                    ),
                    onPlate = onPlate,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(4.dp))
            SubduedLine(state.elapsedMillis.formatElapsed(), onPlate)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimarySessionControl(
                label = stringResource(
                    if (isPaused) R.string.action_resume else R.string.action_pause,
                ),
                icon = painterResource(
                    if (isPaused) R.drawable.ic_play else R.drawable.ic_pause
                ),
                onPlate = onPlate,
                onClick = onPauseResume,
            )
            SecondarySessionControl(
                label = stringResource(R.string.action_stop),
                onPlate = onPlate,
                onClick = onStop,
            )
        }
    }
}

private fun WorkoutMode.progressUnitRes(): Int =
    if (this == WorkoutMode.CUSTOM) R.string.session_set_unit else R.string.session_round_unit

/** Secondary text on a phase plate, at the shared subdued opacity. */
@Composable
private fun SubduedLine(
    text: String,
    onPlate: Color,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall,
) {
    Text(
        text = text,
        style = style,
        color = onPlate.copy(alpha = SUBDUED_ON_PLATE),
        textAlign = TextAlign.Center,
    )
}
