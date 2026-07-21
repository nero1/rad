package com.malawi.radio.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.malawi.radio.BuildConfig
import com.malawi.radio.i18n.AppLanguage
import com.malawi.radio.ui.theme.AppThemeOption
import com.malawi.radio.ui.theme.DefaultAppThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("app_settings")

data class AppSettings(
    val theme: AppThemeOption = DefaultAppThemeOption,
    val backgroundPlay: Boolean = BuildConfig.BACKGROUND_PLAY_DEFAULT,
    val language: AppLanguage = AppLanguage.from(BuildConfig.DEFAULT_LANGUAGE)
)

class AppSettingsStore(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    private val backgroundPlayKey = booleanPreferencesKey("background_play")
    private val languageKey = stringPreferencesKey("language")

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            theme = runCatching { AppThemeOption.valueOf(prefs[themeKey] ?: DefaultAppThemeOption.name) }.getOrDefault(DefaultAppThemeOption),
            backgroundPlay = prefs[backgroundPlayKey] ?: BuildConfig.BACKGROUND_PLAY_DEFAULT,
            language = AppLanguage.from(prefs[languageKey])
        )
    }

    suspend fun setTheme(theme: AppThemeOption) = context.settingsDataStore.edit { it[themeKey] = theme.name }
    suspend fun setBackgroundPlay(enabled: Boolean) = context.settingsDataStore.edit { it[backgroundPlayKey] = enabled }
    suspend fun setLanguage(language: AppLanguage) = context.settingsDataStore.edit { it[languageKey] = language.name }
}
