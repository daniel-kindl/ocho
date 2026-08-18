package dev.danielkindl.ocho.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielkindl.ocho.R
import dev.danielkindl.ocho.core.format.formatElapsed
import dev.danielkindl.ocho.domain.model.PREPARE_COUNTDOWN_MILLIS
import dev.danielkindl.ocho.domain.model.SessionRequest
import dev.danielkindl.ocho.domain.model.WorkoutMode
import dev.danielkindl.ocho.domain.model.WorkoutPreset
import dev.danielkindl.ocho.domain.model.limitPresetName
import dev.danielkindl.ocho.domain.model.toPlan
import dev.danielkindl.ocho.ui.components.DeletePresetDialog
import dev.danielkindl.ocho.ui.components.DurationPicker
import dev.danielkindl.ocho.ui.components.ErrorPlate
import dev.danielkindl.ocho.ui.components.PresetsSection
import dev.danielkindl.ocho.ui.components.RunTimeline
import dev.danielkindl.ocho.ui.components.SavePresetDialog
import dev.danielkindl.ocho.ui.components.WheelPicker
import dev.danielkindl.ocho.ui.components.toRunSegments
import dev.danielkindl.ocho.ui.text.defaultPresetNameText
import dev.danielkindl.ocho.ui.text.patternText
import dev.danielkindl.ocho.ui.text.summaryText

