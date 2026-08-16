package dev.danielkindl.ocho.ui.settings

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasStateDescription
import dev.danielkindl.ocho.domain.model.UserSettings
import dev.danielkindl.ocho.ui.components.WheelPicker
import dev.danielkindl.ocho.ui.theme.OchoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Instrumented checks for the Settings actions and picker semantics users rely on. */
class SettingsAccessibilityTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsSwitchesExposeLabelsAndInvokeCallbacks() {
        var sound: Boolean? = null
        var vibration: Boolean? = null
        var countdown: Boolean? = null

        setSettings(
            onSoundEnabled = { sound = it },
            onVibrationEnabled = { vibration = it },
            onCountdownBeepsEnabled = { countdown = it },
        )

        composeRule.onNodeWithContentDescription("Sound")
            .assertIsToggleable()
            .performClick()
        composeRule.onNodeWithContentDescription("Vibration")
            .assertIsToggleable()
            .performClick()
        composeRule.onNodeWithContentDescription("Countdown beeps")
            .assertIsToggleable()
            .performClick()

        assertEquals(false, sound)
        assertEquals(false, vibration)
        assertEquals(false, countdown)
    }

    @Test
    fun countdownBeepSwitchIsDisabledWhenSoundIsOff() {
        setSettings(settings = UserSettings(soundEnabled = false))

        composeRule.onNodeWithContentDescription("Countdown beeps")
            .assertIsNotEnabled()
    }

    @Test
    fun blockedNotificationsExposeAllowAction() {
        var notificationActionCount = 0
        setSettings(
            notificationsAllowed = false,
            onOpenNotifications = { notificationActionCount++ },
        )

        composeRule.onNodeWithText("Allow")
            .assert(hasClickAction())
            .performClick()
        assertEquals(1, notificationActionCount)
    }

    @Test
    fun allowedNotificationsExposeAllowedState() {
        setSettings(notificationsAllowed = true)
        composeRule.onNodeWithText("Allowed").assertExists()
    }

    @Test
    fun aboutRowsAreTappableActions() {
        val actions = mutableListOf<String>()
        setSettings(
            onOpenFeedback = { actions += "feedback" },
            onOpenLicenses = { actions += "licences" },
            onOpenPrivacyPolicy = { actions += "privacy" },
        )

        composeRule.onNodeWithText("Feedback").performClick()
        composeRule.onNodeWithText("Licences").performClick()
        composeRule.onNodeWithText("Privacy Policy").performClick()

        assertEquals(listOf("feedback", "licences", "privacy"), actions)
    }

    @Test
    fun wheelPickerExposesItsLabelAndSelectedValue() {
        composeRule.setContent {
            OchoTheme {
                WheelPicker(
                    count = 60,
                    selected = 12,
                    onSelect = {},
                    contentDescription = "Work seconds",
                )
            }
        }

        composeRule.onNodeWithContentDescription("Work seconds")
            .assert(hasStateDescription("12"))
    }

    private fun setSettings(
        settings: UserSettings = UserSettings(),
        notificationsAllowed: Boolean = false,
        onNavigateUp: () -> Unit = {},
        onOpenLicenses: () -> Unit = {},
        onOpenFeedback: () -> Unit = {},
        onOpenNotifications: () -> Unit = {},
        onOpenPrivacyPolicy: () -> Unit = {},
        onSoundEnabled: (Boolean) -> Unit = {},
        onVibrationEnabled: (Boolean) -> Unit = {},
        onCountdownBeepsEnabled: (Boolean) -> Unit = {},
    ) {
        composeRule.setContent {
            OchoTheme {
                SettingsContent(
                    settings = settings,
                    notificationsAllowed = notificationsAllowed,
                    versionName = "test",
                    actions = SettingsContentActions(
                        onNavigateUp = onNavigateUp,
                        onOpenLicenses = onOpenLicenses,
                        onOpenFeedback = onOpenFeedback,
                        onOpenNotifications = onOpenNotifications,
                        onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                        onSoundEnabled = onSoundEnabled,
                        onVibrationEnabled = onVibrationEnabled,
                        onCountdownBeepsEnabled = onCountdownBeepsEnabled,
                        updateSection = {},
                    ),
                )
            }
        }
    }
}
