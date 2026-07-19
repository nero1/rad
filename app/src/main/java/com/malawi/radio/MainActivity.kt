package com.malawi.radio

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.system.exitProcess
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.malawi.radio.player.PlaybackState
import com.malawi.radio.player.RadioPlaybackService
import com.malawi.radio.ui.ads.DEFAULT_INTERSTITIAL_AD_UNIT_ID
import com.malawi.radio.ui.ads.INTERSTITIAL_DELAY_MINUTES
import com.malawi.radio.ui.components.MarqueeText
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

private const val SCROLL_HINT_PREFS = "scroll_hint_prefs"
private const val SCROLL_HINT_VERSION_KEY = "version_code"
private const val SCROLL_HINT_OPEN_COUNT_KEY = "open_count"

private enum class Tab(val label: String) { STATIONS("Stations"), NOW_PLAYING("Player"), FAVORITES("Faves"), SETTINGS("Settings") }

class MainActivity : ComponentActivity() {
    private val factory by lazy { ViewModelFactory(application as MalawiRadioApp) }

    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        loadInterstitial()
        setContent { MalawiRadioApp(factory, activity = this, onExit = ::exitAppCompletely) }
    }

    private fun exitAppCompletely() {
        val app = application as MalawiRadioApp
        app.playerManager.stop()
        app.playerManager.release()
        stopService(Intent(this, RadioPlaybackService::class.java))
        finishAndRemoveTask()
        exitProcess(0)
    }

    fun showInterstitialIfLoaded() {
        interstitialAd?.show(this)
        interstitialAd = null
        loadInterstitial()
    }

    private fun loadInterstitial() {
        InterstitialAd.load(this, DEFAULT_INTERSTITIAL_AD_UNIT_ID, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
            override fun onAdFailedToLoad(error: LoadAdError) { interstitialAd = null }
        })
    }
}

