package com.malawi.radio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MalawiRadioTheme(
    themeOption: AppThemeOption = AppThemeOption.DARK_MODE,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = themeOption.scheme(),
        typography = Typography,
        content = content
    )
}
