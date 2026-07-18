package com.malawi.radio.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.malawi.radio.ui.theme.AppThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("app_settings")

data class AppSettings(val theme: AppThemeOption = AppThemeOption.DARK_MODE, val backgroundPlay: Boolean = true)

class AppSettingsStore(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    private val backgroundPlayKey = booleanPreferencesKey("background_play")

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            theme = runCatching { AppThemeOption.valueOf(prefs[themeKey] ?: AppThemeOption.DARK_MODE.name) }.getOrDefault(AppThemeOption.DARK_MODE),
            backgroundPlay = prefs[backgroundPlayKey] ?: true
        )
    }

    suspend fun setTheme(theme: AppThemeOption) = context.settingsDataStore.edit { it[themeKey] = theme.name }
    suspend fun setBackgroundPlay(enabled: Boolean) = context.settingsDataStore.edit { it[backgroundPlayKey] = enabled }
}