@Composable
private fun MalawiRadioApp(factory: ViewModelFactory, activity: MainActivity, onExit: () -> Unit) {
    val stationListVm: StationListViewModel = viewModel(factory = factory)
    val nowPlayingVm: NowPlayingViewModel = viewModel(factory = factory)
    val favoritesVm: FavoritesViewModel = viewModel(factory = factory)
    val settingsVm: SettingsViewModel = viewModel(factory = factory)
    val settings by settingsVm.settings.collectAsState(initial = com.malawi.radio.data.settings.AppSettings())
    val context = androidx.compose.ui.platform.LocalContext.current

    MalawiRadioTheme(themeOption = settings.theme) {
        var selectedTab by remember { mutableStateOf(Tab.STATIONS) }
        var backArmedAt by remember { mutableLongStateOf(0L) }
        var nextInterstitialAt by remember { mutableLongStateOf(System.currentTimeMillis() + INTERSTITIAL_DELAY_MINUTES * 60_000L) }
        val playerState by nowPlayingVm.playerState.collectAsState()
        var showStationScrollHint by remember { mutableStateOf(activity.shouldShowStationScrollHint()) }

        val lifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(playerState.playbackState, settings.backgroundPlay) {
            val intent = Intent(context, RadioPlaybackService::class.java)
            if (settings.backgroundPlay && playerState.currentStation != null && playerState.playbackState != PlaybackState.IDLE) {
                context.startRadioPlaybackForegroundService(intent)
            } else if (!settings.backgroundPlay || playerState.playbackState == PlaybackState.IDLE) {
                context.stopService(intent)
            }
        }

        DisposableEffect(lifecycleOwner, settings.backgroundPlay) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP && !settings.backgroundPlay) {
                    nowPlayingVm.stop()
                    context.stopService(Intent(context, RadioPlaybackService::class.java))
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        BackHandler {
            if (selectedTab != Tab.STATIONS) selectedTab = Tab.STATIONS else {
                val now = System.currentTimeMillis()
                if (now - backArmedAt < 2000) onExit() else {
                    backArmedAt = now
                    Toast.makeText(context, "Tap back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val selectTab: (Tab) -> Unit = { tab ->
            if (tab != selectedTab) {
                val now = System.currentTimeMillis()
                if (now >= nextInterstitialAt) {
                    activity.showInterstitialIfLoaded()
                    nextInterstitialAt = now + INTERSTITIAL_DELAY_MINUTES * 60_000L
                }
                selectedTab = tab
            }
        }

        Scaffold(bottomBar = { BottomArea(playerState, selectedTab, { nowPlayingVm.togglePlayPause() }, { selectTab(Tab.NOW_PLAYING) }, selectTab) }) { padding ->
            Box(Modifier.padding(padding)) {
                when (selectedTab) {
                    Tab.STATIONS -> StationListScreen(stationListVm, onStationSelected = { selectTab(Tab.NOW_PLAYING) }, currentTheme = settings.theme, onThemeSelected = settingsVm::setTheme, showScrollHint = showStationScrollHint, onScrollHintShown = { showStationScrollHint = false })
                    Tab.NOW_PLAYING -> NowPlayingScreen(viewModel = nowPlayingVm)
                    Tab.FAVORITES -> FavoritesScreen(favoritesVm, onStationSelected = { selectTab(Tab.NOW_PLAYING) })
                    Tab.SETTINGS -> SettingsScreen(settingsVm, appName = "Malawi Radio")
                }
            }
        }
    }
}

@Composable
private fun BottomArea(playerState: com.malawi.radio.player.PlayerUiState, selectedTab: Tab, onTogglePlay: () -> Unit, onMiniClick: () -> Unit, onTab: (Tab) -> Unit) {
    Column {
        if (playerState.currentStation != null && selectedTab != Tab.NOW_PLAYING) MiniPlayerBar(playerState.currentStation!!.name, playerState.currentTitle, playerState.playbackState == PlaybackState.PLAYING, playerState.playbackState == PlaybackState.BUFFERING, onTogglePlay, onMiniClick)
        NavigationBar {
            listOf(Tab.STATIONS to Icons.Filled.List, Tab.NOW_PLAYING to Icons.Filled.Radio, Tab.FAVORITES to Icons.Filled.Favorite, Tab.SETTINGS to Icons.Filled.Settings).forEach { (tab, icon) ->
                NavigationBarItem(selected = selectedTab == tab, onClick = { onTab(tab) }, icon = { Icon(icon, contentDescription = tab.label) }, label = { Text(tab.label, textAlign = androidx.compose.ui.text.style.TextAlign.Center) })
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(stationName: String, currentTitle: String?, isPlaying: Boolean, isBuffering: Boolean, onTogglePlay: () -> Unit, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stationName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.42f))
        currentTitle?.takeIf { it.isNotBlank() }?.let { title ->
            Spacer(Modifier.width(12.dp))
            MiniPlayerSongTitle(title = title, modifier = Modifier.weight(0.58f))
            Spacer(Modifier.width(12.dp))
        }
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { if (isBuffering) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary) else IconButton(onClick = onTogglePlay) { Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play/Pause", tint = MaterialTheme.colorScheme.primary) } }
    }
}

@Composable
private fun MiniPlayerSongTitle(title: String, modifier: Modifier = Modifier) {
    MarqueeText(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Suppress("DEPRECATION")
private fun Context.currentVersionCode(): Long = runCatching {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
}.getOrDefault(0L)

private fun Context.shouldShowStationScrollHint(): Boolean {
    val prefs = getSharedPreferences(SCROLL_HINT_PREFS, Context.MODE_PRIVATE)
    val currentVersionCode = currentVersionCode()
    val savedVersionCode = prefs.getLong(SCROLL_HINT_VERSION_KEY, Long.MIN_VALUE)
    val openCount = if (savedVersionCode == currentVersionCode) prefs.getInt(SCROLL_HINT_OPEN_COUNT_KEY, 0) else 0
    val shouldShow = openCount < 2

    if (shouldShow) {
        prefs.edit()
            .putLong(SCROLL_HINT_VERSION_KEY, currentVersionCode)
            .putInt(SCROLL_HINT_OPEN_COUNT_KEY, openCount + 1)
            .apply()
    }

    return shouldShow
}

private fun Context.startRadioPlaybackForegroundService(intent: Intent) {
    try {
        ContextCompat.startForegroundService(this, intent)
    } catch (exception: IllegalStateException) {
        // Android 12+ can reject foreground-service starts after the app has already
        // moved to the background. If the service was started while the app was
        // visible, playback continues; otherwise avoid crashing the process.
    }
}
