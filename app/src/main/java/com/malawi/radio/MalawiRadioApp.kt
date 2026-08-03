package com.malawi.radio

import android.app.Application
import com.malawi.radio.data.local.FavoritesStore
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlayerManager
import com.malawi.radio.data.settings.AppSettingsStore
import com.google.android.gms.ads.MobileAds
import com.malawi.radio.util.AppStorageManager

/**
 * Holds app-wide singletons. This is a small enough app that a lightweight manual
 * DI setup here is simpler than pulling in Hilt/Koin — PlayerManager in particular
 * MUST be a single shared instance, since both the UI (ViewModels) and the
 * background RadioPlaybackService need to control the same ExoPlayer.
 */
class MalawiRadioApp : Application() {

    lateinit var playerManager: PlayerManager
        private set
    lateinit var stationRepository: StationRepository
        private set
    lateinit var settingsStore: AppSettingsStore
        private set

    override fun onCreate() {
        super.onCreate()

        playerManager = PlayerManager(this)
        stationRepository = StationRepository(this, FavoritesStore(this))
        settingsStore = AppSettingsStore(this)

        MobileAds.initialize(this)

        // Off the main thread: this walks cacheDir/codeCacheDir/externalCacheDir
        // and can involve real disk I/O (and file deletion) if the cache has
        // grown large since last launch. Running it inline here would add to
        // cold start time, worst exactly when there's the most cache to trim.
        Thread {
            AppStorageManager.trimStartupCache(this@MalawiRadioApp)
        }.start()
    }
}
