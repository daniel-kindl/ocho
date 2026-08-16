package dev.danielkindl.ocho.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.danielkindl.ocho.R
import dev.danielkindl.ocho.domain.model.UserSettings
import dev.danielkindl.ocho.ui.components.POST_NOTIFICATIONS_PERMISSION

/**
 * Feedback toggles, distribution-specific update controls, and app information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onOpenLicenses: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var notificationsAllowed by remember {
        mutableStateOf(hasNotificationPermission(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                notificationsAllowed = hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    SettingsContent(
        settings = settings,
        notificationsAllowed = notificationsAllowed,
        versionName = versionName ?: "unknown",
        actions = SettingsContentActions(
            onNavigateUp = onNavigateUp,
            onOpenLicenses = onOpenLicenses,
            onOpenFeedback = { openFeedback(context, versionName ?: "unknown") },
            onOpenNotifications = { openNotificationSettings(context) },
            onOpenPrivacyPolicy = {
                context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URI.toUri()))
            },
            onSoundEnabled = viewModel::setSoundEnabled,
            onVibrationEnabled = viewModel::setVibrationEnabled,
            onCountdownBeepsEnabled = viewModel::setCountdownBeepsEnabled,
        ),
    )
}

/** User interactions supplied to the testable Settings content. */
@Suppress("LongParameterList")
class SettingsContentActions(
    /** Leaves the Settings destination. */
    val onNavigateUp: () -> Unit,
    /** Opens the bundled licence notices. */
    val onOpenLicenses: () -> Unit,
    /** Starts the feedback flow. */
    val onOpenFeedback: () -> Unit,
    /** Opens Android's notification access settings. */
    val onOpenNotifications: () -> Unit,
    /** Opens the published privacy policy. */
    val onOpenPrivacyPolicy: () -> Unit,
    /** Persists a new sound preference. */
    val onSoundEnabled: (Boolean) -> Unit,
    /** Persists a new vibration preference. */
    val onVibrationEnabled: (Boolean) -> Unit,
    /** Persists a new countdown-beep preference. */
    val onCountdownBeepsEnabled: (Boolean) -> Unit,
    /** Renders the distribution-specific update control. */
    val updateSection: @Composable () -> Unit = { DistributionUpdateSection() },
)

