package com.malawi.radio.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.util.AppStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

enum class PlaybackState { IDLE, BUFFERING, PLAYING, PAUSED, ERROR }

data class PlayerUiState(
    val currentStation: RadioStation? = null,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val errorMessage: String? = null,
    val currentTitle: String? = null
)

/**
 * Wraps a single ExoPlayer instance for continuous radio streaming.
 * Handles buffering states and automatic reconnect on stream drop —
 * important because mobile data on many Malawi ISPs is unstable and
 * live streams don't "resume", they just die and need a fresh connect.
 */
class PlayerManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var reconnectJob: Job? = null
    private var cacheTrimJob: Job? = null
    private var playGeneration = 0

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(appContext).build().apply {
            setWakeMode(C.WAKE_MODE_NETWORK)
            addListener(playerListener)
        }
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> updateState(PlaybackState.BUFFERING)
                Player.STATE_READY -> {
                    reconnectAttempts = 0
                    if (exoPlayer.playWhenReady) updateState(PlaybackState.PLAYING)
                }
                Player.STATE_ENDED -> attemptReconnect()
                Player.STATE_IDLE -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) updateState(PlaybackState.PLAYING)
        }

        override fun onPlayerError(error: PlaybackException) {
            attemptReconnect()
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val title = listOf(
                mediaMetadata.title,
                mediaMetadata.displayTitle,
                mediaMetadata.subtitle
            ).firstOrNull { !it.isNullOrBlank() }?.toString()?.trim()
            _uiState.value = _uiState.value.copy(currentTitle = title)
        }
    }

    private fun updateState(state: PlaybackState, error: String? = null) {
        _uiState.value = _uiState.value.copy(playbackState = state, errorMessage = error)
    }

    private fun ensureCacheTrimJob() {
        if (cacheTrimJob?.isActive == true) return
        cacheTrimJob = scope.launch(Dispatchers.IO) {
            while (true) {
                AppStorageManager.trimCache(appContext)
                delay(CACHE_TRIM_INTERVAL_MS)
            }
        }
    }

    private fun cancelCacheTrimJob() {
        cacheTrimJob?.cancel()
        cacheTrimJob = null
    }

    private fun attemptReconnect() {
        val station = _uiState.value.currentStation ?: run {
            updateState(PlaybackState.ERROR, "No station selected")
            return
        }
        if (reconnectAttempts >= maxReconnectAttempts) {
            updateState(PlaybackState.ERROR, "Couldn't connect. Check your internet and try again.")
            return
        }
        reconnectAttempts++
        updateState(PlaybackState.BUFFERING)
        val generation = playGeneration
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(1500L * reconnectAttempts) // simple backoff
            if (generation == playGeneration && _uiState.value.currentStation?.id == station.id) playStation(station, isReconnect = true)
        }
    }

    fun restoreStation(station: RadioStation) {
        if (_uiState.value.currentStation == null) {
            _uiState.value = _uiState.value.copy(currentStation = station, playbackState = PlaybackState.PAUSED, errorMessage = null, currentTitle = null)
        }
    }

    fun playStation(station: RadioStation, isReconnect: Boolean = false) {
        if (!isReconnect) {
            reconnectAttempts = 0
            playGeneration++
            reconnectJob?.cancel()
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
        _uiState.value = _uiState.value.copy(currentStation = station, errorMessage = null, currentTitle = null)
        val mediaItem = MediaItem.fromUri(station.streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        ensureCacheTrimJob()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            cancelCacheTrimJob()
            updateState(PlaybackState.PAUSED)
        } else {
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
            ensureCacheTrimJob()
        }
    }

    fun stop() {
        playGeneration++
        reconnectJob?.cancel()
        cancelCacheTrimJob()
        exoPlayer.stop()
        _uiState.value = _uiState.value.copy(playbackState = PlaybackState.IDLE, errorMessage = null, currentTitle = null)
    }

    fun release() {
        reconnectJob?.cancel()
        cancelCacheTrimJob()
        exoPlayer.release()
    }

    private companion object {
        const val CACHE_TRIM_INTERVAL_MS = 5 * 60 * 1000L
    }
}
