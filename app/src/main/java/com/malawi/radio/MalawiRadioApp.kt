package com.malawi.radio

import android.app.Application
import android.content.ComponentCallbacks2
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
        AppStorageManager.trimCache(this)
        MobileAds.initialize(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            AppStorageManager.trimCache(this)
        }
    }
}
