package com.malawi.radio.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.malawi.radio.BuildConfig

enum class AppThemeOption(val label: String) {
    DARK_MODE("Dark Mode"), LIGHT_MODE("Light Mode"), HIGH_CONTRAST("High Contrast"), ROSE_WIND("Rose Wind"), MOONLIGHT("Moonlight"),
    PURPLE_HIBISCUS("Hibiscus"), BLUE_SKIES("Blue Skies"),
    DESERT_HEAT("Desert Heat"), SNOW("Snow"), GREEN_GLORY("Green Glory");

    companion object {
        fun fromConfigValue(value: String): AppThemeOption = runCatching {
            val normalized = if (value.equals("midnight", ignoreCase = true)) {
                "HIGH_CONTRAST"
            } else {
                value.trim().uppercase().replace('-', '_')
            }
            valueOf(normalized)
        }.getOrDefault(DARK_MODE)
    }
}

val DefaultAppThemeOption: AppThemeOption = AppThemeOption.fromConfigValue(BuildConfig.DEFAULT_THEME)

fun AppThemeOption.scheme(): ColorScheme = when (this) {
    AppThemeOption.DARK_MODE -> darkColorScheme(primary = Color(0xFFFFD100), onPrimary = Color.Black, secondary = Color(0xFF009543), tertiary = Color(0xFFCE1126), background = Color(0xFF0D0D0D), surface = Color(0xFF1A1A1A), surfaceVariant = Color(0xFF232323), onBackground = Color(0xFFF5F5F5), onSurface = Color(0xFFF5F5F5), onSurfaceVariant = Color(0xFFA8A8A8))
    AppThemeOption.LIGHT_MODE -> lightColorScheme(primary = Color(0xFF795900), secondary = Color(0xFF276B3A), tertiary = Color(0xFFB3261E), background = Color(0xFFFFFBFE), surface = Color.White, surfaceVariant = Color(0xFFF1E7D0))
    AppThemeOption.HIGH_CONTRAST -> darkColorScheme(primary = Color(0xFFFFFF00), onPrimary = Color.Black, secondary = Color(0xFF00FFFF), onSecondary = Color.Black, tertiary = Color(0xFFFF66FF), onTertiary = Color.Black, background = Color.Black, onBackground = Color.White, surface = Color(0xFF000000), onSurface = Color.White, surfaceVariant = Color(0xFF1A1A1A), onSurfaceVariant = Color.White, error = Color(0xFFFF4D4D), onError = Color.Black)
    AppThemeOption.ROSE_WIND -> lightColorScheme(primary = Color(0xFFC2185B), secondary = Color(0xFF8E4968), tertiary = Color(0xFFFF8A80), background = Color(0xFFFFF7FA), surfaceVariant = Color(0xFFF7DDE8))
    AppThemeOption.MOONLIGHT -> darkColorScheme(primary = Color(0xFFB7C9FF), secondary = Color(0xFF83A2D9), tertiary = Color(0xFFE4C1F9), background = Color(0xFF101624), surface = Color(0xFF182033), surfaceVariant = Color(0xFF222B40))
    AppThemeOption.PURPLE_HIBISCUS -> darkColorScheme(primary = Color(0xFFD0BCFF), secondary = Color(0xFFE1BEE7), tertiary = Color(0xFFFFB1C8), background = Color(0xFF1D102A), surface = Color(0xFF2B183D), surfaceVariant = Color(0xFF3A2250))
    AppThemeOption.BLUE_SKIES -> lightColorScheme(primary = Color(0xFF0277BD), secondary = Color(0xFF00A6A6), tertiary = Color(0xFFFFB703), background = Color(0xFFEAF7FF), surfaceVariant = Color(0xFFD4F0FF))
    AppThemeOption.DESERT_HEAT -> lightColorScheme(primary = Color(0xFFC2410C), secondary = Color(0xFFB7791F), tertiary = Color(0xFF7C2D12), background = Color(0xFFFFF4E6), surfaceVariant = Color(0xFFFAD7A0))
    AppThemeOption.SNOW -> lightColorScheme(primary = Color(0xFF3B82F6), secondary = Color(0xFF64748B), tertiary = Color(0xFF94A3B8), background = Color(0xFFF8FAFC), surfaceVariant = Color(0xFFE2E8F0))
    AppThemeOption.GREEN_GLORY -> lightColorScheme(primary = Color(0xFF047857), secondary = Color(0xFF65A30D), tertiary = Color(0xFFFACC15), background = Color(0xFFF0FDF4), surfaceVariant = Color(0xFFD9F99D))
}