/**
 * Configures a workout of any mode.
 *
 * Replaces the separate EMOM and Tabata setup screens, which differed only in which
 * duration pickers they showed. The mode arrives as a navigation argument and
 * selects the pickers, the labels and the run timeline shape.
 *
 * @param onStartSession receives the assembled request; navigation is the caller's
 *   concern, so this screen stays independent of the nav graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSetupScreen(
    onStartSession: (SessionRequest) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: WorkoutSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val defaultPresetName = state.defaultPresetNameText()
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var dialogPresetName by rememberSaveable { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<WorkoutPreset?>(null) }

    if (showSaveDialog) {
        SavePresetDialog(
            name = dialogPresetName,
            onNameChange = { dialogPresetName = it },
            onSave = {
                viewModel.savePreset(
                    name = dialogPresetName.limitPresetName(),
                    fallbackName = defaultPresetName,
                )
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false },
        )
    }

    presetToDelete?.let { preset ->
        DeletePresetDialog(
            presetName = preset.name,
            onConfirm = {
                viewModel.deletePreset(preset.id)
                presetToDelete = null
            },
            onDismiss = { presetToDelete = null },
        )
    }

    Scaffold(
        topBar = { WorkoutSetupTopBar(state.mode, onNavigateUp) },
        bottomBar = { WorkoutSetupBottomBar(state, onStartSession) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = MAX_CONTENT_WIDTH)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SetupSummary(state)

                SetupPanel { WorkoutModePickers(state, viewModel) }

                if (state.intervalExceedsTotal) {
                    ErrorPlate(
                        message = stringResource(R.string.setup_interval_error),
                    )
                }

                if (state.isValid) {
                    SetupPanel {
                        RunTimeline(
                            segments = state.timelineSegments(),
                        )
                    }
                }

                PresetsSection(
                    presets = presets,
                    getLabel = { it.name },
                    getSummary = { it.summaryText() },
                    onPresetClick = viewModel::loadPreset,
                    onDeleteClick = { presetToDelete = it },
                    onSavePreset = {
                        dialogPresetName = defaultPresetName
                        showSaveDialog = true
                    },
                    saveEnabled = state.isValid,
                    canDelete = { !it.builtIn },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WorkoutSetupTopBar(
    mode: WorkoutMode,
    onNavigateUp: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(mode.titleRes())) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.action_back),
                )
            }
        },
    )
}

@Composable
private fun WorkoutSetupBottomBar(
    state: WorkoutSetupUiState,
    onStartSession: (SessionRequest) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
    ) {
        Button(
            onClick = { onStartSession(state.toRequest()) },
            enabled = state.isValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .height(52.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_play),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.action_start), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun WorkoutModePickers(
    state: WorkoutSetupUiState,
    viewModel: WorkoutSetupViewModel,
) {
    when (state.mode) {
        WorkoutMode.EMOM -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompactDurationPicker(
                label = stringResource(R.string.setup_total),
                minutes = state.totalMinutes,
                seconds = state.totalSeconds,
                onMinutesChange = viewModel::setTotalMinutes,
                onSecondsChange = viewModel::setTotalSeconds,
                modifier = Modifier.weight(1f),
            )
            CompactDurationPicker(
                label = stringResource(R.string.setup_interval),
                minutes = state.intervalMinutes,
                seconds = state.intervalSeconds,
                onMinutesChange = viewModel::setIntervalMinutes,
                onSecondsChange = viewModel::setIntervalSeconds,
                modifier = Modifier.weight(1f),
            )
        }

        WorkoutMode.TABATA -> Column {
            CompactDurationPicker(
                label = stringResource(R.string.setup_total_duration),
                minutes = state.totalMinutes,
                seconds = state.totalSeconds,
                onMinutesChange = viewModel::setTotalMinutes,
                onSecondsChange = viewModel::setTotalSeconds,
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            PhasePickerRow(
                work = DurationPickerState(
                    label = stringResource(R.string.setup_work),
                    minutes = state.workMinutes,
                    seconds = state.workSeconds,
                    onMinutesChange = viewModel::setWorkMinutes,
                    onSecondsChange = viewModel::setWorkSeconds,
                ),
                rest = DurationPickerState(
                    label = stringResource(R.string.setup_rest),
                    minutes = state.restMinutes,
                    seconds = state.restSeconds,
                    onMinutesChange = viewModel::setRestMinutes,
                    onSecondsChange = viewModel::setRestSeconds,
                ),
            )
        }

        WorkoutMode.AMRAP -> CompactDurationPicker(
            label = stringResource(R.string.setup_total_duration),
            minutes = state.totalMinutes,
            seconds = state.totalSeconds,
            onMinutesChange = viewModel::setTotalMinutes,
            onSecondsChange = viewModel::setTotalSeconds,
            modifier = Modifier.fillMaxWidth(),
        )

        WorkoutMode.CUSTOM -> Column {
            CountPicker(
                label = stringResource(R.string.setup_sets),
                count = state.setCount,
                onCountChange = viewModel::setCount,
                compact = true,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            PhasePickerRow(
                work = DurationPickerState(
                    label = stringResource(R.string.setup_work),
                    minutes = state.workMinutes,
                    seconds = state.workSeconds,
                    onMinutesChange = viewModel::setWorkMinutes,
                    onSecondsChange = viewModel::setWorkSeconds,
                ),
                rest = DurationPickerState(
                    label = stringResource(R.string.setup_rest),
                    minutes = state.restMinutes,
                    seconds = state.restSeconds,
                    onMinutesChange = viewModel::setRestMinutes,
                    onSecondsChange = viewModel::setRestSeconds,
                ),
            )
        }
    }
}

@Composable
private fun SetupSummary(state: WorkoutSetupUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.totalDurationMillis.formatElapsed(),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(state.mode.iconRes()),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.patternText(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SetupPanel(
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
private fun PhasePickerRow(
    work: DurationPickerState,
    rest: DurationPickerState,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactDurationPicker(
            label = work.label,
            minutes = work.minutes,
            seconds = work.seconds,
            onMinutesChange = work.onMinutesChange,
            onSecondsChange = work.onSecondsChange,
            modifier = Modifier.weight(1f),
        )
        CompactDurationPicker(
            label = rest.label,
            minutes = rest.minutes,
            seconds = rest.seconds,
            onMinutesChange = rest.onMinutesChange,
            onSecondsChange = rest.onSecondsChange,
            modifier = Modifier.weight(1f),
        )
    }
}

private data class DurationPickerState(
    val label: String,
    val minutes: Int,
    val seconds: Int,
    val onMinutesChange: (Int) -> Unit,
    val onSecondsChange: (Int) -> Unit,
)

@Composable
private fun CompactDurationPicker(
    label: String,
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    DurationPicker(
        label = label,
        minutes = minutes,
        seconds = seconds,
        onMinutesChange = onMinutesChange,
        onSecondsChange = onSecondsChange,
        modifier = modifier,
        compact = true,
    )
}

/** Resource-backed screen title for a mode. */
@StringRes
private fun WorkoutMode.titleRes(): Int = when (this) {
    WorkoutMode.EMOM -> R.string.mode_emom_title
    WorkoutMode.TABATA -> R.string.mode_tabata_title
    WorkoutMode.AMRAP -> R.string.mode_amrap_title
    WorkoutMode.CUSTOM -> R.string.mode_custom_title
}

private fun WorkoutMode.iconRes(): Int = when (this) {
    WorkoutMode.EMOM -> R.drawable.ic_activity
    WorkoutMode.TABATA -> R.drawable.ic_rotate_cw
    WorkoutMode.AMRAP -> R.drawable.ic_zap
    WorkoutMode.CUSTOM -> R.drawable.ic_rotate_cw
}

private val MAX_CONTENT_WIDTH = 640.dp

/**
 * Builds the run timeline preview.
 *
 * No longer branches on mode: the plan already knows the shape of every workout, and
 * the caller has checked [WorkoutSetupUiState.isValid], which is what makes building
 * a request here safe.
 */
private fun WorkoutSetupUiState.timelineSegments() =
    toRequest().toPlan().toRunSegments(PREPARE_COUNTDOWN_MILLIS)

@Composable
private fun CountPicker(
    label: String,
    count: Int,
    onCountChange: (Int) -> Unit,
    compact: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = if (compact) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            WheelPicker(
                count = 99,
                selected = count - 1,
                onSelect = { onCountChange(it + 1) },
                formatter = { "%02d".format(it + 1) },
                itemHeight = if (compact) 32.dp else 44.dp,
                pickerWidth = if (compact) 54.dp else 80.dp,
                contentDescription = stringResource(R.string.setup_sets),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.setup_sets_unit),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
