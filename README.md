# Ocho

Ocho is an Android workout interval timer for EMOM, Tabata, AMRAP, and Custom Timer workouts.
The app uses a large clock, clear phase colours, sound, and vibration so that you can follow a workout without continuous screen interaction.

[![Dev CI](https://github.com/daniel-kindl/ocho/actions/workflows/dev-ci.yml/badge.svg?branch=dev)](https://github.com/daniel-kindl/ocho/actions/workflows/dev-ci.yml)
[![Release](https://github.com/daniel-kindl/ocho/actions/workflows/release.yml/badge.svg)](https://github.com/daniel-kindl/ocho/actions/workflows/release.yml)
[![Latest release](https://img.shields.io/github/v/release/daniel-kindl/ocho?label=latest)](https://github.com/daniel-kindl/ocho/releases/latest)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue)](LICENSE)
[![API 26+](https://img.shields.io/badge/API-26%2B-brightgreen)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)

<p>
  <a href="https://play.google.com/store/apps/details?id=dev.danielkindl.ocho"><img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="60"></a>
  <a href="https://github.com/daniel-kindl/ocho/releases/latest"><img src="website/public/badges/github-apk.svg" alt="Download the Ocho APK from GitHub" height="60"></a>
</p>

Project site: [daniel-kindl.github.io/ocho](https://daniel-kindl.github.io/ocho/)

The name **Ocho** has two references. An *ocho* is a figure-eight step in tango. *Ocho* is also Spanish for **eight**, which is the number of rounds in a standard Tabata workout.

---

## Screenshots

<p align="center">
  <img src="website/public/screenshots/mockups/home.png" width="24%" alt="Home screen with cards for EMOM, Tabata, AMRAP and Custom Timer inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/tabata-setup.png" width="24%" alt="Tabata setup with 20 rounds, 45 seconds of work and 15 seconds of rest inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/tabata-work.png" width="24%" alt="Tabata work phase showing the current interval inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/tabata-rest.png" width="24%" alt="Tabata rest phase showing the recovery interval inside a Pixel 9a mockup.">
</p>

<p align="center">
  <img src="website/public/screenshots/mockups/emom-setup.png" width="24%" alt="EMOM setup screen inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/prepare.png" width="24%" alt="Prepare countdown before the first interval inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/emom-paused.png" width="24%" alt="Paused EMOM session inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/finish.png" width="24%" alt="Completed workout summary inside a Pixel 9a mockup.">
</p>

<p align="center">
  <img src="website/public/screenshots/mockups/custom-setup.png" width="24%" alt="Custom Timer setup with centered sets, work, and rest controls inside a Pixel 9a mockup.">
  <img src="website/public/screenshots/mockups/emom-presets.png" width="24%" alt="EMOM setup with a compact saved preset row, summary, and delete action inside a Pixel 9a phone mockup.">
  <img src="website/public/screenshots/mockups/amrap-presets.png" width="24%" alt="AMRAP setup with three distinct compact saved presets: Quick Start, Steady Pace, and Long Burn, inside a Pixel 9a phone mockup.">
</p>

The screenshots show the complete workout flow. They include mode selection, timer configuration, preparation, work, pause, rest, and completion. The bar above the Start control previews the workout structure before the workout starts.

The preset screenshots show the compact two-line preset layout. The AMRAP example contains three saved workouts with different durations so that the values are easy to compare.

During a workout, the current phase uses the complete screen. Work and rest use different colours and different lightness values.

---

## Features

| Feature | Detail |
|---------|--------|
| EMOM timer | Set the total duration and interval duration. |
| Tabata timer | Set the total duration, work duration, and rest duration. The app changes phases automatically. |
| AMRAP timer | Set the total duration. The timer runs as one continuous block. |
| Custom Timer | Set the number of work sets, work duration, and rest duration. There is no rest phase after the final work set. |
| Phase colours | Each phase uses a full-screen colour. The colours also use different lightness values. |
| Run timeline | Preview the relative duration and order of workout phases before the workout starts. |
| Sound feedback | Use a different sound for each event. Sounds use the Android alarm audio stream. |
| Vibration feedback | Use different vibration patterns for interval changes and workout completion. |
| Pause and resume | Pause a workout and continue from the same timer state. |
| Pre-start countdown | Use a three-second countdown before the first interval. |
| Presets | Save, load, name, and delete configurations for each timer mode. |
| Progress and summary | Show workout progress and a completion summary. |
| Exit confirmation | Ask for confirmation before Stop or the Back gesture ends an active workout. |
| Updates | The GitHub version can check GitHub Releases for updates. The Google Play version uses Google Play updates. |
| Workout display | Use a large, high-contrast display and keep the screen on during a workout. |

---

## Install

Ocho has two distribution methods. Choose one method.

### Google Play

Install Ocho from [Google Play](https://play.google.com/store/apps/details?id=dev.danielkindl.ocho).
Google Play installs and updates this version. The Google Play version does not contain the Ocho GitHub updater or the APK installation function.

### GitHub APK

Download the [latest GitHub release](https://github.com/daniel-kindl/ocho/releases/latest).
Install the APK manually. Android can require permission to install an app from an unknown source.

The GitHub version includes the Ocho GitHub updater. The updater accepts only the official Ocho release asset. It checks the package identity before it gives the APK to Android. Android verifies the signing key before installation.

### Update channels

| Channel | Application name | Source | Publication |
|---------|------------------|--------|-------------|
| Stable | `Ocho` | `releases/latest` | Each tagged release from `main` |
| Dev | `Ocho Dev` | Latest prerelease | Each push to `dev` |

The stable and dev versions use different `applicationId` values and different application data. You can install both versions on one device. One version does not offer updates for the other version.

Use the dev version only for testing. It can contain incomplete or unstable changes.

---

## Usage

### EMOM

Set the total duration and interval duration. Select Start. Ocho gives a sound and vibration cue at each interval boundary.

Select Pause to stop the timer temporarily. Select Stop to end the workout. Ocho asks for confirmation before it ends an active workout.

### Tabata

Set the total duration, work duration, and rest duration. Ocho changes between work and rest automatically. Different sounds and screen colours identify the two phases.

### AMRAP

Set the total duration. Select Start. The timer runs as one continuous block and does not give interval cues. A three-second countdown occurs before the timer ends.

Count completed rounds separately.

### Custom Timer

Set the number of work sets, work duration, and rest duration. Ocho runs a rest phase between work sets. It does not run a rest phase after the final work set.

### Presets

Select Save in the Presets section to store the current timer configuration. Ocho provides a default preset name from the configuration. You can edit this name.

A preset name can contain a maximum of 50 Unicode characters. The save dialog shows the current character count.

Select a preset row to load the preset. Select the delete control to remove the preset.

### Settings

Use Settings to enable or disable sound and vibration. Settings also contains feedback, licence, and privacy-policy links.

The Updates section is available only in the GitHub version.

---

## Build from source

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for build requirements, Gradle commands, release signing, package structure, and timer design.

See [docs/PUBLISHING.md](docs/PUBLISHING.md) for distribution variants and GPLv3 publication requirements. See [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md) for the privacy policy.

Run these commands to verify the Android project:

```powershell
./gradlew.bat :app:testGithubDebugUnitTest :app:testPlayDebugUnitTest
./gradlew.bat :app:compileGithubDebugAndroidTestKotlin :app:compilePlayDebugAndroidTestKotlin
```

See [docs/TESTING.md](docs/TESTING.md) for emulator tests and CI test procedures.

Local instrumentation tests use the configured `Pixel_9a` Android 17 (API 37) emulator. Start the emulator before you run a connected test task. Wait until ADB reports that the device is ready.

The project currently provides English only. Android application text is in Android resources. Website text is in `website/src/i18n/en.ts`. See [docs/LOCALIZATION.md](docs/LOCALIZATION.md) before you add a locale.

---

## Website

The website is an Astro project in `website/`.

Run the local development server:

```bash
cd website
npm ci
npm run dev
```

Create the production static output with `npm run build`.

A push to `main` deploys `website/dist/` to GitHub Pages. A pull request that changes the website builds and verifies the site but does not deploy it.

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch rules, commit conventions, the release process, and the [contributor terms](CONTRIBUTING.md#contributor-terms).

See [SECURITY.md](SECURITY.md) for the security policy.

---

## License

Ocho is free software under the [GNU General Public License v3.0](LICENSE). You can use, study, modify, and redistribute the software under the terms of this licence.

If you redistribute Ocho, you must comply with the GPLv3 requirements. See the licence for the complete requirements.

Copyright © 2026 Daniel Kindl for his contributions. Contributors retain copyright in their contributions. See [CONTRIBUTING.md](CONTRIBUTING.md).

Ocho can also be offered under separate commercial terms.

The name "Ocho", the wordmark, and the numeral-8 icon are not part of the GPL licence grant. If you create a fork for redistribution, use a different name and brand.

Bundled fonts use the SIL Open Font License 1.1. Lucide icons use the ISC licence. Other third-party libraries keep their applicable licences. See [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) and Settings > Licences for the complete notices.
