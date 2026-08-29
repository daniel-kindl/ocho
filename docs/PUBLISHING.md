# Publishing Ocho

This document describes the reproducible release variants and the GitHub Pages site.
The commands below are run from the repository root with JDK 17:

```bash
./gradlew check
./gradlew testGithubDebugUnitTest testPlayDebugUnitTest
./gradlew lintPlayRelease
./gradlew lintGithubRelease
./gradlew bundlePlayRelease
./gradlew assembleGithubRelease
```

## Distribution variants

The `distribution` flavor dimension contains `play` and `github`. Release version name
and version code are defined in `app/build.gradle.kts`; release tags must match the
version name and version codes must increase for every published update.

The flavor/build-type application IDs are:

| Variant family | Application ID |
| --- | --- |
| `playDebug`, `githubDebug` | `dev.danielkindl.ocho.debug` |
| `playDev`, `githubDev` | `dev.danielkindl.ocho.dev` |
| `playRelease`, `githubRelease` | `dev.danielkindl.ocho` |

| Variant | Task | Artifact | Channel behavior |
| --- | --- | --- | --- |
| Play release | `bundlePlayRelease` | `app/build/outputs/bundle/playRelease/app-play-release.aab` | Google Play upload; Play-managed flexible updater |
| GitHub release | `assembleGithubRelease` | `app/build/outputs/apk/github/release/app-github-release.apk` | Manual GitHub Release APK; retains self-updater |

The AAB is not directly installable. The GitHub artifact is an APK because GitHub
users install it manually.

The exact source revision producing an artifact is recorded with:

```bash
git rev-parse HEAD
git status --short
```

Release automation should include that revision in release notes or an accompanying
build record. The public repository contains the source sets and Gradle configuration
for both variants.

## Stable-branch provenance

The protected `main` branch is the stable release line. A stable tag is valid only
when its commit is descended from the current `origin/main`; the release workflow
fetches `main` and checks this with `git merge-base --is-ancestor` before setting up
Java, running Gradle, signing, or publishing anything. This prevents a tag created
on an unrelated branch from bypassing the stable review path.

The published `v3.7.0` tag is immutable and remains the historical release point.
Synchronising stable history means bringing that tag and its ancestors into `main`,
never moving or recreating the tag. The provenance guard is intentionally tested in
the workflow with both a descended commit (accepted) and an unrelated commit
(rejected).

## Signing and update compatibility

The stable GitHub app keeps the existing application ID and must continue to be
signed with the existing GitHub release key. CI reads `KEYSTORE_FILE`,
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`; the keystore and
`keystore.properties` are local/secret inputs and must never be committed.

For a first Play listing, configure Play App Signing deliberately. If Google uses a
different app-signing certificate from the existing GitHub release key, Android will
not allow a Play-installed app to be updated by the GitHub APK, or a GitHub-installed
app to be updated by the Play app; users would need to uninstall before changing
channels. If channel migration is required, the maintainer must either use compatible
signing material where Play permits it or make and announce a separate package-ID
decision before publishing. This change does not silently change the package ID or
signing strategy.

Do not distribute Play-signed artifacts through GitHub. Keep the GitHub release APK
and its updater pointed at the manually published GitHub release APKs.

## Manifest and artifact checks

Merged manifests are available after a release build at:

```text
app/build/intermediates/merged_manifest/playRelease/processPlayReleaseMainManifest/AndroidManifest.xml
app/build/intermediates/merged_manifest/githubRelease/processGithubReleaseMainManifest/AndroidManifest.xml
```

The Play manifest must not contain `REQUEST_INSTALL_PACKAGES`, the GitHub updater's
`FileProvider`, `InstallResultReceiver`, or GitHub updater metadata/components. The
Play source set contains only the Play In-App Updates runtime and its user-driven
flexible flow. The GitHub manifest intentionally contains
`INTERNET`, `REQUEST_INSTALL_PACKAGES`, `FileProvider`, and
`InstallResultReceiver`; its GitHub Settings implementation contains the existing
check/download/install flow.

Useful checks include:

```bash
grep -E 'REQUEST_INSTALL_PACKAGES|FileProvider|InstallResultReceiver|data.update|INTERNET' \
  app/build/intermediates/merged_manifest/playRelease/processPlayReleaseMainManifest/AndroidManifest.xml

