package com.malawi.radio.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlayerManager
import com.malawi.radio.player.PlayerUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    private val playerManager: PlayerManager,
    private val repository: StationRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerUiState> = playerManager.uiState

    private val _sleepRemaining = MutableStateFlow(0L)
    val sleepRemaining: StateFlow<Long> = _sleepRemaining.asStateFlow()
    private var sleepJob: Job? = null

    private val _isCurrentFavorite = MutableStateFlow(false)
    val isCurrentFavorite: StateFlow<Boolean> = _isCurrentFavorite.asStateFlow()

    init {
        viewModelScope.launch {
            combine(playerManager.uiState, repository.favoriteIds()) { state, favoriteIds ->
                state.currentStation?.id in favoriteIds
            }.collect { _isCurrentFavorite.value = it }
        }
    }

    fun restoreLastStationIfIdle() {
        viewModelScope.launch {
            repository.lastStation().collect { station ->
                if (station != null && playerState.value.currentStation == null) {
                    playerManager.restoreStation(station)
                }
            }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun stop() {
        playerManager.stop()
    }

    fun toggleFavorite() {
        val stationId = playerState.value.currentStation?.id ?: return
        _isCurrentFavorite.value = !_isCurrentFavorite.value
        viewModelScope.launch { repository.toggleFavorite(stationId) }
    }

    fun setSleepTimer(seconds: Long) {
        _sleepRemaining.value = seconds
        startSleepTimer()
    }

    fun addSleepTime(seconds: Long) {
        setSleepTimer(_sleepRemaining.value + seconds)
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _sleepRemaining.value = 0L
    }

    private fun startSleepTimer() {
        sleepJob?.cancel()
        sleepJob = viewModelScope.launch {
            while (_sleepRemaining.value > 0) {
                delay(1000)
                _sleepRemaining.value = (_sleepRemaining.value - 1).coerceAtLeast(0)
            }
            playerManager.stop()
        }
    }
}
