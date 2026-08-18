package dev.danielkindl.ocho.ui.components

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.danielkindl.ocho.domain.model.WorkoutMode
import dev.danielkindl.ocho.domain.model.WorkoutPreset
import dev.danielkindl.ocho.ui.theme.OchoTheme
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

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
                    getLabel = { it.name },
                    getSummary = { "1min total · every 20s" },
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

    @Test
    fun presetCardShowsSummaryAndLoadsFromItsWholeSurface() {
        var clicked = false
        composeRule.setContent {
            OchoTheme {
                PresetsSection(
                    presets = listOf(
                        WorkoutPreset(
                            id = "demo",
                            name = "Demo",
                            mode = WorkoutMode.EMOM,
                            totalMinutes = 1,
                            totalSeconds = 0,
                        ),
                    ),
                    getLabel = { it.name },
                    getSummary = { "1min total · every 20s" },
                    onPresetClick = { clicked = true },
                    onDeleteClick = {},
                    onSavePreset = {},
                    saveEnabled = true,
                )
            }
        }

        composeRule.onNodeWithText("1min total · every 20s").assertExists()
        composeRule.onNodeWithContentDescription("Load preset Demo").performClick()

        assertTrue(clicked)
    }

    @Test
    fun emptyStateExplainsHowToCreateTheFirstPreset() {
        composeRule.setContent {
            OchoTheme {
                PresetsSection(
                    presets = emptyList<String>(),
                    getLabel = { it },
                    getSummary = { it },
                    onPresetClick = {},
                    onDeleteClick = {},
                    onSavePreset = {},
                    saveEnabled = true,
                )
            }
        }

        composeRule.onNodeWithText(
            "No saved presets yet. Save this workout to use it again.",
        ).assertExists()
    }
}
