package com.malawi.radio.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlayerManager
import com.malawi.radio.player.PlayerUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    private val playerManager: PlayerManager,
    private val repository: StationRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerUiState> = playerManager.uiState

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun stop() {
        playerManager.stop()
    }

    fun toggleFavorite() {
        val stationId = playerState.value.currentStation?.id ?: return
        viewModelScope.launch { repository.toggleFavorite(stationId) }
    }
}
