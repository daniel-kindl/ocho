# Localization

Ocho is English-only for now, but both products have an explicit localization
boundary so translations can be added without moving copy out of business logic.

## Android app

The default locale is English (`en`). Android's generated per-app locale metadata is
enabled in `app/build.gradle.kts` with `generateLocaleConfig = true`, and
`app/src/main/res/resources.properties` declares `en` as the unqualified resource
locale. There is intentionally no language picker yet; Android can expose the app's
supported languages automatically when another locale is added.

Keep app UI copy in `app/src/main/res/values/strings.xml`. Add a translation in a
directory such as `app/src/main/res/values-de/strings.xml`, using the same resource
names. Use `stringResource()` and `pluralStringResource()` from Compose, or
`Context.getString()` in services, receivers, and view models. Do not return English
labels from domain models or format user-visible durations with string concatenation;
return structured values and resolve their wording at the UI/resource boundary.

Plural resources are used even where English currently has the same form. This keeps
durations and round/set counts ready for languages with different quantity rules.

The unit tests cover structured setup summaries, and the Android resource test pins
the current English output. When a locale is added, run both the JVM tests and the
instrumentation tests with that locale, including RTL checks where applicable.

## Website

The public Astro site keeps its copy in the typed `website/src/i18n/en.ts` module.
Shared components and pages read `siteCopy` from `website/src/i18n/index.ts`; the
default locale is `en`, and `getSiteCopy()` currently falls back to English for every
other request because no translated module exists yet.

To add a website locale:

1. Add a typed module beside `en.ts` with the same copy shape.
2. Register it in `website/src/i18n/index.ts` and choose a route or locale-selection
   strategy before exposing it publicly.
3. Keep image paths, links, and layout in Astro components; translate copy, labels,
   metadata, and accessibility text in the locale module.
4. Run `npm run build` and review the generated pages for text overflow and metadata.

There is no language selector or translated route until a second language is ready.
