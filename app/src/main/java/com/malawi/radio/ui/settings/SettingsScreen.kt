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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.malawi.radio.data.settings.AppSettings
import com.malawi.radio.ui.stationlist.StationListViewModel
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.ui.ads.MediumRectangleAd
import com.malawi.radio.ui.theme.AppThemeOption

private const val SUPPORT_EMAIL = "appachi@ng4n.com"

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, stationListViewModel: StationListViewModel, appName: String) {
    val settings by viewModel.settings.collectAsState(initial = AppSettings())
    val stationState by stationListViewModel.uiState.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        HorizontalBannerAd(Modifier.padding(vertical = 8.dp))
        ManageStationsList(
            stations = stationState.allStations,
            hiddenStationIds = stationState.hiddenStationIds,
            onVisibilityChanged = stationListViewModel::setStationVisible
        )
        ThemeSelector(settings.theme, viewModel::setTheme)
        ListItem(
            headlineContent = { Text("Background Play", fontWeight = FontWeight.Bold) },
            supportingContent = { Text(if (settings.backgroundPlay) "On" else "Off") },
            trailingContent = { Switch(settings.backgroundPlay, viewModel::setBackgroundPlay) }
        )
        ExpandableSettingsCard("Contact Us") { ContactContent(appName) }
        ExpandableSettingsCard("Help / FAQs") { HelpContent(appName) }
        ExpandableSettingsCard("Advertize") { AdvertizeContent(appName) }
        ExpandableSettingsCard("Privacy Policy") { PrivacyPolicyContent(appName) }
        MediumRectangleAd(Modifier.padding(horizontal = 12.dp, vertical = 12.dp))
        Spacer(Modifier.height(48.dp))
    }
}


