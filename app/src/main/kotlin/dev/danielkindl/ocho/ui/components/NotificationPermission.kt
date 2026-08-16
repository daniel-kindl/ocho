package dev.danielkindl.ocho.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.danielkindl.ocho.R

internal const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"

/**
 * Shows the notification rationale at application startup, before the system prompt.
 *
 * The permission is useful before a workout starts: it keeps an active session visible
 * outside Ocho and exposes pause, resume, and stop controls from the notification shade.
 * Asking here also means starting a workout never interrupts its three-second prepare
 * countdown with an unrelated system dialog.
 *
 * **Denial is not an error.** The app continues normally and the timer remains exact;
 * the user simply does not get the ongoing notification. "Not now" dismisses this
 * startup screen for the current app session, so it cannot block the user from using
 * Ocho.
 *
 * No-op below Android 13, where the permission is granted at install time.
 */
@Composable
fun NotificationPermissionGate(
    skipInitialPrompt: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Granted or not, the app continues normally. */ }

    var showPermissionScreen by remember {
        mutableStateOf(
            !skipInitialPrompt && shouldRequestNotificationPermission(context),
        )
    }

    if (!showPermissionScreen) {
        content()
        return
    }

    NotificationPermissionScreen(
        onAllow = {
            launcher.launch(POST_NOTIFICATIONS_PERMISSION)
            showPermissionScreen = false
        },
        onNotNow = { showPermissionScreen = false },
    )
}

/** Runs the notification rationale as the second first-run setup step. */
@Composable
fun NotificationPermissionOnboardingStep(onComplete: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Granted or not, setup is complete. */ }

    var showPermissionScreen by remember {
        mutableStateOf(shouldRequestNotificationPermission(context))
    }

    if (!showPermissionScreen) {
        LaunchedEffect(Unit) { onComplete() }
        return
    }

    NotificationPermissionScreen(
        onAllow = {
            launcher.launch(POST_NOTIFICATIONS_PERMISSION)
            showPermissionScreen = false
            onComplete()
        },
        onNotNow = {
            showPermissionScreen = false
            onComplete()
        },
        showProgress = true,
    )
}

private fun shouldRequestNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            POST_NOTIFICATIONS_PERMISSION,
        ) != PackageManager.PERMISSION_GRANTED
}

@Composable
private fun NotificationPermissionScreen(
    onAllow: () -> Unit,
    onNotNow: () -> Unit,
    showProgress: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.width(12.dp))
                HorizontalDivider(
                    modifier = Modifier.width(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.notification_permission_setup),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (showProgress) {
                SetupProgress(currentStep = 2)
            }

            Spacer(Modifier.weight(1f))

            Column {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.notification_permission_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.notification_permission_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onAllow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = stringResource(R.string.notification_permission_allow),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            TextButton(
                onClick = onNotNow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.notification_permission_not_now))
            }
            Text(
                text = stringResource(R.string.notification_permission_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
