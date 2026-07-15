package com.malawi.radio.player

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.malawi.radio.MalawiRadioApp

/**
 * Foreground service that keeps the radio stream alive in the background and
 * exposes lock-screen / notification playback controls (play, pause, station name)
 * via Media3's MediaSession. This is what lets the app keep playing when the
 * user switches apps or locks their phone.
 *
 * Uses the shared PlayerManager from MalawiRadioApp rather than creating its own,
 * so the UI and the background service always control the same ExoPlayer instance.
 */
class RadioPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val playerManager = (application as MalawiRadioApp).playerManager
        mediaSession = MediaSession.Builder(this, playerManager.exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
