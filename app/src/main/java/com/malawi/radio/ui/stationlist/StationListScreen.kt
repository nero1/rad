package com.malawi.radio.ui.stationlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.ui.theme.AppThemeOption

@Composable
fun StationListScreen(
    viewModel: StationListViewModel,
    onStationSelected: (RadioStation) -> Unit,
    currentTheme: AppThemeOption = AppThemeOption.DARK_MODE,
    onThemeSelected: (AppThemeOption) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Malawi Radio", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            var themeMenu by rememberSaveable { mutableStateOf(false) }

            IconButton(onClick = { themeMenu = true }) { Icon(Icons.Filled.Palette, contentDescription = "Change theme") }
            DropdownMenu(expanded = themeMenu, onDismissRequest = { themeMenu = false }) {
                AppThemeOption.entries.forEach { theme -> DropdownMenuItem(text = { Text(theme.label) }, onClick = { onThemeSelected(theme); themeMenu = false }) }
            }
        }
        HorizontalBannerAd(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.stations, key = { it.id }) { station ->
                    StationRow(
                        station = station,
                        isFavorite = station.id in state.favoriteIds,
                        onClick = {
                            viewModel.playStation(station)
                            onStationSelected(station)
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(station.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) } // room for mini-player bar
            }
        }
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
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
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

        Spacer(Modifier.width(14.dp))

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
