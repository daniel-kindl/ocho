package dev.danielkindl.ocho.ui.components

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import dev.danielkindl.ocho.domain.model.MAX_PRESET_NAME_LENGTH
import dev.danielkindl.ocho.ui.theme.OchoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Regression coverage for defensive preset-name input handling. */
class PresetSaveDeleteDialogsAccessibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun presetNameInputCapsLongPunctuationText() {
        var name = ""
        composeRule.setContent {
            OchoTheme {
                SavePresetDialog(
                    name = name,
                    onNameChange = { name = it },
                    onSave = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNode(hasSetTextAction())
            .performTextInput("!@#$%^&*()_+=-".repeat(8))

        composeRule.runOnIdle {
            assertEquals(
                MAX_PRESET_NAME_LENGTH,
                name.codePointCount(0, name.length),
            )
            assertEquals("!@#$%^&*()_+=-".repeat(3) + "!@#$%^&*", name)
        }
    }
}
