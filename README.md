# Malawi Radio

A native Android app (Kotlin + Jetpack Compose + Media3/ExoPlayer) for
streaming Malawi radio stations. No backend required — the station list is
bundled as JSON inside the app.

## ⚠️ Before you build a release

The stream URLs in `app/src/main/assets/stations.json` are **placeholders
that need verification** — see [`VERIFY_STREAMS.md`](./VERIFY_STREAMS.md) for
how to find and confirm the real ones. The app will build and run fine as-is;
individual stations just may not play until their URLs are corrected.

## Architecture

- **UI**: Jetpack Compose, single Activity, 3 tabs (Stations / Now Playing / Favorites)
- **Playback**: Media3 `ExoPlayer` + `MediaSessionService` for background
  playback with lock-screen/notification controls
- **Data**: Station list bundled in `assets/stations.json`, favorites persisted
  locally via Jetpack DataStore — nothing hits the network except the actual
  audio stream
- **DI**: Manual (no Hilt) — see `MalawiRadioApp.kt` and `ViewModelFactory.kt`

See `PROJECT_STRUCTURE.md` for the full file/folder breakdown and what each
piece does.

## Building via GitHub Actions (no local Android Studio needed)

Every push to `main` triggers `.github/workflows/build.yml`, which builds a
debug APK and uploads it as a workflow artifact:

1. Push this repo to GitHub
2. Go to the **Actions** tab → the latest run
3. Download the `malawi-radio-debug` artifact (a zip containing `app-debug.apk`)
4. Unzip, transfer the APK to your phone, enable "install unknown apps" for
   your file manager/browser, and install

Debug builds are unsigned by Google Play standards but installable directly —
no signing key needed for personal testing.

## Building a signed release later

When you're ready to publish (Play Store or sideload distribution), you'll
need to:
1. Generate a keystore
2. Add it as GitHub Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`)
3. Add a signing config block to `app/build.gradle.kts` and a `assembleRelease` step to the workflow

Ask for this setup when you're ready — it's a small addition on top of what's here.

## Local editing without a CLI

- Use **github.dev** (press `.` on the repo page in GitHub) for a full VS Code
  editor in the browser — works on mobile browsers
- Or the **GitHub mobile app** for quick single-file edits
- Commits to `main` auto-trigger the build workflow above

## Template configuration

Use [`xmanifest.md`](./xmanifest.md) as the central template manifest when cloning this project for another market, country, or niche. It records the app name, package name, theme options, AdMob test IDs, interstitial delay, icon guidance, background playback default, and versioning policy.

For release builds, copy [`.env.example`](./.env.example) into your own environment/secrets store and provide real values for:

- `ADMOB_APP_ID`
- `ADMOB_BANNER_ID`
- `ADMOB_INTERSTITIAL_ID`
- `APP_NAME`
- `APPLICATION_ID`
- `VERSION_CODE`
- `VERSION_NAME`

If AdMob values are omitted, the app uses Google's test AdMob IDs from `xmanifest.md` so debug builds are safe.

## Current app upgrades

- Targets Android API level 36.
- Versioning now starts at `1.00` (`VERSION_CODE=100`) so future Play Store builds can increment by `0.01` / +1 code.
- Added a fourth bottom-nav Settings screen with theme selection, background-play toggle, About, Help/FAQs, Contact, and Privacy Policy content.
- Added theme switching for Dark Mode, Light Mode, Rose Wind, Moonlight, Purple Hibiscus, Midnight, Blue Skies, Desert Heat, Snow, and Green Glory.
- Added AdMob dependency and banner placements using test IDs by default, including list-page header banners and now-playing banner space.
- Added back-button protection: non-home tabs return to Stations first; pressing back on Stations asks users to tap again before exiting.
- Added a sleep timer on Now Playing with 5, 10, 15, 30, 60, and 120 minute options, countdown display, +5 minutes, and cancel.
- Improved bad-stream handling by canceling stale reconnect attempts when the user selects another station.

## Battery, memory, cache, and bandwidth notes

The app keeps one shared ExoPlayer instance for the UI, notification, and background service rather than creating multiple players. Reconnect attempts are capped and canceled when the user switches stations, preventing old broken streams from continuing to buffer in the background. Station data remains bundled locally and favorites/settings use lightweight DataStore preferences, so normal app network usage is limited to audio streams and ads.
