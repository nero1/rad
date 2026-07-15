package com.malawi.radio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MalawiRadioColorScheme = darkColorScheme(
    primary = MalawiSun,
    onPrimary = MalawiBlack,
    secondary = MalawiGreen,
    onSecondary = TextPrimary,
    tertiary = MalawiRed,
    background = MalawiBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    error = MalawiRed
)

@Composable
fun MalawiRadioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // App is intentionally dark-first (fits a radio/night-listening use case,
    // and echoes the flag's black background) regardless of system theme.
    MaterialTheme(
        colorScheme = MalawiRadioColorScheme,
        typography = Typography,
        content = content
    )
}
