# Testing Ocho

## Automated checks

Run the two flavor-specific JVM suites from the repository root:

```powershell
./gradlew.bat :app:testGithubDebugUnitTest :app:testPlayDebugUnitTest
```

The GitHub suite covers release parsing, strict asset validation, updater persistence,
download policy, installer package identity, and the GitHub update ViewModel. The Play
suite runs the shared application logic against the Play variant and intentionally has
no Play update runtime or updater-specific tests.

Compile the shared Compose instrumentation tests for both variants with:

```powershell
./gradlew.bat :app:compileGithubDebugAndroidTestKotlin :app:compilePlayDebugAndroidTestKotlin
```

CI compiles both instrumentation variants, but does not boot an emulator or execute
instrumentation tests. Earlier hosted-emulator attempts had repeated startup failures,
followed by an instrumentation failure, so the emulator job is deliberately not a
release gate. Connected execution remains a local release-candidate check on the
documented AVD below.

## Local emulator

The configured local end-to-end test target is the `Pixel_9a` Android 17 (API 37)
emulator. Its usual ADB serial is `emulator-5554`; if Android assigns a different
serial, use the value reported by `adb devices -l` in the commands below.

- AVD: `Pixel_9a`
- ADB serial: `emulator-5554`
- Android: 17 (API 37)
- Display: 1080 × 2424 portrait
- Demo status-bar mode: enabled for clean screenshots

On the development machine, this AVD can take two minutes or more to finish
booting. Check its state with:

```powershell
adb devices -l
adb -s emulator-5554 shell getprop sys.boot_completed
```

Continue when ADB reports `device` and `sys.boot_completed` is `1`; an `offline`
device during startup is expected.

Build and install the Play debug variant from the repository root:

```powershell
./gradlew.bat :app:assemblePlayDebug
adb -s emulator-5554 install -r app/build/outputs/apk/play/debug/app-play-debug.apk
```

Run the instrumentation tests with the local emulator running:

```powershell
./gradlew.bat :app:connectedGithubDebugAndroidTest :app:connectedPlayDebugAndroidTest
```

Keep the emulator running after a test session when follow-up manual testing is
needed.

## Release-candidate manual checks

On the `Pixel_9a` AVD, run both connected tasks and then exercise each workout type:
EMOM, Tabata, AMRAP, and Custom Timer. For each type, check the setup summary,
start/prepare transition, work and rest transitions, final completion state, pause,
resume, and stop. Also check that timing continues with the screen off and while the
app is backgrounded, that foreground-service and notification state remain correct,
and that the wake lock is released after pause, stop, and completion.

Review Settings accessibility semantics in both flavors: the GitHub build exposes
the Updates group and the Play build does not. Check TalkBack labels, 48dp touch
targets, focus order, long text, 1.3× font scale, landscape, and the short portrait
viewport. Review the Play manifest/AAB and GitHub APK checks from
[docs/PUBLISHING.md](PUBLISHING.md), then repeat the same checks with the final
release signing configuration.

These checks are required before final release approval; they are not claims that
GitHub Actions executes an emulator test.

## Layout regression pass

For layout checks, exercise the setup and Settings screens at the default text
size, at 1.3× font scale, in landscape, and with a short portrait viewport.
Verify that the scrolling body can reveal every picker label and the Presets
section above the fixed Start button, and that the Settings footer is fully
visible at the bottom. Presets should remain compact two-line rows: the name may
ellipsize, but its mode-specific summary must remain readable. The trailing delete
control must retain a 48dp touch target. In the save dialog, long punctuation and
Unicode input must stop at 50 characters without breaking an emoji or other
surrogate pair.

The short-viewport check can be reproduced with:

```powershell
adb -s emulator-5554 shell wm size 1080x1800
adb -s emulator-5554 shell settings put system font_scale 1.3
```

Restore the emulator before handing it back:

```powershell
adb -s emulator-5554 shell wm size reset
adb -s emulator-5554 shell settings put system font_scale 1.0
adb -s emulator-5554 shell cmd window user-rotation lock 0
```
