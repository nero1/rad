package com.malawi.radio.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.malawi.radio.ui.theme.AppThemeOption

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, appName: String) {
    val settings by viewModel.settings.collectAsState(initial = com.malawi.radio.data.settings.AppSettings())
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ThemeSelector(settings.theme, viewModel::setTheme)
        ListItem(headlineContent = { Text("Background play") }, supportingContent = { Text("Keep playing when the screen turns off or you leave the app.") }, trailingContent = { Switch(settings.backgroundPlay, viewModel::setBackgroundPlay) })
        SettingsCard("About", "$appName is a template radio streaming app built with Kotlin, Jetpack Compose, Media3 and AdMob-ready placements.")
        SettingsCard("Help / FAQs", "Choose a station to play. Favorite stations with the heart. Use the sleep timer on Now Playing to stop playback automatically.")
        SettingsCard("Contact", "For adverts, questions or feedback contact us by sending an email appachiapps@ng4n.com. Make sure you mention $appName in your email message.")
        SettingsCard("Privacy Policy", "This template stores favorites and settings on your device. Audio streams and ads may be served by third parties according to their policies.")
    }
}

@Composable
fun ThemeSelector(selected: AppThemeOption, onSelected: (AppThemeOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(headlineContent = { Text("Theme") }, supportingContent = { Text(selected.label) }, trailingContent = { Button(onClick = { expanded = true }) { Text("Change") } })
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        AppThemeOption.entries.forEach { theme -> DropdownMenuItem(text = { Text(theme.label) }, onClick = { onSelected(theme); expanded = false }) }
    }
}

@Composable
private fun SettingsCard(title: String, body: String) { ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(title, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text(body) } } }
