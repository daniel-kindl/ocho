package dev.danielkindl.ocho.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import dev.danielkindl.ocho.R
import dev.danielkindl.ocho.domain.model.WorkoutMode
import dev.danielkindl.ocho.domain.model.WorkoutPreset
import dev.danielkindl.ocho.ui.setup.DurationValue
import dev.danielkindl.ocho.ui.setup.WorkoutPattern
import dev.danielkindl.ocho.ui.setup.WorkoutSetupUiState

/** Resolves a compact duration using the current locale's quantity resources. */
@Composable
fun durationText(value: DurationValue): String {
    val minutes = if (value.minutes > 0) {
        pluralStringResource(R.plurals.duration_minutes, value.minutes, value.minutes)
    } else {
        null
    }
    val seconds = if (value.seconds > 0 || minutes == null) {
        pluralStringResource(R.plurals.duration_seconds, value.seconds, value.seconds)
    } else {
        null
    }
    return listOfNotNull(minutes, seconds).joinToString(separator = " ")
}

/** Resolves the structured setup pattern into localized text. */
@Composable
fun WorkoutSetupUiState.patternText(): String = when (val pattern = pattern) {
    is WorkoutPattern.Emom -> stringResource(
        R.string.pattern_emom,
        pattern.rounds,
        durationText(pattern.interval),
    )
    is WorkoutPattern.Tabata -> stringResource(
        R.string.pattern_tabata,
        pattern.rounds,
        durationText(pattern.work),
        durationText(pattern.rest),
    )
    is WorkoutPattern.Amrap -> stringResource(
        R.string.pattern_amrap,
        durationText(pattern.total),
    )
    is WorkoutPattern.Custom -> if (pattern.rest.minutes > 0 || pattern.rest.seconds > 0) {
        stringResource(
            R.string.pattern_custom_with_rest,
            pattern.sets,
            durationText(pattern.work),
            durationText(pattern.rest),
        )
    } else {
        stringResource(
            R.string.pattern_custom_without_rest,
            pattern.sets,
            durationText(pattern.work),
        )
    }
}

/** Resolves the locale-aware default name shown when saving a setup. */
@Composable
fun WorkoutSetupUiState.defaultPresetNameText(): String = when (val pattern = pattern) {
    is WorkoutPattern.Emom -> stringResource(
        R.string.preset_default_emom,
        durationText(DurationValue(totalMinutes, totalSeconds)),
        durationText(pattern.interval),
    )
    is WorkoutPattern.Tabata -> stringResource(
        R.string.preset_default_tabata,
        durationText(DurationValue(totalMinutes, totalSeconds)),
        durationText(pattern.work),
        durationText(pattern.rest),
    )
    is WorkoutPattern.Amrap -> stringResource(
        R.string.preset_default_amrap,
        durationText(pattern.total),
    )
    is WorkoutPattern.Custom -> if (pattern.rest.minutes > 0 || pattern.rest.seconds > 0) {
        stringResource(
            R.string.preset_default_custom_with_rest,
            pattern.sets,
            durationText(pattern.work),
            durationText(pattern.rest),
        )
    } else {
        stringResource(
            R.string.preset_default_custom_without_rest,
            pattern.sets,
            durationText(pattern.work),
        )
    }
}

/** Resolves the mode-specific summary displayed under a saved preset name. */
@Composable
fun WorkoutPreset.summaryText(): String {
    val total = durationText(DurationValue(totalMinutes, totalSeconds))
    return when (mode) {
        WorkoutMode.EMOM -> stringResource(
            R.string.preset_summary_emom,
            total,
            durationText(DurationValue(intervalMinutes, intervalSeconds)),
        )
        WorkoutMode.TABATA -> stringResource(
            R.string.preset_summary_tabata,
            total,
            durationText(DurationValue(workMinutes, workSeconds)),
            durationText(DurationValue(restMinutes, restSeconds)),
        )
        WorkoutMode.AMRAP -> stringResource(R.string.preset_summary_amrap, total)
        WorkoutMode.CUSTOM -> if (restMinutes > 0 || restSeconds > 0) {
            pluralStringResource(
                R.plurals.preset_summary_custom_with_rest,
                setCount,
                durationText(DurationValue(workMinutes, workSeconds)),
                durationText(DurationValue(restMinutes, restSeconds)),
            )
        } else {
            pluralStringResource(
                R.plurals.preset_summary_custom_without_rest,
                setCount,
                durationText(DurationValue(workMinutes, workSeconds)),
            )
        }
    }
}
