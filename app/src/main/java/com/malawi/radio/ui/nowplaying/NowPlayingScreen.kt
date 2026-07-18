package com.malawi.radio.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.malawi.radio.data.repository.StationRepository
import com.malawi.radio.player.PlaybackState
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.ui.ads.MediumRectangleAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NowPlayingScreen(viewModel: NowPlayingViewModel) {
    val state by viewModel.playerState.collectAsState()
    val station = state.currentStation
    var sleepRemaining by remember { mutableLongStateOf(0L) }
    var sleepMenu by remember { mutableStateOf(false) }
    LaunchedEffect(sleepRemaining) {
        if (sleepRemaining > 0) { delay(1000); sleepRemaining -= 1; if (sleepRemaining == 0L) viewModel.stop() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (station == null) {
            Icon(
                imageVector = Icons.Filled.Radio,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tap a station to start listening",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            return@Column
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Radio,
                contentDescription = null,
                modifier = Modifier.size(90.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = station.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        val subtitle = listOfNotNull(
            station.city.takeIf { it.isNotBlank() },
            station.genre.takeIf { it.isNotBlank() }
        ).joinToString(" · ")
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(10.dp))

        val statusText = when (state.playbackState) {
            PlaybackState.BUFFERING -> "Buffering…"
            PlaybackState.PLAYING -> "On air"
            PlaybackState.PAUSED -> "Paused"
            PlaybackState.ERROR -> state.errorMessage ?: "Playback error"
            PlaybackState.IDLE -> ""
        }
        if (statusText.isNotBlank()) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (state.playbackState == PlaybackState.ERROR)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(24.dp))

            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (state.playbackState == PlaybackState.PLAYING)
                        Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(24.dp))
            Box {
                IconButton(onClick = { sleepMenu = true }, modifier = Modifier.size(48.dp)) {
                    Icon(if (sleepRemaining > 0) Icons.Filled.TimerOff else Icons.Filled.Bedtime, contentDescription = "Sleep timer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = sleepMenu, onDismissRequest = { sleepMenu = false }) {
                    DropdownMenuItem(text = { Text("Sleep Timer") }, onClick = {})
                    if (sleepRemaining > 0) {
                        DropdownMenuItem(text = { Text("%02d:%02d remaining".format(sleepRemaining / 60, sleepRemaining % 60)) }, onClick = {})
                        DropdownMenuItem(text = { Text("+ Add 5 minutes") }, onClick = { sleepRemaining += 300 })
                        DropdownMenuItem(text = { Text("Cancel Timer") }, onClick = { sleepRemaining = 0; sleepMenu = false })
                    } else {
                        listOf(5L to "5 minutes", 10L to "10 minutes", 15L to "15 minutes", 30L to "30 minutes", 60L to "1 hour", 120L to "2 hours").forEach { (mins, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { sleepRemaining = mins * 60; sleepMenu = false })
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        MediumRectangleAd(Modifier.padding(horizontal = 8.dp))
        Spacer(Modifier.height(10.dp))
        HorizontalBannerAd(Modifier.padding(horizontal = 8.dp))
    }
}
