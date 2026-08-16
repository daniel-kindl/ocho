# Local testing

The local Android emulator is available for end-to-end testing.

- AVD: `Pixel_9a`
- ADB serial: `emulator-5554`
- Android: 17 (API 37)
- Display: 1080 × 2424 portrait
- Demo status-bar mode: enabled for clean screenshots

On the development machine, this AVD can take two minutes or more to finish
booting. Keep waiting until ADB reports `device` and `sys.boot_completed` is `1`;
an `offline` device during that window is expected.

Build and install the Play debug variant from the repository root:

```powershell
./gradlew.bat :app:assemblePlayDebug
adb -s emulator-5554 install -r app/build/outputs/apk/play/debug/app-play-debug.apk
```

Keep the emulator running after a test session when follow-up manual testing is
needed.

## Layout regression pass

For layout checks, exercise the setup and Settings screens at the default text
size, at 1.3× font scale, in landscape, and with a short portrait viewport.
Verify that the scrolling body can reveal every picker label and the Presets
section above the fixed Start button, and that the Settings footer is fully
visible at the bottom. The delete control on a saved preset must retain a 48dp
touch target.

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
