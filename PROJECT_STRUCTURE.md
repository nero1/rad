# Project structure

```
MalawiRadio/
├── .github/workflows/build.yml       CI: builds debug APK on every push to main
├── gradle/wrapper/                   Gradle wrapper binaries (lets Actions build without local Gradle install)
├── gradlew, gradlew.bat              Wrapper scripts (already executable)
├── settings.gradle.kts               Declares the `app` module
├── build.gradle.kts                  Root: declares plugin versions (AGP, Kotlin, KSP, serialization)
├── gradle.properties                 JVM args, AndroidX flags
├── VERIFY_STREAMS.md                 How to find/confirm real station stream URLs
├── README.md                         Setup + build instructions
│
└── app/
    ├── build.gradle.kts              App module deps: Compose, Media3/ExoPlayer, Room-adjacent DataStore, kotlinx.serialization, Coil
    ├── proguard-rules.pro
    │
    └── src/main/
        ├── AndroidManifest.xml       Declares MainActivity, RadioPlaybackService (foreground media service), permissions
        │
        ├── assets/
        │   └── stations.json         ⭐ THE STATION LIST — edit this to add/fix stations, no code changes needed
        │
        ├── res/
        │   ├── values/
        │   │   ├── strings.xml
        │   │   ├── colors.xml        Malawi flag palette (black/red/green/sun-yellow)
        │   │   └── themes.xml
        │   ├── drawable/
        │   │   ├── ic_launcher_foreground.xml   Sun + radio-wave icon (not the default Android robot)
        │   │   └── ic_notification.xml
        │   ├── mipmap-anydpi-v26/    Adaptive launcher icon XML
        │   └── xml/
        │       └── network_security_config.xml  Allows HTTP streams (many Icecast/Shoutcast stations aren't HTTPS)
        │
        └── java/com/malawi/radio/
            ├── MalawiRadioApp.kt              Application class — holds the single shared PlayerManager + StationRepository
            ├── MainActivity.kt                Single Activity: bottom nav (Stations/Now Playing/Favorites) + mini-player bar
            │
            ├── data/
            │   ├── model/
            │   │   └── RadioStation.kt        Data class matching stations.json shape (kotlinx.serialization)
            │   ├── local/
            │   │   └── FavoritesStore.kt       DataStore-backed favorite station IDs + last-played station
            │   └── repository/
            │       └── StationRepository.kt    Loads stations.json from assets, combines with favorites
            │
            ├── player/
            │   ├── PlayerManager.kt            Wraps ExoPlayer: play/pause, state flow, auto-reconnect w/ backoff on stream drop
            │   └── RadioPlaybackService.kt      Media3 MediaSessionService — background playback + lock-screen controls
            │
            ├── ui/
            │   ├── ViewModelFactory.kt          Manual DI (no Hilt) — wires ViewModels to shared singletons
            │   ├── stationlist/
            │   │   ├── StationListScreen.kt      Browsable list of all stations
            │   │   └── StationListViewModel.kt
            │   ├── nowplaying/
            │   │   ├── NowPlayingScreen.kt        Big "now playing" card: play/pause, favorite, status (buffering/error)
            │   │   └── NowPlayingViewModel.kt
            │   ├── favorites/
            │   │   ├── FavoritesScreen.kt
            │   │   └── FavoritesViewModel.kt
            │   └── theme/
            │       ├── Color.kt                  Malawi flag colors as Compose Color values
            │       ├── Theme.kt                   Dark theme (radio/night-listening friendly)
            │       └── Type.kt
            │
            └── util/
                └── NetworkConnectivityObserver.kt  Flow of online/offline state (used to avoid hammering a dead stream)
```

## Data flow at a glance

```
stations.json (assets)
      │
      ▼
StationRepository ──────► StationListViewModel ──► StationListScreen
      │                                                    │
      │                                          user taps a station
      │                                                    ▼
      │                                          PlayerManager.playStation()
      │                                                    │
      ▼                                                    ▼
FavoritesStore (DataStore)              ExoPlayer ── RadioPlaybackService (background/lock-screen)
      │                                                    │
      ▼                                                    ▼
FavoritesViewModel/Screen                     NowPlayingViewModel/Screen (state flow)
```

`PlayerManager` is created once in `MalawiRadioApp` and shared by both the UI
(via ViewModels) and `RadioPlaybackService`, so there's only ever one
`ExoPlayer` instance controlling playback — the UI and the background service
are just two windows onto the same player state.
