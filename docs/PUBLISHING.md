# Publishing Ocho

This document describes the reproducible release variants and the GitHub Pages site.
The commands below are run from the repository root with JDK 17:

```bash
./gradlew check
./gradlew test
./gradlew lintPlayRelease
./gradlew lintGithubRelease
./gradlew bundlePlayRelease
./gradlew assembleGithubRelease
```

## Distribution variants

The `distribution` flavor dimension contains `play` and `github`. Both release
variants use application ID `dev.danielkindl.ocho`, version name `3.3.0`, and version
code 13 in this revision. The version code must increase for every published update;
the existing GitHub release is reported as version code 12.

The flavor/build-type application IDs are:

| Variant family | Application ID |
| --- | --- |
| `playDebug`, `githubDebug` | `dev.danielkindl.ocho.debug` |
| `playDev`, `githubDev` | `dev.danielkindl.ocho.dev` |
| `playRelease`, `githubRelease` | `dev.danielkindl.ocho` |

| Variant | Task | Artifact | Channel behavior |
| --- | --- | --- | --- |
| Play release | `bundlePlayRelease` | `app/build/outputs/bundle/playRelease/app-play-release.aab` | Google Play upload; no self-updater |
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

The Play manifest must not contain `REQUEST_INSTALL_PACKAGES`, `INTERNET`,
`FileProvider`, `InstallResultReceiver`, or updater metadata/components. The Play
Settings implementation is a no-op for the update section, and the Play source set
does not compile updater classes. The GitHub manifest intentionally contains
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

No new proprietary runtime dependency is used by the flavor split.

## Manual Google Play release

Google Play publishing is intentionally not automated. The maintainer must still:

1. Configure the Play listing and Play App Signing intentionally, resolving the key
   compatibility choice above.
2. Build and inspect `bundlePlayRelease` from the intended source revision.
3. Upload the AAB manually in Play Console and complete the required store/privacy
   declarations.
4. Ensure Google Play Automatic Protection, installer checks, anti-tamper, and similar
   restrictions are disabled.

No Play service account, Developer API credential, or Play upload action is needed or
present in this repository.

## GitHub Releases

The existing release workflow continues to publish the GitHub APK manually through a
draft GitHub Release. It validates the tag, runs the GitHub unit tests, signs
`assembleGithubRelease` with CI-provided secrets, computes a checksum, publishes
build provenance, and publishes the APK. It does not publish to Google Play.

Development builds use `assembleGithubDev -PdevBuildNumber=<run number>` and retain
the existing prerelease updater channel.

## GitHub Pages

The static site source lives in `docs/index.html`, `docs/styles.css`, and
`docs/privacy-policy.html`; screenshots are reused from `docs/screenshots/`. Preview
it locally with:

```bash
python3 -m http.server 8000 --directory docs
```

Then open `http://127.0.0.1:8000/`. The expected project Pages URL is:

`https://daniel-kindl.github.io/ocho/`

The `release.yml` workflow validates and deploys the `docs/` directory after the
tagged release has built and published successfully. It uses a separate, least-
privilege Pages job in the same release workflow, so the site changes only when a
release is made. In repository settings, the maintainer must enable Pages with
**Source: GitHub Actions** if it is not already enabled. The workflow cannot change
that repository setting. The Google Play CTA intentionally remains “Google Play
coming soon” until a real listing URL exists.
