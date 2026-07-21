package com.malawi.radio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.malawi.radio.MainActivity
import com.malawi.radio.BuildConfig
import com.malawi.radio.MalawiRadioApp
import com.malawi.radio.i18n.I18n
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val PLAYBACK_NOTIFICATION_CHANNEL_ID = "radio_playback"
private const val PLAYBACK_NOTIFICATION_ID = 1001

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
        createNotificationChannel()
        startForeground(PLAYBACK_NOTIFICATION_ID, buildNotification())
        val playerManager = (application as MalawiRadioApp).playerManager
        mediaSession = MediaSession.Builder(this, playerManager.exoPlayer).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        startForeground(PLAYBACK_NOTIFICATION_ID, buildNotification())
        return result
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        (application as MalawiRadioApp).playerManager.stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            PLAYBACK_NOTIFICATION_CHANNEL_ID,
            serviceStrings().notificationChannelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = serviceStrings().notificationChannelDescription.format(BuildConfig.APP_NAME)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun serviceStrings() = I18n.strings(runBlocking { (application as MalawiRadioApp).settingsStore.settings.first().language })

    private fun buildNotification(): Notification {
        val stationName = (application as MalawiRadioApp).playerManager.uiState.value.currentStation?.name
            ?: BuildConfig.APP_NAME
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, PLAYBACK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(stationName)
            .setContentText(serviceStrings().notificationPlayingLiveRadio)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
