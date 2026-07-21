package com.malawi.radio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malawi.radio.data.settings.AppSettingsStore
import com.malawi.radio.i18n.AppLanguage
import com.malawi.radio.ui.theme.AppThemeOption
import kotlinx.coroutines.launch

class SettingsViewModel(private val store: AppSettingsStore) : ViewModel() {
    val settings = store.settings
    fun setTheme(theme: AppThemeOption) = viewModelScope.launch { store.setTheme(theme) }
    fun setBackgroundPlay(enabled: Boolean) = viewModelScope.launch { store.setBackgroundPlay(enabled) }
    fun setLanguage(language: AppLanguage) = viewModelScope.launch { store.setLanguage(language) }
}
