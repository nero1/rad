package com.malawi.radio

import android.app.Application
import com.malawi.radio.data.local.FavoritesStore
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlayerManager

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

    override fun onCreate() {
        super.onCreate()
        playerManager = PlayerManager(this)
        stationRepository = StationRepository(this, FavoritesStore(this))
    }
}
