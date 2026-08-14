package dev.danielkindl.ocho.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.danielkindl.ocho.R
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(painterResource(R.drawable.ic_arrow_left), contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ListItem(
                leadingContent = {
                    Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null)
                },
                headlineContent = { Text("Sound") },
                supportingContent = {
                    Text(
                        "Play a beep at each timer event (uses alarm audio stream, ignores silent mode)",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = viewModel::setSoundEnabled,
                    )
                },
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(painterResource(R.drawable.ic_zap), contentDescription = null)
                },
                headlineContent = { Text("Vibration") },
                supportingContent = {
                    Text(
                        "Vibrate at each timer event and on workout completion",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = viewModel::setVibrationEnabled,
                    )
                },
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(painterResource(R.drawable.ic_rotate_cw), contentDescription = null)
                },
                headlineContent = { Text("Countdown beeps") },
                supportingContent = {
                    Text(
                        "Tick down the last three seconds before each interval. " +
                            "Turned off by the sound switch above.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = settings.countdownBeepsEnabled,
                        onCheckedChange = viewModel::setCountdownBeepsEnabled,
                        // Sound is the parent switch: with it off there is nothing to
                        // count down with, so offering the choice would be a lie.
                        enabled = settings.soundEnabled,
                    )
                },
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(Icons.Outlined.Notifications, contentDescription = null)
                },
                headlineContent = {
                    Text(stringResource(R.string.settings_notifications))
                },
                supportingContent = {
                    Text(
                        stringResource(
                            if (notificationsAllowed) {
                                R.string.settings_notifications_allowed
                            } else {
                                R.string.settings_notifications_blocked
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Text(
                        stringResource(
                            if (notificationsAllowed) {
                                R.string.settings_notifications_status_allowed
                            } else {
                                R.string.settings_notifications_status_blocked
                            }
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.clickable { openNotificationSettings(context) },
            )
            HorizontalDivider()
            DistributionUpdateSection()
            HorizontalDivider()

            // Required, not decorative: the bundled fonts and icons are licensed on
            // condition that their notices travel with every copy of the software,
            // and the APK is the copy users receive.
            ListItem(
                headlineContent = { Text("Licences") },
                supportingContent = {
                    Text(
                        "Open-source components used in Ocho",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                modifier = Modifier.clickable(onClick = onOpenLicenses),
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Privacy Policy") },
                supportingContent = {
                    Text(
                        "How Ocho handles settings, workouts, and network access",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                modifier = Modifier.clickable {
                    context.startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            PRIVACY_POLICY_URI.toUri(),
                        )
                    )
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            // The wordmark is set in type, not drawn: "ocho", always lowercase, with
            // only the final o in brand green so it reads as the timer dial. Two
            // pre-rendered variants rather than live text, so the exact tracking and
            // the green-o treatment survive font fallback.
            Image(
                painter = painterResource(
                    if (isSystemInDarkTheme()) R.drawable.wordmark_dark else R.drawable.wordmark_light
                ),
                contentDescription = "Ocho",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(40.dp)
                    .padding(bottom = 8.dp),
            )

            // A LinkAnnotation rather than the old ClickableText plus manual offset
            // lookup. The framework now owns hit testing, which also means the link
            // is exposed to accessibility services as a link instead of as plain text
            // that happens to respond to taps.
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
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp),
            )
            Text(
                text = "v$versionName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp),
            )
        }
    }
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
