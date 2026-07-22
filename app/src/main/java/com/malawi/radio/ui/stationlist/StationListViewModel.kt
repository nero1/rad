package com.malawi.radio.ui.stationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StationListUiState(
    val allStations: List<RadioStation> = emptyList(),
    val stations: List<RadioStation> = emptyList(),
    val hiddenStationIds: Set<String> = emptySet(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

class StationListViewModel(
    private val repository: StationRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationListUiState())
    val uiState: StateFlow<StationListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.visibleStations().collect { stations ->
                _uiState.value = _uiState.value.copy(stations = stations, isLoading = false)
            }
        }
        viewModelScope.launch {
            val allStations = repository.getAllStations()
            _uiState.value = _uiState.value.copy(allStations = allStations)
        }
        viewModelScope.launch {
            repository.hiddenStationIds().collect { hiddenIds ->
                _uiState.value = _uiState.value.copy(hiddenStationIds = hiddenIds)
            }
        }
        viewModelScope.launch {
            repository.favoriteIds().collect { favIds ->
                _uiState.value = _uiState.value.copy(favoriteIds = favIds)
            }
        }
    }

    fun playStation(station: RadioStation) {
        playerManager.playStation(station)
        viewModelScope.launch { repository.recordStationPlayed(station.id) }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch { repository.toggleFavorite(stationId) }
    }

    fun setStationVisible(stationId: String, visible: Boolean) {
        viewModelScope.launch { repository.setStationVisible(stationId, visible) }
    }
}
