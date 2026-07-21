package com.malawi.radio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
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
import com.malawi.radio.i18n.AppLanguage
import com.malawi.radio.i18n.I18n
import com.malawi.radio.i18n.Strings
import com.malawi.radio.ui.ads.HorizontalBannerAd
import com.malawi.radio.ui.ads.MediumRectangleAd
import com.malawi.radio.ui.theme.AppThemeOption

private const val SUPPORT_EMAIL = "appachi@ng4n.com"

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, appName: String) {
    val settings by viewModel.settings.collectAsState(initial = AppSettings())
    val strings = I18n.strings(settings.language)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(strings.settings, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        HorizontalBannerAd(Modifier.padding(vertical = 8.dp))
        ThemeSelector(settings.theme, viewModel::setTheme, strings)
        ListItem(
            headlineContent = { Text(strings.backgroundPlay, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(if (settings.backgroundPlay) strings.on else strings.off) },
            trailingContent = { Switch(settings.backgroundPlay, viewModel::setBackgroundPlay) }
        )
        LanguageSelector(settings.language, viewModel::setLanguage, strings)
        ExpandableSettingsCard(strings.contactUs) { ContactContent(appName, strings) }
        ExpandableSettingsCard(strings.helpFaqs) { HelpContent(appName, strings) }
        ExpandableSettingsCard(strings.advertize) { AdvertizeContent(appName, strings) }
        ExpandableSettingsCard(strings.privacyPolicy) { PrivacyPolicyContent(appName, strings) }
        MediumRectangleAd(Modifier.padding(horizontal = 12.dp, vertical = 12.dp))
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
fun ThemeSelector(selected: AppThemeOption, onSelected: (AppThemeOption) -> Unit, strings: Strings) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        ListItem(
            headlineContent = { Text(strings.theme, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(selected.label) },
            trailingContent = { Button(onClick = { expanded = true }) { Text(strings.change) } }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppThemeOption.entries.forEach { theme ->
                DropdownMenuItem(text = { Text(theme.label) }, onClick = { onSelected(theme); expanded = false })
            }
        }
    }
}

@Composable
fun LanguageSelector(selected: AppLanguage, onSelected: (AppLanguage) -> Unit, strings: Strings) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        ListItem(
            headlineContent = { Text(strings.language, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(selected.displayName) },
            trailingContent = { Button(onClick = { expanded = true }) { Text(strings.change) } }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                AppLanguage.selectorOptions.forEach { language ->
                    DropdownMenuItem(text = { Text(language.displayName) }, onClick = { onSelected(language); expanded = false })
                }
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
private fun ContactContent(appName: String, strings: Strings) {
    Paragraph(strings.contactText.format(SUPPORT_EMAIL))
    Paragraph(strings.mentionAppText.format(appName))
}

@Composable
private fun HelpContent(appName: String, strings: Strings) {
    SectionTitle(strings.quickStart)
    strings.quickStartBullets.forEach { Bullet(it) }

    Spacer(Modifier.height(6.dp))
    SectionTitle(strings.troubleshooting)
    strings.troubleshootingItems.forEach { (lead, text) ->
        BulletWithBoldLead(lead, text.format(SUPPORT_EMAIL, appName))
    }
}

@Composable
private fun AdvertizeContent(appName: String, strings: Strings) {
    Paragraph(strings.advertText.format(SUPPORT_EMAIL))
    Paragraph(strings.advertMentionText.format(appName))
}

@Composable
private fun PrivacyPolicyContent(appName: String, strings: Strings) {
    Paragraph(strings.privacyIntro.format(appName, appName))
    strings.policySections.forEach { (title, body) ->
        PolicySection(title, body.format(appName, SUPPORT_EMAIL, appName))
    }
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
