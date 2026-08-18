package dev.danielkindl.ocho.data.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.widget.Toast
import dev.danielkindl.ocho.R

/** Reports the result of an APK install session to the user. */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION ->
                intent.confirmationIntent()?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let(context::startActivity)
            PackageInstaller.STATUS_SUCCESS ->
                showToast(context, context.getString(R.string.update_installed, context.getString(R.string.app_name)))
            else -> showToast(context, installFailureMessage(context, intent, status))
        }
    }

    private fun installFailureMessage(context: Context, intent: Intent, status: Int): String =
        intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)?.let {
            context.getString(R.string.update_install_failed, it)
        } ?: context.getString(R.string.update_install_failed, "status $status")

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @Suppress("DEPRECATION")
    private fun Intent.confirmationIntent(): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            getParcelableExtra(Intent.EXTRA_INTENT)
        }
}
