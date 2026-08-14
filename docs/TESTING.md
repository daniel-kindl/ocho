# Local testing

The local Android emulator is available for end-to-end testing.

- AVD: `Pixel_9a`
- ADB serial: `emulator-5554`
- Android: 17 (API 37)
- Display: 1080 × 2424 portrait
- Demo status-bar mode: enabled for clean screenshots

Build and install the Play debug variant from the repository root:

```powershell
./gradlew.bat :app:assemblePlayDebug
adb -s emulator-5554 install -r app/build/outputs/apk/play/debug/app-play-debug.apk
```

Keep the emulator running after a test session when follow-up manual testing is
needed.
