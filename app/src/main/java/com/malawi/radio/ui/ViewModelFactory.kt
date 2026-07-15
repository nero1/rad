package com.malawi.radio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.malawi.radio.MalawiRadioApp
import com.malawi.radio.ui.favorites.FavoritesViewModel
import com.malawi.radio.ui.nowplaying.NowPlayingViewModel
import com.malawi.radio.ui.stationlist.StationListViewModel

/**
 * Simple manual DI factory. This app is small enough that pulling in Hilt would add
 * more build complexity than it saves — everything just needs the shared
 * StationRepository and PlayerManager from MalawiRadioApp.
 */
class ViewModelFactory(private val app: MalawiRadioApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            StationListViewModel::class.java ->
                StationListViewModel(app.stationRepository, app.playerManager) as T
            NowPlayingViewModel::class.java ->
                NowPlayingViewModel(app.playerManager, app.stationRepository) as T
            FavoritesViewModel::class.java ->
                FavoritesViewModel(app.stationRepository, app.playerManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