/**
 * Renders Settings with explicit actions so the screen remains usable with TalkBack
 * and can be exercised without constructing the Hilt navigation graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    settings: UserSettings,
    notificationsAllowed: Boolean,
    versionName: String,
    actions: SettingsContentActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = actions.onNavigateUp) {
                        Icon(painterResource(R.drawable.ic_arrow_left), contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = MAX_CONTENT_WIDTH)
                    .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            SettingsGroup(title = "Workout") {
                ListItem(
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null)
                    },
                    headlineContent = { Text("Sound") },
                    supportingContent = {
                        Text("Timer event beeps · alarm volume · ignores silent mode")
                    },
                    trailingContent = {
                        Switch(
                            checked = settings.soundEnabled,
                            onCheckedChange = actions.onSoundEnabled,
                            modifier = Modifier.semantics {
                                contentDescription = "Sound"
                            },
                        )
                    },
                )
                SettingsDivider()
                ListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.ic_zap), contentDescription = null)
                    },
                    headlineContent = { Text("Vibration") },
                    supportingContent = { Text("Timer event vibrations") },
                    trailingContent = {
                        Switch(
                            checked = settings.vibrationEnabled,
                            onCheckedChange = actions.onVibrationEnabled,
                            modifier = Modifier.semantics {
                                contentDescription = "Vibration"
                            },
                        )
                    },
                )
                SettingsDivider()
                ListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.ic_rotate_cw), contentDescription = null)
                    },
                    headlineContent = { Text("Countdown beeps") },
                    supportingContent = { Text("Last 3 seconds of each interval") },
                    trailingContent = {
                        Switch(
                            checked = settings.countdownBeepsEnabled,
                            onCheckedChange = actions.onCountdownBeepsEnabled,
                            enabled = settings.soundEnabled,
                            modifier = Modifier.semantics {
                                contentDescription = "Countdown beeps"
                            },
                        )
                    },
                )
            }

            SettingsGroup(title = "Notifications") {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Outlined.Notifications, contentDescription = null)
                    },
                    headlineContent = { Text(stringResource(R.string.settings_notifications)) },
                    supportingContent = {
                        Text(
                            stringResource(
                                if (notificationsAllowed) {
                                    R.string.settings_notifications_allowed
                                } else {
                                    R.string.settings_notifications_blocked
                                }
                            )
                        )
                    },
                    trailingContent = if (notificationsAllowed) {
                        {
                            Text(
                                stringResource(R.string.settings_notifications_status_allowed),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        {
                            TextButton(
                                onClick = actions.onOpenNotifications,
                                modifier = Modifier.semantics {
                                    contentDescription = "Allow notifications"
                                },
                            ) {
                                Text("Allow")
                            }
                        }
                    },
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (notificationsAllowed) {
                                "Manage notification access"
                            } else {
                                "Allow notifications"
                            },
                            onClick = actions.onOpenNotifications,
                        )
                        .semantics {
                            stateDescription = if (notificationsAllowed) "Allowed" else "Not allowed"
                        },
                )
            }

            SettingsGroup(title = "Updates") {
                actions.updateSection()
            }

            SettingsGroup(title = "About") {
                ListItem(
                    headlineContent = { Text("Feedback") },
                    supportingContent = { Text("Send feedback or report an issue") },
                    trailingContent = { SettingsChevron() },
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = "Open feedback",
                        onClick = actions.onOpenFeedback,
                    ),
                )
                SettingsDivider()
                // Required, not decorative: the bundled fonts and icons are licensed on
                // condition that their notices travel with every copy of the software.
                ListItem(
                    headlineContent = { Text("Licences") },
                    supportingContent = { Text("Open-source notices") },
                    trailingContent = { SettingsChevron() },
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = "Open licences",
                        onClick = actions.onOpenLicenses,
                    ),
                )
                SettingsDivider()
                ListItem(
                    headlineContent = { Text("Privacy Policy") },
                    supportingContent = { Text("How Ocho handles data") },
                    trailingContent = { SettingsChevron() },
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = "Open privacy policy",
                        onClick = actions.onOpenPrivacyPolicy,
                    ),
                )
            }

                SettingsFooter(versionName)
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SettingsFooter(versionName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(
                if (isSystemInDarkTheme()) R.drawable.wordmark_dark else R.drawable.wordmark_light
            ),
            contentDescription = "Ocho",
            modifier = Modifier.height(40.dp),
        )
        val authorLink = buildAnnotatedString {
            append("Made by ")
            withLink(
                LinkAnnotation.Url(
                    url = "https://daniel-kindl.github.io/",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    ),
                )
            ) { append("Daniel Kindl") }
        }
        Text(
            text = authorLink,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun SettingsChevron() {
    Icon(
        imageVector = Icons.Outlined.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            POST_NOTIFICATIONS_PERMISSION,
        ) == PackageManager.PERMISSION_GRANTED

private fun openNotificationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    )
}

private const val PRIVACY_POLICY_URI = "https://daniel-kindl.github.io/ocho/privacy-policy.html"
private const val FEEDBACK_EMAIL = "support@danielkindl.dev"
private const val FEEDBACK_ISSUES_URI = "https://github.com/daniel-kindl/ocho/issues"

private val MAX_CONTENT_WIDTH = 640.dp

private fun openFeedback(context: Context, versionName: String) {
    val emailIntent = Intent(
        Intent.ACTION_SENDTO,
        "mailto:$FEEDBACK_EMAIL".toUri(),
    ).apply {
        putExtra(Intent.EXTRA_SUBJECT, "Ocho feedback (v$versionName)")
        putExtra(
            Intent.EXTRA_TEXT,
            "Please describe your feedback or issue here.\n\nOcho version: $versionName",
        )
    }
    val target = if (emailIntent.resolveActivity(context.packageManager) != null) {
        emailIntent
    } else {
        Intent(Intent.ACTION_VIEW, FEEDBACK_ISSUES_URI.toUri())
    }
    context.startActivity(target)
}
