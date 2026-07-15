package com.malawi.radio.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.malawi.radio.data.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PlaybackState { IDLE, BUFFERING, PLAYING, PAUSED, ERROR }

data class PlayerUiState(
    val currentStation: RadioStation? = null,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val errorMessage: String? = null
)

/**
 * Wraps a single ExoPlayer instance for continuous radio streaming.
 * Handles buffering states and automatic reconnect on stream drop —
 * important because mobile data on many Malawi ISPs is unstable and
 * live streams don't "resume", they just die and need a fresh connect.
 */
class PlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
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
    }

    private fun updateState(state: PlaybackState, error: String? = null) {
        _uiState.value = _uiState.value.copy(playbackState = state, errorMessage = error)
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
        scope.launch {
            delay(1500L * reconnectAttempts) // simple backoff
            playStation(station, isReconnect = true)
        }
    }

    fun playStation(station: RadioStation, isReconnect: Boolean = false) {
        if (!isReconnect) reconnectAttempts = 0
        _uiState.value = _uiState.value.copy(currentStation = station, errorMessage = null)
        val mediaItem = MediaItem.fromUri(station.streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            updateState(PlaybackState.PAUSED)
        } else {
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
        }
    }

    fun stop() {
        exoPlayer.stop()
        updateState(PlaybackState.IDLE)
    }

    fun release() {
        exoPlayer.release()
    }
}
