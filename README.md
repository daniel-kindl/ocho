# Ocho

Ocho is an Android interval timer for EMOM, Tabata, AMRAP, and custom interval workouts.
It uses a large timer display, clear phase colours, sound signals, and vibration signals.

[![Dev CI](https://github.com/daniel-kindl/ocho/actions/workflows/dev-ci.yml/badge.svg?branch=dev)](https://github.com/daniel-kindl/ocho/actions/workflows/dev-ci.yml)
[![Release](https://github.com/daniel-kindl/ocho/actions/workflows/release.yml/badge.svg)](https://github.com/daniel-kindl/ocho/actions/workflows/release.yml)
[![Latest release](https://img.shields.io/github/v/release/daniel-kindl/ocho?label=latest)](https://github.com/daniel-kindl/ocho/releases/latest)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue)](LICENSE)
[![API 26+](https://img.shields.io/badge/API-26%2B-brightgreen)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)

<p>
  <a href="https://play.google.com/store/apps/details?id=dev.danielkindl.ocho"><img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="60"></a>
  <a href="https://github.com/daniel-kindl/ocho/releases/latest"><img src="website/public/badges/github-apk.svg" alt="Download APK from GitHub" height="60"></a>
</p>

Project site: [daniel-kindl.github.io/ocho](https://daniel-kindl.github.io/ocho/)

The word **ocho** means **eight** in Spanish. It is also the name of a figure-eight step in tango.
A classic Tabata workout also uses eight rounds.

---

## Screenshots

<p align="center">
  <img src="website/public/screenshots/mockups/home.png" width="24%" alt="Home screen with cards for EMOM, Tabata, AMRAP, and Custom Timer inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/tabata-setup.png" width="24%" alt="Tabata setup with 20 rounds, 45 seconds of work, and 15 seconds of rest inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/tabata-work.png" width="24%" alt="Tabata work phase that shows the current interval inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/tabata-rest.png" width="24%" alt="Tabata rest phase that shows the recovery interval inside a Pixel 9a mockup.">
</p>

<p align="center">
  <img src="website/public/screenshots/mockups/emom-setup.png" width="24%" alt="EMOM setup screen inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/prepare.png" width="24%" alt="Prepare countdown before the first interval inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/emom-paused.png" width="24%" alt="Paused EMOM session inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/finish.png" width="24%" alt="Completed workout summary inside a Pixel 9a mockup.">
</p>

<p align="center">
  <img src="website/public/screenshots/mockups/custom-setup.png" width="24%" alt="Custom Timer setup with sets, work, and rest controls inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/emom-presets.png" width="24%" alt="EMOM setup with a saved preset row and timing summary inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/amrap-presets.png" width="24%" alt="AMRAP setup with three saved presets named Quick Start, Steady Pace, and Long Burn inside a Pixel 9a mockup.">
</p>

The screenshots show the main workout flow. They include mode selection, setup, the prepare countdown,
work and rest phases, pause, presets, and the completed-workout summary.

The workout timeline above the Start button shows the sequence of the workout before it starts.
Saved preset rows show the preset name and the applicable timing values.

During a workout, the timer and the current phase use most of the screen.
Work and rest phases use different colours and different brightness levels.

---

## Features

| Feature | Description |
|---------|-------------|
| EMOM timer | Set the total duration and interval duration. |
| Tabata timer | Set the total duration, work duration, and rest duration. Ocho changes phases automatically. |
| AMRAP timer | Set one total duration. Ocho does not give interval signals during the workout. |
| Custom Timer | Set the number of work sets, work duration, and rest duration. Ocho does not add a rest phase after the final work set. |
| Phase colours | Each phase uses a full-screen colour. Work and rest also use different brightness levels. |
| Workout timeline | See the sequence and relative duration of workout phases before you start. |
| Sound signals | Ocho gives different sound signals for workout events. Signals use the Android alarm audio stream. |
| Vibration signals | Ocho uses different vibration patterns for interval changes and workout completion. |
| Pause and resume | Pause a workout and resume it without changing the interval timing. |
| Prepare countdown | Ocho gives a three-second countdown before the first interval. |
| Presets | Save, load, name, and delete configurations for each timer mode. |
| Progress and summary | See workout progress during the session and a summary after completion. |
| Exit confirmation | Ocho asks for confirmation before it ends an active workout. |
| Updates | The GitHub version can check GitHub Releases for updates. The Google Play version uses Google Play for updates. |
| Workout display | Ocho uses large text, high contrast, and a screen-on mode during workouts. |

---

## Install

Ocho is available from [Google Play](https://play.google.com/store/apps/details?id=dev.danielkindl.ocho)
and from [GitHub Releases](https://github.com/daniel-kindl/ocho/releases/latest).

### Google Play version

Install the Google Play version from Google Play.
Google Play manages updates for this version.
The Google Play version does not contain the Ocho GitHub updater or APK installer.

### GitHub version

Download the APK from the latest GitHub release and install it manually.
Android can ask you to allow installation from an unknown source.

The GitHub version contains the Ocho update function.
The updater accepts only the official Ocho release asset.
It checks the APK package identity before it sends the APK to Android.
Android then verifies the application signing key before installation.

### Update channels

| Channel | Application name | Source | Publication |
|---------|------------------|--------|-------------|
| Stable | `Ocho` | Latest GitHub release | Each tagged release from `main` |
| Dev | `Ocho Dev` | Latest prerelease | Each push to `dev` |

The stable version and the dev version have different `applicationId` values and separate application data.
You can install both versions on the same device.
One version does not offer updates for the other version.

Use the dev version only to test changes before they reach `main`.
The dev version can contain incomplete or unstable changes.

---

## Usage

### EMOM

Set the total duration and interval duration. Then select **Start**.
Ocho gives a sound signal and a vibration signal at each interval boundary.
Select **Pause** to pause the workout. Select **Stop** to end the workout before completion.

### Tabata

Set the total duration, work duration, and rest duration. Then select **Start**.
Ocho changes between work and rest phases automatically.
Work and rest use different colours and sound signals.

### AMRAP

Set the total duration. Then select **Start**.
Ocho does not give interval signals during the workout.
A countdown occurs before the workout ends.
Record your rounds separately.

### Custom Timer

Set the number of work sets, work duration, and rest duration.
Rest occurs only between work sets.
The workout ends immediately after the final work set.

### Presets

Select **Save** in the Presets section to save the current configuration.
Ocho fills the name field from the current configuration. You can change the name before you save it.
Preset names can contain a maximum of 50 Unicode characters.

Select a preset row to load it.
Use the delete control at the end of the row to delete the preset.

### Settings

Use Settings to enable or disable sound and vibration.
Settings also contains feedback, licence, and privacy-policy links.

The GitHub version shows update controls in Settings.
The Google Play version does not show these controls.

---

## Build from source

Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for build requirements, Gradle commands,
release signing, the package layout, and the timer architecture.

Read [docs/PUBLISHING.md](docs/PUBLISHING.md) for publication procedures and GPLv3 requirements.
Read [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md) for the privacy policy.
Read [docs/TESTING.md](docs/TESTING.md) for local and CI test procedures.

Use these commands for the main local verification tasks:

```powershell
./gradlew.bat :app:testGithubDebugUnitTest :app:testPlayDebugUnitTest
./gradlew.bat :app:compileGithubDebugAndroidTestKotlin :app:compilePlayDebugAndroidTestKotlin
```

Local instrumentation tests and manual UI checks use the configured `Pixel_9a` Android 17 (API 37) emulator.
Start the emulator before you run connected tests. Wait until ADB reports that the device is ready.

Ocho currently provides English text only.
Android application strings and plural rules are in Android resources.
Website text is in `website/src/i18n/en.ts`.
Read [docs/LOCALIZATION.md](docs/LOCALIZATION.md) before you add a locale.

---

## Website

The public website is an Astro project in `website/`.

To run the website locally, use:

```bash
cd website
npm ci
npm run dev
```

Use `npm run build` to create the static production output.
Pushes to `main` deploy `website/dist/` with the GitHub Pages workflow.
Pull requests that change the website build and verify it, but they do not deploy it.

---

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) for branch rules, commit conventions, the release process,
and the [contributor terms](CONTRIBUTING.md#contributor-terms).

Read [SECURITY.md](SECURITY.md) for the security policy.

---

## License

Ocho is free software under the [GNU General Public License v3.0](LICENSE).
You can use, study, modify, and redistribute it under the terms of that licence.
If you redistribute Ocho, you must also comply with the GPLv3 source-code requirements.

Copyright © 2026 Daniel Kindl for his contributions.
Contributors retain copyright in their contributions. See [CONTRIBUTING.md](CONTRIBUTING.md).
Ocho can also be offered under separate commercial terms.

The name "Ocho", the wordmark, and the numeral-8 icon are not covered by the GPL.
If you distribute a fork, use a different name and different branding.

Bundled fonts and icons keep their own licences.
The bundled fonts are IBM Plex Sans, JetBrains Mono, and Space Grotesk under the SIL OFL 1.1.
Lucide icons use the ISC licence.
Read [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) for the applicable third-party licence texts.
The same licence texts are available in the application under **Settings > Licences**.
