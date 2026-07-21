package com.malawi.radio.i18n

import com.malawi.radio.BuildConfig

enum class AppLanguage(val code: String, val displayName: String) {
    AFRIKAANS("af", "Afrikaans"),
    ARABIC("ar", "Arabic"),
    DUTCH("nl", "Dutch"),
    ENGLISH("en", "English"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    HAUSA("ha", "Hausa"),
    HINDI("hi", "Hindi"),
    IGBO("ig", "Igbo"),
    ISIZULU("zu", "IsiZulu"),
    ITALIAN("it", "Italian"),
    KISWAHILI("sw", "Kiswahili"),
    KOREAN("ko", "Korean"),
    MANDARIN("zh", "Mandarin"),
    NORWEGIAN("no", "Norwegian"),
    PORTUGUESE("pt", "Portuguese"),
    RUSSIAN("ru", "Russian"),
    SPANISH("es", "Spanish"),
    SWEDISH("sv", "Swedish"),
    TAGALOG("tl", "Tagalog"),
    TWI("tw", "Twi"),
    VIETNAMESE("vi", "Vietnamese"),
    YORUBA("yo", "Yoruba");

    companion object {
        val selectorOptions: List<AppLanguage> = entries.sortedBy { it.displayName }

        fun from(value: String?): AppLanguage {
            val normalized = value?.trim().orEmpty()
            return entries.firstOrNull { it.name.equals(normalized, true) || it.code.equals(normalized, true) }
                ?: entries.firstOrNull { it.name.equals(BuildConfig.DEFAULT_LANGUAGE, true) || it.code.equals(BuildConfig.DEFAULT_LANGUAGE, true) }
                ?: ENGLISH
        }
    }
}