apkanalyzer manifest permissions app/build/outputs/apk/github/release/app-github-release.apk
apksigner verify --verbose app/build/outputs/apk/github/release/app-github-release.apk
```

The Play grep should produce no output. Inspect the AAB with `bundletool` or unzip
its base module and inspect `manifest/AndroidManifest.xml`; the same permission and
component absence must hold there. The release builds are non-debuggable, use R8,
and use resource shrinking. Mapping files are produced under
`app/build/outputs/mapping/playRelease/` and `app/build/outputs/mapping/githubRelease/`
and should be retained privately with crash reports as appropriate.

No automatic protection, installer check, anti-tamper mechanism, or Play upload step
is configured. Normal AAB packaging, R8, resource shrinking, AAPT2, and Play App
Signing are compatible build/distribution tools and do not change Ocho's license.

## GPLv3 and third-party compliance

Both the Play AAB and GitHub APK are built from the publicly available GPLv3 source
in this repository. Ocho's own source remains under GNU GPLv3; `LICENSE`, copyright,
warranty, commercial-licensing/trademark wording, the in-app Settings → Licences
screen, and `THIRD-PARTY-NOTICES.md` remain part of the project. Third-party fonts,
icons, and libraries retain their separate licenses and are not relicensed as GPLv3.

The Play and GitHub builds are alternative distributions of the same GPLv3 project.
Google build and packaging tools do not impose a proprietary license on Ocho. Do not
add a no-redistribution EULA or other restriction to either build. Google Play
Automatic Protection, installer checks, anti-tamper protection, and similar features
that prevent modification or redistribution must remain disabled for this GPL build.

The Play flavor uses Google's Play In-App Updates runtime; the GitHub flavor retains
its existing platform-only updater dependencies.

## Google Play release (approval required)

Play publishing is intentionally blocked in CI until the maintainer explicitly
authorizes enabling it. The release workflow may build and retain the Play AAB as
an artifact, but it does not upload anything to Google Play. The only Play upload
path is the explicit manual `workflow_dispatch` candidate described below.

When Play publishing is authorized, the maintainer must first:

1. Configure the Play listing and Play App Signing intentionally, resolving the key
   compatibility choice above.
2. Complete the required store, privacy, and app-content declarations.
3. Upload the first `bundlePlayRelease` manually, preferably to internal testing, so
   the Play app and its initial release are established.
4. Ensure Google Play Automatic Protection, installer checks, anti-tamper, and similar
   restrictions remain disabled.

While production access is pending, run **Dev CI** manually from the `dev` branch
in the GitHub Actions UI and enter the exact API track name of the Closed-testing
track. This is the only workflow that can upload to Play, and it requires that
explicit manual dispatch. It uploads a completed Closed-testing candidate and
retains the matching APK as a GitHub Actions artifact; it does not publish to the
production track.

After production access is granted, promote the tested Closed-testing release to
Production in Play Console. A future production-upload workflow must be enabled
explicitly before CI can upload a production draft.

## GitHub Releases

The existing release workflow continues to publish the GitHub APK manually through a
draft GitHub Release. It validates the tag, runs both flavor unit-test suites, signs
`assembleGithubRelease` with CI-provided secrets, computes a checksum, publishes
build provenance, and publishes the APK. It does not publish to Google Play.

The ordinary push-based `dev` CI continues to publish the separate GitHub
development APK channel using `assembleGithubDev -PdevBuildNumber=<run number>`.
The manual Play candidate path uses the stable-package `playRelease` and
`githubRelease` variants so their versions match the production artifacts.

## GitHub Pages

The static site is an Astro project under `website/`. Preview it locally with:

```bash
cd website
npm ci
npm run dev
```

The production build can be generated with:

```bash
npm run build
```

Then open the development URL shown by Astro. The expected project Pages URL is:

`https://daniel-kindl.github.io/ocho/`

The `pages.yml` workflow validates website changes in pull requests and builds and
deploys `website/dist/` after every push to `main`. The Android release workflow does
not deploy the website. In
repository settings, the maintainer must enable Pages with **Source: GitHub Actions**
if it is not already enabled. The workflow cannot change that repository setting.
The Google Play CTA intentionally remains “Google Play coming soon” until a real
listing URL exists.
