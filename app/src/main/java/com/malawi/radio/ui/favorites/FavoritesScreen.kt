package com.malawi.radio.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.ui.stationlist.StationRow
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.i18n.AppLanguage
import com.malawi.radio.i18n.I18n
import com.malawi.radio.ui.ads.MediumRectangleAd

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onStationSelected: (RadioStation) -> Unit,
    language: AppLanguage
) {
    val favorites by viewModel.favorites.collectAsState()
    val strings = I18n.strings(language)

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = strings.favorites,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp)
        )
        if (favorites.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = strings.noFavorites,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            HorizontalBannerAd(Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites.size, key = { favorites[it].id }) { index ->
                    val station = favorites[index]
                    StationRow(
                        station = station,
                        isFavorite = true,
                        onClick = {
                            viewModel.playStation(station)
                            onStationSelected(station)
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(station.id) },
                        strings = strings
                    )
                    if (index == 2) {
                        HorizontalBannerAd(Modifier.padding(vertical = 8.dp))
                    }
                }
                item { MediumRectangleAd(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}
