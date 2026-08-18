package dev.danielkindl.ocho.ui.setup

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import dev.danielkindl.ocho.domain.model.SessionRequest
import dev.danielkindl.ocho.domain.model.WorkoutMode
import dev.danielkindl.ocho.domain.model.WorkoutPreset
import dev.danielkindl.ocho.domain.repository.WorkoutPresetRepository
import dev.danielkindl.ocho.ui.theme.OchoTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Regression coverage for the extracted setup screen sections and actions. */
class WorkoutSetupAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emom_mode_exposes_its_picker_controls() {
        setMode(WorkoutMode.EMOM)
        onNode("Total").assertExists()
        onNode("Interval").assertExists()
    }

    @Test
    fun tabata_mode_exposes_its_picker_controls() {
        setMode(WorkoutMode.TABATA)
        onNode("Total duration").assertExists()
        onNode("Work").assertExists()
        onNode("Rest").assertExists()
    }

    @Test
    fun custom_mode_exposes_its_picker_controls() {
        setMode(WorkoutMode.CUSTOM)
        onNode("Sets").assertExists()
        onNode("Work").assertExists()
        onNode("Rest").assertExists()
    }

    @Test
    fun valid_configuration_enables_start_and_delivers_request() {
        var request: SessionRequest? = null
        setMode(WorkoutMode.EMOM) { request = it }

        composeRule.onNodeWithText("Start").assertIsEnabled().performClick()

        assertTrue(request is SessionRequest.Emom)
    }

    @Test
    fun invalid_configuration_disables_start() {
        val repository = FakeWorkoutPresetRepository()
        val viewModel = WorkoutSetupViewModel(
            repository,
            SavedStateHandle(mapOf("mode" to WorkoutMode.EMOM.name)),
        )
        viewModel.setTotalMinutes(0)
        viewModel.setTotalSeconds(0)

        composeRule.setContent {
            OchoTheme {
                WorkoutSetupScreen(onStartSession = {}, onNavigateUp = {}, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("Start").assertIsNotEnabled()
    }

    private fun setMode(
        mode: WorkoutMode,
        onStartSession: (SessionRequest) -> Unit = {},
    ) {
        val viewModel = WorkoutSetupViewModel(
            FakeWorkoutPresetRepository(),
            SavedStateHandle(mapOf("mode" to mode.name)),
        )
        composeRule.setContent {
            OchoTheme {
                WorkoutSetupScreen(
                    onStartSession = onStartSession,
                    onNavigateUp = {},
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun onNode(text: String) = composeRule.onNodeWithText(text)
}

private class FakeWorkoutPresetRepository : WorkoutPresetRepository {
    private val presets = MutableStateFlow<List<WorkoutPreset>>(emptyList())

    override fun getPresets(mode: WorkoutMode): Flow<List<WorkoutPreset>> = presets

    override suspend fun savePreset(preset: WorkoutPreset) {
        presets.value = presets.value.filterNot { it.id == preset.id } + preset
    }

    override suspend fun deletePreset(id: String) {
        presets.value = presets.value.filterNot { it.id == id }
    }
}
