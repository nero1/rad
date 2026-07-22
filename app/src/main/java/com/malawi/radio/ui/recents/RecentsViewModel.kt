package com.malawi.radio.ui.recents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecentsViewModel(
    private val repository: StationRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _recents = MutableStateFlow<List<RadioStation>>(emptyList())
    val recents: StateFlow<List<RadioStation>> = _recents

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    init {
        viewModelScope.launch { repository.recentStations().collect { _recents.value = it } }
        viewModelScope.launch { repository.favoriteIds().collect { _favoriteIds.value = it } }
    }

    fun playStation(station: RadioStation) {
        playerManager.playStation(station)
        viewModelScope.launch { repository.recordStationPlayed(station.id) }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch { repository.toggleFavorite(stationId) }
    }
}
