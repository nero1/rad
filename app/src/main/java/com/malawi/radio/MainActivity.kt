package com.malawi.radio

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.malawi.radio.ui.settings.SettingsScreen
import com.malawi.radio.ui.settings.SettingsViewModel
import com.malawi.radio.ui.stationlist.StationListScreen
import com.malawi.radio.ui.stationlist.StationListViewModel
import com.malawi.radio.ui.theme.AppThemeOption
import com.malawi.radio.ui.theme.MalawiRadioTheme

private enum class Tab(val label: String) { STATIONS("Stations"), NOW_PLAYING("Now Playing"), FAVORITES("Favorites"), SETTINGS("Settings") }

class MainActivity : ComponentActivity() {
    private val factory by lazy { ViewModelFactory(application as MalawiRadioApp) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        setContent { MalawiRadioApp(factory, onExit = { finish() }) }
    }
}

@Composable
private fun MalawiRadioApp(factory: ViewModelFactory, onExit: () -> Unit) {
    val stationListVm: StationListViewModel = viewModel(factory = factory)
    val nowPlayingVm: NowPlayingViewModel = viewModel(factory = factory)
    val favoritesVm: FavoritesViewModel = viewModel(factory = factory)
    val settingsVm: SettingsViewModel = viewModel(factory = factory)
    val settings by settingsVm.settings.collectAsState(initial = com.malawi.radio.data.settings.AppSettings())
    val context = androidx.compose.ui.platform.LocalContext.current

    MalawiRadioTheme(themeOption = settings.theme) {
        var selectedTab by remember { mutableStateOf(Tab.STATIONS) }
        var backArmedAt by remember { mutableLongStateOf(0L) }
        val playerState by nowPlayingVm.playerState.collectAsState()

        BackHandler {
            if (selectedTab != Tab.STATIONS) selectedTab = Tab.STATIONS else {
                val now = System.currentTimeMillis()
                if (now - backArmedAt < 2000) onExit() else {
                    backArmedAt = now
                    Toast.makeText(context, "Tap back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Scaffold(bottomBar = { BottomArea(playerState, selectedTab, { nowPlayingVm.togglePlayPause() }, { selectedTab = Tab.NOW_PLAYING }, { selectedTab = it }) }) { padding ->
            Box(Modifier.padding(padding)) {
                when (selectedTab) {
                    Tab.STATIONS -> StationListScreen(stationListVm, onStationSelected = { selectedTab = Tab.NOW_PLAYING }, currentTheme = settings.theme, onThemeSelected = settingsVm::setTheme)
                    Tab.NOW_PLAYING -> NowPlayingScreen(viewModel = nowPlayingVm)
                    Tab.FAVORITES -> FavoritesScreen(favoritesVm, onStationSelected = { selectedTab = Tab.NOW_PLAYING })
                    Tab.SETTINGS -> SettingsScreen(settingsVm, appName = "Malawi Radio")
                }
            }
        }
    }
}

@Composable
private fun BottomArea(playerState: com.malawi.radio.player.PlayerUiState, selectedTab: Tab, onTogglePlay: () -> Unit, onMiniClick: () -> Unit, onTab: (Tab) -> Unit) {
    Column {
        if (playerState.currentStation != null && selectedTab != Tab.NOW_PLAYING) MiniPlayerBar(playerState.currentStation!!.name, playerState.playbackState == PlaybackState.PLAYING, playerState.playbackState == PlaybackState.BUFFERING, onTogglePlay, onMiniClick)
        NavigationBar {
            listOf(Tab.STATIONS to Icons.Filled.List, Tab.NOW_PLAYING to Icons.Filled.Radio, Tab.FAVORITES to Icons.Filled.Favorite, Tab.SETTINGS to Icons.Filled.Settings).forEach { (tab, icon) ->
                NavigationBarItem(selected = selectedTab == tab, onClick = { onTab(tab) }, icon = { Icon(icon, contentDescription = tab.label) }, label = { Text(tab.label) })
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(stationName: String, isPlaying: Boolean, isBuffering: Boolean, onTogglePlay: () -> Unit, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stationName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (isBuffering) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary) else IconButton(onClick = onTogglePlay) { Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play/Pause", tint = MaterialTheme.colorScheme.primary) }
    }
}
