package dev.danielkindl.ocho.ui.components

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import dev.danielkindl.ocho.domain.model.WorkoutMode
import dev.danielkindl.ocho.domain.model.WorkoutPreset
import dev.danielkindl.ocho.ui.theme.OchoTheme
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/** Regression checks for the preset controls' touch-target sizing. */
class PresetsAccessibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun savedPresetDeleteControlKeepsMinimumTouchTarget() {
        composeRule.setContent {
            OchoTheme {
                PresetsSection(
                    presets = listOf(
                        WorkoutPreset(
                            id = "demo",
                            name = "Demo",
                            mode = WorkoutMode.CUSTOM,
                            totalMinutes = 1,
                            totalSeconds = 0,
                        ),
                    ),
                    getKey = { it.id },
                    getLabel = { it.name },
                    onPresetClick = {},
                    onDeleteClick = {},
                    onSavePreset = {},
                    saveEnabled = true,
                    showEmptyMessage = false,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Delete Demo")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }
}
