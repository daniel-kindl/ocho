# Ocho privacy policy

Last updated: 2026-08-12

Ocho is an offline-first workout timer. This policy describes the Android application
variants built from this repository. No personal-data collection, analytics, crash
reporting, advertising, account system, or cloud workout synchronisation is included
in the verified source code.

## Data stored on the device

Workout settings and saved workout presets are stored locally in Android DataStore
preferences. They remain on the device and are not uploaded by Ocho. Removing the
app removes its app-private stored data according to the device's normal Android
uninstall behavior.

## Permissions

Both variants use the permissions needed for workout timing:

- `VIBRATE` provides interval and completion feedback.
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` keep an active timer
  running reliably in the background and with the screen off.
- `WAKE_LOCK` prevents the CPU from sleeping during an active timer.
- `POST_NOTIFICATIONS` allows the ongoing workout notification on Android 13 and
  newer. The workout can still run if notification access is denied.

The Play variant has no network or package-install permission. It does not contain
the GitHub updater.

The GitHub variant additionally uses:

- `INTERNET` to check the public GitHub Releases API and download the APK offered by
  a GitHub release.
- `REQUEST_INSTALL_PACKAGES` so the user can choose to install a downloaded APK.
  Android still requires the user to grant this app permission to install unknown
  apps. Ocho does not silently install an update.

The GitHub build also includes an app-private `FileProvider` and an install-result
receiver used only to hand a downloaded APK to Android's package installer and report
the result.

## Network requests

The GitHub stable build checks:

`https://api.github.com/repos/daniel-kindl/ocho/releases/latest`

The GitHub development build checks the public release list and selects the newest
eligible prerelease. The selected release's public APK URL is downloaded through
Android's system `DownloadManager`. Requests are made only for the updater flow and
the launch-time check in the GitHub variant. The Play variant makes no Ocho network
requests.

Ocho does not send workout data, settings, identifiers, advertising IDs, contacts,
location, or health data to these endpoints.

## Third parties and licenses

Ocho's own source is licensed under GNU GPLv3. Bundled fonts, icons, and libraries
retain their separate licenses. See the in-app Settings → Licences screen,
[LICENSE](../LICENSE), and [THIRD-PARTY-NOTICES.md](../THIRD-PARTY-NOTICES.md).

## Contact

The repository maintainer's verified support channel is the [Ocho issue tracker](https://github.com/daniel-kindl/ocho/issues).
No separate legal publisher identity or postal address is asserted here; the
maintainer should replace this placeholder with the final legal publisher/contact
details before a store submission if required.
