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
