package com.malawi.radio.ui.nowplaying

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.malawi.radio.BuildConfig
import com.malawi.radio.i18n.AppLanguage
import com.malawi.radio.i18n.I18n
import com.malawi.radio.player.PlaybackState
import com.malawi.radio.ui.ads.MediumRectangleAd
import com.malawi.radio.ui.components.MarqueeText

@Composable
fun NowPlayingScreen(viewModel: NowPlayingViewModel, language: AppLanguage) {
    val state by viewModel.playerState.collectAsState()
    val station = state.currentStation
    val sleepRemaining by viewModel.sleepRemaining.collectAsState()
    val isFavorite by viewModel.isCurrentFavorite.collectAsState()
    var sleepMenu by remember { mutableStateOf(false) }
    val strings = I18n.strings(language)

    if (station == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            EmptyNowPlayingPrompt(strings)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(124.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Radio,
                contentDescription = null,
                modifier = Modifier.size(68.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

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

        Spacer(Modifier.height(4.dp))
        MediumRectangleAd(Modifier.padding(horizontal = 8.dp, vertical = 0.dp))
        Spacer(Modifier.height(2.dp))

        val statusText = when (state.playbackState) {
            PlaybackState.BUFFERING -> strings.buffering
            PlaybackState.PLAYING -> strings.onAir
            PlaybackState.PAUSED -> strings.paused
            PlaybackState.ERROR -> state.errorMessage ?: strings.playbackError
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

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = strings.favorite,
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(24.dp))

            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (state.playbackState == PlaybackState.PLAYING)
                        Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = strings.playPause,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(24.dp))
            Box {
                IconButton(onClick = { sleepMenu = true }, modifier = Modifier.size(48.dp)) {
                    Icon(if (sleepRemaining > 0) Icons.Filled.HourglassBottom else Icons.Filled.Bedtime, contentDescription = strings.sleepTimer, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = sleepMenu, onDismissRequest = { sleepMenu = false }) {
                    DropdownMenuItem(text = { Text(strings.sleepTimer) }, onClick = {})
                    if (sleepRemaining > 0) {
                        DropdownMenuItem(text = { Text(strings.remaining.format(sleepRemaining / 60, sleepRemaining % 60)) }, onClick = {})
                        DropdownMenuItem(text = { Text(strings.add5Minutes) }, onClick = { viewModel.addSleepTime(300); sleepMenu = false })
                        DropdownMenuItem(text = { Text(strings.cancelTimer) }, onClick = { viewModel.cancelSleepTimer(); sleepMenu = false })
                    } else {
                        listOf(5L to strings.minutes5, 10L to strings.minutes10, 15L to strings.minutes15, 30L to strings.minutes30, 60L to strings.hour1, 120L to strings.hours2).forEach { (mins, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.setSleepTimer(mins * 60); sleepMenu = false })
                        }
                    }
                }
            }
        }
        state.currentTitle?.takeIf { it.isNotBlank() }?.let { title ->
            Spacer(Modifier.height(6.dp))
            if (BuildConfig.SCROLLING_MARQUEE_ENABLED) {
                MarqueeSongTitle(
                    title = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                )
            } else {
                Spacer(Modifier.height(20.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun MarqueeSongTitle(title: String, modifier: Modifier = Modifier) {
    MarqueeText(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        staticAlignment = Alignment.Center
    )
}

@Composable
private fun EmptyNowPlayingPrompt(strings) {
    val transition = rememberInfiniteTransition(label = "stations-nav-hint")
    val offsetY by transition.animateFloat(
        initialValue = -36f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stations-nav-hint-offset"
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Radio,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = strings.tapStationStart,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-10).dp)
                .fillMaxWidth(0.25f)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = strings.goToStations,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                modifier = Modifier
                    .graphicsLayer { translationY = offsetY }
                    .size(48.dp)
            )
        }
    }
}
