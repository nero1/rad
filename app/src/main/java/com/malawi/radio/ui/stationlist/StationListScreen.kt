package com.malawi.radio.ui.stationlist

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.malawi.radio.BuildConfig
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.ui.ads.MediumRectangleAd
import com.malawi.radio.ui.theme.AppThemeOption
import com.malawi.radio.ui.theme.DefaultAppThemeOption
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun StationListScreen(
    viewModel: StationListViewModel,
    onStationSelected: (RadioStation) -> Unit,
    currentTheme: AppThemeOption = DefaultAppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit = {},
    showScrollHint: Boolean = false,
    onScrollHintShown: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(BuildConfig.APP_NAME, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            var themeMenu by rememberSaveable { mutableStateOf(false) }

            IconButton(onClick = { themeMenu = true }) { Icon(Icons.Filled.Palette, contentDescription = "Change theme") }
            DropdownMenu(expanded = themeMenu, onDismissRequest = { themeMenu = false }) {
                AppThemeOption.entries.forEach { theme -> DropdownMenuItem(text = { Text(theme.label) }, onClick = { onThemeSelected(theme); themeMenu = false }) }
            }
        }
        HorizontalBannerAd(Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val listState = rememberLazyListState()
            var isScrollHintVisible by rememberSaveable(showScrollHint) { mutableStateOf(showScrollHint) }

            LaunchedEffect(showScrollHint) {
                if (showScrollHint) {
                    isScrollHintVisible = true
                    delay(5_500)
                    isScrollHintVisible = false
                    onScrollHintShown()
                }
            }

            LaunchedEffect(listState.isScrollInProgress) {
                if (listState.isScrollInProgress) isScrollHintVisible = false
            }

            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.stations.size, key = { state.stations[it].id }) { index ->
                        val station = state.stations[index]
                        StationRow(
                            station = station,
                            isFavorite = station.id in state.favoriteIds,
                            onClick = {
                                viewModel.playStation(station)
                                onStationSelected(station)
                            },
                            onFavoriteClick = { viewModel.toggleFavorite(station.id) }
                        )
                        if (index == 2) {
                            HorizontalBannerAd(Modifier.padding(vertical = 8.dp))
                        }
                    }
                    item { MediumRectangleAd(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) }
                    item { Spacer(Modifier.height(96.dp)) } // room for mini-player bar
                }

                if (isScrollHintVisible) {
                    ScrollDownHint(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollDownHint(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scroll-hint")
    val offsetY by transition.animateFloat(
        initialValue = 56f,
        targetValue = -56f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "scroll-hint-offset"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.58f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "scroll-hint-alpha"
    )

    Box(
        modifier = modifier
            .height(136.dp)
            .width(88.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Icon(
            imageVector = Icons.Filled.TouchApp,
            contentDescription = "Slide up for more stations",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
            modifier = Modifier
                .offset { IntOffset(x = 0, y = offsetY.roundToInt()) }
                .graphicsLayer { rotationZ = -12f }
                .size(52.dp)
        )
    }
}

@Composable
fun StationRow(
    station: RadioStation,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            val subtitle = listOfNotNull(
                station.city.takeIf { it.isNotBlank() },
                station.genre.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
