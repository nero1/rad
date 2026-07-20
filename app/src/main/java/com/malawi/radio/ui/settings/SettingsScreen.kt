package com.malawi.radio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.malawi.radio.data.settings.AppSettings
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.ui.ads.MediumRectangleAd
import com.malawi.radio.ui.theme.AppThemeOption

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, appName: String) {
    val settings by viewModel.settings.collectAsState(initial = AppSettings())
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        HorizontalBannerAd(Modifier.padding(vertical = 8.dp))
        ThemeSelector(settings.theme, viewModel::setTheme)
        ListItem(
            headlineContent = { Text("Background Play", fontWeight = FontWeight.Bold) },
            supportingContent = { Text("Keep playing when the screen turns off or you leave the app.") },
            trailingContent = { Switch(settings.backgroundPlay, viewModel::setBackgroundPlay) }
        )
        ExpandableSettingsCard("About", "$appName is a template radio streaming app built with Kotlin, Jetpack Compose, Media3 and AdMob-ready placements.")
        ExpandableSettingsCard("Help / FAQs", "Choose a station to play. Favorite stations with the heart. Use the sleep timer on Now Playing to stop playback automatically.")
        ExpandableSettingsCard("Privacy Policy", "This template stores favorites and settings on your device. Audio streams and ads may be served by third parties according to their policies.")
        ExpandableSettingsCard("Contact", "For adverts, questions or feedback contact us by sending an email appachiapps@ng4n.com. Make sure you mention $appName in your email message.")
        MediumRectangleAd(Modifier.padding(horizontal = 12.dp, vertical = 12.dp))
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
fun ThemeSelector(selected: AppThemeOption, onSelected: (AppThemeOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
        ListItem(
            headlineContent = { Text("Theme", fontWeight = FontWeight.Bold) },
            supportingContent = { Text(selected.label) },
            trailingContent = { Button(onClick = { expanded = true }) { Text("Change") } }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppThemeOption.entries.forEach { theme ->
                DropdownMenuItem(text = { Text(theme.label) }, onClick = { onSelected(theme); expanded = false })
            }
        }
    }
}

@Composable
private fun ExpandableSettingsCard(title: String, body: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    Text(body)
                }
            }
        }
    }
}