@Composable
private fun ManageStationsList(
    stations: List<com.malawi.radio.data.model.RadioStation>,
    hiddenStationIds: Set<String>,
    onVisibilityChanged: (String, Boolean) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Manage Stations List", fontWeight = FontWeight.Bold)
                    Text("Add / Remove Stations", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { expanded = !expanded }) { Text("Manage") }
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Uncheck a station to hide it from the list of Stations you see.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    stations.forEach { station ->
                        Row(Modifier.fillMaxWidth().clickable { onVisibilityChanged(station.id, station.id in hiddenStationIds) }.padding(vertical = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(checked = station.id !in hiddenStationIds, onCheckedChange = { checked -> onVisibilityChanged(station.id, checked) })
                            Spacer(Modifier.width(8.dp))
                            Text(station.name, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelector(selected: AppThemeOption, onSelected: (AppThemeOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
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
private fun ExpandableSettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ContactContent(appName: String) {
    Paragraph("For adverts, questions or feedback contact us by sending an email to $SUPPORT_EMAIL.")
    Paragraph("Make sure you mention $appName in your email message.")
}

@Composable
private fun HelpContent(appName: String) {
    SectionTitle("Quick Start")
    Bullet("Click any station in the stations list to connect to it and start playing.")
    Bullet("Tap the play button (▶︎) in the Player page to pause playing and tap it again to continue.")
    Bullet("Tap the heart icon (❤️) to add a station to your favorites.")
    Bullet("Change the color theme of the app from the Settings page or by tapping on the palette icon (🎨) on the top right of the Stations list page.")
    Bullet("Set a sleep timer to automatically turn off the radio after a specific time by clicking the moon icon (🌙) on the Player page. When a sleep timer is set, the moon icon changes to an hourglass (⌛). Tap the hourglass for options to cancel or extend the timer.")
    Bullet("Background play enables the radio to keep playing even if you leave the app or when the phone screen is turned off. You can turn this option on or off from the Settings page.")

    Spacer(Modifier.height(6.dp))
    SectionTitle("Troubleshooting")
    BulletWithBoldLead("No station playing", "If none of the stations in the list are playing, make sure your network is connected and internet is active, and check whether other apps are connected. You may also try restarting the app or your device. Make sure also that you have the latest version of the app. If the problem persists for up to a week, kindly email $SUPPORT_EMAIL to notify us. Remember to mention $appName in your email message.")
    BulletWithBoldLead("A single station not playing", "If the station was previously playing and now has stopped while other stations are still playing, try connecting again later or restarting the app. If the problem persists for up to a month it means the station may be out of commission, kindly email $SUPPORT_EMAIL to notify us. Remember to mention $appName in your email message.")
    BulletWithBoldLead("Request a new station", "If a station you want is not already in the stations list, we may be able to add it in the next version of the app. Contact us via email at $SUPPORT_EMAIL to make a request for the new station. Remember to mention $appName in your email message.")
    BulletWithBoldLead("Ads sound too loud", "You can turn off the sound in an advert by tapping the loudspeaker icon in any of the corners of the advert.")
    BulletWithBoldLead("Is it possible to turn off ads?", "Unfortunately at this time there is no way to turn off the adverts in the app because that is how we get the funding to continue providing the app to you for free. In the future we may consider adding an option to remove ads but it would have to be a paid option with a small monthly/yearly subscription. If you would like such an option kindly email $SUPPORT_EMAIL to indicate your interest and let us know how much you would be willing to pay for such a service. If we get enough interest we will implement the feature. Remember to mention $appName in your email message.")
}

@Composable
private fun AdvertizeContent(appName: String) {
    Paragraph("To place your advert, contact us by sending an email to $SUPPORT_EMAIL.")
    Paragraph("Kindly make sure you mention $appName in your email message.")
}

@Composable
private fun PrivacyPolicyContent(appName: String) {
    Paragraph("This Privacy Policy explains how $appName handles information when you use the app. By using $appName, you agree to the practices described below.")
    PolicySection("Information we collect", "The app is designed for radio streaming and does not require you to create an account. We may store your app preferences on your device, including favorite stations, selected theme, background-play preference, and playback-related settings. These preferences help the app work consistently for you.")
    PolicySection("Radio streaming", "When you play a station, the audio stream is provided by the station or its streaming provider. Those providers may receive standard technical information such as your IP address, device type, app or browser user agent, playback request time, and connection diagnostics needed to deliver the stream.")
    PolicySection("Advertizing", "$appName is supported by adverts. Advertizing partners, including Google AdMob where enabled, may collect or receive device identifiers, approximate location, ad interaction data, and other technical information to deliver, limit, measure, and improve adverts. These partners may use cookies, mobile advertizing identifiers, or similar technologies subject to their own privacy policies and your device settings.")
    PolicySection("Device permissions", "The app uses network access to load station lists, play audio streams, and show adverts. If background playback is enabled, the app may continue audio playback while the app is not in the foreground or while your screen is off. The app does not request access to your contacts, photos, microphone, camera, or precise GPS location for normal radio playback.")
    PolicySection("How we use information", "Information handled by the app is used to provide radio playback, remember your settings, maintain favorites, troubleshoot technical issues, improve reliability, prevent abuse, and support the free version of the app through advertizing.")
    PolicySection("Data stored on your device", "Favorites and settings are stored locally on your device. You can change these settings inside the app, clear app storage from your device settings, or uninstall the app to remove locally stored app data.")
    PolicySection("Information you send to us", "If you contact us by email, we will receive the information you choose to include, such as your email address, message content, device details, station requests, advert enquiries, and any screenshots or logs you attach. We use this information to respond to you, provide support, investigate problems, and manage advert requests.")
    PolicySection("Sharing of information", "We do not sell personal information that you email to us. We may share information when required to operate the app, comply with legal obligations, protect users or the app, respond to support requests, or work with service providers such as advertizing, analytics, hosting, and radio streaming partners.")
    PolicySection("Children's privacy", "$appName is intended for a general audience. We do not knowingly request personal information from children. If you believe a child has sent personal information to us, contact us and we will take reasonable steps to delete it.")
    PolicySection("Your choices", "You can manage advertizing identifiers and ad personalization through your device settings where supported. You can disable background play in Settings, remove favorites in the app, clear local app data, or uninstall the app at any time.")
    PolicySection("Security and retention", "We use reasonable safeguards appropriate for a radio streaming app, but no app or internet transmission can be guaranteed to be completely secure. Email messages and support records may be retained for as long as needed to respond, maintain records, resolve disputes, and comply with legal obligations.")
    PolicySection("Changes to this policy", "We may update this Privacy Policy when app features, advertizing tools, legal requirements, or operational practices change. Continued use of the app after an update means you accept the updated policy.")
    PolicySection("Contact", "If you have questions about this Privacy Policy or how $appName handles information, email $SUPPORT_EMAIL and mention $appName in your message.")
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun Paragraph(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun Bullet(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BulletWithBoldLead(lead: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", style = MaterialTheme.typography.bodyMedium)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(lead) }
                append(": $text")
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionTitle(title)
        Paragraph(body)
    }
}
