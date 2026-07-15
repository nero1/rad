package com.malawi.radio

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malawi.radio.player.PlaybackState
import com.malawi.radio.ui.ViewModelFactory
import com.malawi.radio.ui.favorites.FavoritesScreen
import com.malawi.radio.ui.favorites.FavoritesViewModel
import com.malawi.radio.ui.nowplaying.NowPlayingScreen
import com.malawi.radio.ui.nowplaying.NowPlayingViewModel
import com.malawi.radio.ui.stationlist.StationListScreen
import com.malawi.radio.ui.stationlist.StationListViewModel
import com.malawi.radio.ui.theme.MalawiRadioTheme

private enum class Tab(val label: String) { STATIONS("Stations"), NOW_PLAYING("Now Playing"), FAVORITES("Favorites") }

class MainActivity : ComponentActivity() {

    private val factory by lazy { ViewModelFactory(application as MalawiRadioApp) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        setContent {
            MalawiRadioTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MalawiRadioApp(factory)
                }
            }
        }
    }
}

@Composable
private fun MalawiRadioApp(factory: ViewModelFactory) {
    val stationListVm: StationListViewModel = viewModel(factory = factory)
    val nowPlayingVm: NowPlayingViewModel = viewModel(factory = factory)
    val favoritesVm: FavoritesViewModel = viewModel(factory = factory)

    var selectedTab by remember { mutableStateOf(Tab.STATIONS) }
    val playerState by nowPlayingVm.playerState.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                // Mini-player bar, visible whenever a station is active and we're not
                // already on the Now Playing tab.
                if (playerState.currentStation != null && selectedTab != Tab.NOW_PLAYING) {
                    MiniPlayerBar(
                        stationName = playerState.currentStation!!.name,
                        isPlaying = playerState.playbackState == PlaybackState.PLAYING,
                        isBuffering = playerState.playbackState == PlaybackState.BUFFERING,
                        onTogglePlay = { nowPlayingVm.togglePlayPause() },
                        onClick = { selectedTab = Tab.NOW_PLAYING }
                    )
                }
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == Tab.STATIONS,
                        onClick = { selectedTab = Tab.STATIONS },
                        icon = { Icon(Icons.Filled.List, contentDescription = "Stations") },
                        label = { Text("Stations") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == Tab.NOW_PLAYING,
                        onClick = { selectedTab = Tab.NOW_PLAYING },
                        icon = { Icon(Icons.Filled.Radio, contentDescription = "Now Playing") },
                        label = { Text("Now Playing") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == Tab.FAVORITES,
                        onClick = { selectedTab = Tab.FAVORITES },
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                Tab.STATIONS -> StationListScreen(
                    viewModel = stationListVm,
                    onStationSelected = { selectedTab = Tab.NOW_PLAYING }
                )
                Tab.NOW_PLAYING -> NowPlayingScreen(viewModel = nowPlayingVm)
                Tab.FAVORITES -> FavoritesScreen(
                    viewModel = favoritesVm,
                    onStationSelected = { selectedTab = Tab.NOW_PLAYING }
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    stationName: String,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stationName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
