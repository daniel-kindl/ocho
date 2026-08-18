package dev.danielkindl.ocho.ui.text

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.danielkindl.ocho.domain.model.WorkoutMode
import dev.danielkindl.ocho.domain.model.WorkoutPreset
import dev.danielkindl.ocho.ui.setup.DurationValue
import dev.danielkindl.ocho.ui.setup.WorkoutSetupUiState
import dev.danielkindl.ocho.ui.theme.OchoTheme
import org.junit.Rule
import org.junit.Test

/** Pins the English resource contract used by the setup and preset summaries. */
class LocalizedTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun duration_and_pattern_text_are_resolved_from_resources() {
        val state = WorkoutSetupUiState(
            mode = WorkoutMode.TABATA,
            totalMinutes = 4,
            workSeconds = 20,
            restSeconds = 10,
        )

        composeRule.setContent {
            OchoTheme {
                Text(state.patternText())
            }
        }

        composeRule.onNodeWithText("8 × (20s work / 10s rest)").assertExists()
    }

    @Test
    fun generated_preset_name_and_saved_summary_are_resolved_from_resources() {
        val state = WorkoutSetupUiState(
            mode = WorkoutMode.EMOM,
            totalMinutes = 20,
            totalSeconds = 30,
            intervalMinutes = 1,
            intervalSeconds = 5,
        )
        val preset = WorkoutPreset(
            id = "emom",
            name = "Morning",
            mode = WorkoutMode.EMOM,
            totalMinutes = 20,
            totalSeconds = 30,
            intervalMinutes = 1,
            intervalSeconds = 5,
        )

        composeRule.setContent {
            OchoTheme {
                Text(state.defaultPresetNameText())
                Text(preset.summaryText())
                Text(durationText(DurationValue(1, 5)))
            }
        }

        composeRule.onNodeWithText("20min 30s / 1min 5s").assertExists()
        composeRule.onNodeWithText("20min 30s total · every 1min 5s").assertExists()
        composeRule.onNodeWithText("1min 5s").assertExists()
    }
}
