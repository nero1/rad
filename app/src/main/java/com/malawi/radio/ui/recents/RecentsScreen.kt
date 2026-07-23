package com.malawi.radio.ui.recents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.ui.ads.MediumRectangleAd
import com.malawi.radio.ui.components.EmptyStationsNavigationHint
import com.malawi.radio.ui.stationlist.StationRow

@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    onStationSelected: (RadioStation) -> Unit,
    onSettingsClick: () -> Unit
) {
    val recents by viewModel.recents.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Recently Played", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onSettingsClick) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
        }
        if (recents.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("No recently played stations yet. Play a station and it will appear here.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }

                EmptyStationsNavigationHint(
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        } else {
            HorizontalBannerAd(Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recents.size, key = { recents[it].id }) { index ->
                    val station = recents[index]
                    StationRow(station = station, isFavorite = station.id in favoriteIds, onClick = { viewModel.playStation(station); onStationSelected(station) }, onFavoriteClick = { viewModel.toggleFavorite(station.id) })
                    if (index == 2) HorizontalBannerAd(Modifier.padding(vertical = 8.dp))
                }
                item { MediumRectangleAd(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) }
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}
