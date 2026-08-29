# Google Play compliance checklist

This checklist is for the Play Console submission of `dev.danielkindl.ocho`. Re-run
it against the final signed `bundlePlayRelease` artifact immediately before a
submission. The GitHub APK is a separate flavor and must not be used as evidence for
the Play declaration.

## `specialUse` foreground service declaration

Current artifact evidence:

- `AndroidManifest.xml` declares `FOREGROUND_SERVICE` and
  `FOREGROUND_SERVICE_SPECIAL_USE`.
- `SessionService` declares `android:foregroundServiceType="specialUse"` and the
  subtype is `Interval workout timer keeping exact time and emitting audio cues while
  backgrounded or with the screen off.`
- The service is started only after the user presses Start for a workout. There is no
  boot, push, or background auto-start path.
- `SessionService.onStartCommand` calls `startForeground` immediately with the
  ongoing workout notification. The notification exposes the current phase and
  pause, resume, and stop controls when notification permission is available.
- Completion and explicit Stop end the session and stop the service; destruction
  releases the partial wake lock. A non-sticky service prevents an empty restart from
  holding a wake lock.

Before submission, enter the following facts in Play Console's foreground-service
declaration:

1. Functionality: an active interval workout timer must keep exact time, emit its
   audio/vibration cues, and remain controllable while the app is backgrounded or the
   screen is off.
2. User initiation: show the user selecting a workout, tapping Start, and the
   service/notification appearing as a direct result.
3. Visible notification: demonstrate the phase, remaining time, and the pause,
   resume, and stop actions in the notification shade or lock screen.
4. Stop behavior: demonstrate Stop and natural completion, then show that the
   ongoing notification disappears and the service no longer holds the wake lock.
5. Interruption impact: explain that deferring or interrupting the service can make
   the active workout lose time and miss phase cues, which defeats the timer's core
   purpose; the user can stop the workout explicitly at any time.
6. Demonstration video: upload a public or unlisted video showing the complete flow
   above, including backgrounding and screen-off behavior, and record its URL in the
   Play Console declaration. The repository's local preview video is not itself a
   hosted declaration URL.

`specialUse` is used because an interval timer is not media playback, data sync,
health tracking, location, or another defined service type. Google reviews special-use
declarations; the manifest, visible behavior, and Console explanation must stay in
agreement.

## Data Safety preparation

The current Play artifact audit supports the following preparation, subject to a
final dependency and artifact review:

- Ocho has no server, account, analytics, advertising, crash-reporting, cloud-sync,
  or Ocho-operated upload path.
- Workout settings and saved presets are processed and stored locally in Android
  DataStore. The Play flavor has no network permission and makes no Ocho network
  requests. Local-only processing is not an Ocho collection or sharing flow.
- The Play AAB contains no GitHub updater, `INTERNET`, `REQUEST_INSTALL_PACKAGES`,
  `FileProvider`, or install receiver. Do not use the GitHub flavor's privacy facts
  to complete the Play form.
- Review the final runtime dependency graph and every SDK's published data-safety
  information. Any SDK that transmits data must be reflected in the form even when
  the transmission goes to a third party rather than to Ocho.
- Review manifest permissions and runtime behavior, then complete the single global
  Data Safety form for the Play package. If the final audit still finds no collected
  or shared user data, select the no-collection/no-sharing responses and link the
  published privacy policy. Do not claim encryption in transit for a data flow that
  does not exist, and do not invent a deletion mechanism for data Ocho never
  collects.
- The app keeps `android:allowBackup="true"`. Android/device backup, transfer, and
  restore services may copy eligible local data according to the user's device
  configuration; this is disclosed in the privacy policy and is not an Ocho-operated
  cloud upload. Recheck the Play form and policy if backup behavior changes.
- Recheck the form whenever the manifest, dependencies, SDK configuration, or
  runtime data behavior changes. The declaration must describe the union of practices
  in versions currently distributed on Play.

## Official requirements

- [Foreground-service and full-screen intent requirements](https://support.google.com/googleplay/android-developer/answer/13392821)
- [Android foreground-service types](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Google Play Data Safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469)
