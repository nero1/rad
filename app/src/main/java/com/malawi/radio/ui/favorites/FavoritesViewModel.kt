package com.malawi.radio.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: StationRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<RadioStation>>(emptyList())
    val favorites: StateFlow<List<RadioStation>> = _favorites

    init {
        viewModelScope.launch {
            repository.favoriteStations().collect { _favorites.value = it }
        }
    }

    fun playStation(station: RadioStation) {
        playerManager.playStation(station)
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch { repository.toggleFavorite(stationId) }
    }
}
