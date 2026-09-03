# Google Play production pipeline

Ocho keeps production publication separate from ordinary CI and from the Closed-testing upload path.
The production workflow is `.github/workflows/play-production.yml`.

The workflow does not upload a new AAB to production. It references the exact `versionCode` that already
exists in the tested Closed-testing release. This prevents production from using a rebuilt artifact that was
not the one tested in Play.

## Safety model

The workflow has two manual modes.

### `check`

`check` is read-only with respect to published Play state. It:

1. creates a temporary Android Publisher edit;
2. lists the app bundles currently known to Play;
3. reads the supplied Closed-testing track;
4. confirms that the requested `versionCode` exists and belongs to exactly one completed release;
5. reports the highest uploaded bundle `versionCode`; and
6. deletes the temporary edit without committing it.

Run this mode from `dev` or `main`. It uses the existing `play-upload` environment and
`PLAY_SERVICE_ACCOUNT_JSON` secret.

This mode is the preferred way to confirm whether the next proposed `versionCode` is safe. For example, if
`check` reports that the highest uploaded bundle is `16`, the next upload must use a code greater than `16`.

### `create-draft`

`create-draft` is intentionally stricter. It runs only when all of these conditions are true:

- the workflow is dispatched from `main`;
- the supplied stable tag is strict `vMAJOR.MINOR.PATCH` SemVer;
- that tag points to the exact checked-out `main` commit;
- the tag passes the stable release provenance guard;
- the tag matches `versionName` in `app/build.gradle.kts`;
- the supplied `versionCode` matches `versionCode` in `app/build.gradle.kts`;
- the GitHub Release for the tag is already published and is not a prerelease;
- the supplied Closed-testing release contains that exact `versionCode` and is completed;
- the bundle exists in Play;
- Play does not contain a higher uploaded bundle version code;
- the production track does not already contain that version code;
- production has no draft, in-progress, or halted release that must be resolved first; and
- the dispatcher enters the exact confirmation text `CREATE PRODUCTION DRAFT`.

The workflow then updates the production track with a release whose status is `draft`, validates the Android
Publisher edit, and commits the edit. It never creates an `inProgress` or `completed` production release.

A draft release is not served to users.

## First production release

Google Play does not offer staged rollout percentages for an app's first production release. Starting the first
production rollout publishes the app to all users in the selected countries or regions.

For Ocho's first public Play release, the final action therefore remains manual:

1. run `check` against the exact Closed-testing candidate;
2. complete the device and manual release checks;
3. complete and verify the foreground-service declaration and demonstration video;
4. verify Data Safety, privacy policy, store listing, content declarations, and country availability;
5. run `create-draft` from the stable `main` tag;
6. open the resulting draft in Play Console and review every warning and declaration; and
7. only then use Play Console to start the production rollout.

The workflow must not be changed to `completed` for the first release.

## GitHub environment setup

Keep production credentials separate from Closed-testing credentials.

Create a GitHub environment named `play-production` and restrict it to the stable release path. Configure any
available environment approval protection that is practical for the repository. Store the production Play service
account JSON as the environment secret:

`PLAY_PRODUCTION_SERVICE_ACCOUNT_JSON`

The production service account needs the Play Console permission required to create and edit production releases.
That permission can publish an app to users, so treat the credential as a production secret. Prefer an app-scoped
service account rather than a broad account-wide credential.

The Closed-testing workflow continues to use `PLAY_SERVICE_ACCOUNT_JSON` through the separate `play-upload`
environment.

The production tooling authenticates directly with Google's OAuth service-account flow and requests only the
`https://www.googleapis.com/auth/androidpublisher` scope. The JSON credential is not written to the repository or
included in an artifact.

## Failure behavior

The workflow fails without committing a production edit when:

- the source release is not completed;
- the requested version is absent from Play;
- a higher bundle version already exists;
- an unfinished production release exists;
- the stable tag/source/version checks do not match; or
- Android Publisher edit validation fails.

For `check`, and for failed `create-draft` attempts before commit, the temporary edit is deleted on a best-effort
basis.

The workflow does not:

- build or upload a replacement AAB;
- create a Git tag;
- create a GitHub Release;
- send Play changes for review automatically;
- start a staged rollout;
- start the first public rollout; or
- change Play App Signing, country availability, Data Safety, or policy declarations.

## Later production updates

Keep the draft-first model after the first release. Staged rollouts are available for updates, but rollout percentage
and final publication should remain explicit Play Console decisions until Ocho has enough production history to
justify automating them.

If rollout automation is added later, implement it as a separate reviewed step. Do not weaken the exact-version,
stable-tag, environment, and unfinished-release guards in this workflow.

## Official references

- Google Play release preparation and rollout: https://support.google.com/googleplay/android-developer/answer/9859348
- Staged rollouts: https://support.google.com/googleplay/android-developer/answer/6346149
- Android Publisher API: https://developers.google.com/android-publisher/api-ref/rest
- Track resource and release status: https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks
- Track update: https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks/update
- Bundle list: https://developers.google.com/android-publisher/api-ref/rest/v3/edits.bundles/list
- Play Console user permissions: https://support.google.com/googleplay/android-developer/answer/9844686
