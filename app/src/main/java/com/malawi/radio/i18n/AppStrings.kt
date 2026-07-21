package com.malawi.radio.i18n

data class Strings(
    val stations: String = "Stations",
    val player: String = "Player",
    val favorites: String = "Favorites",
    val settings: String = "Settings",
    val tapBackAgainToExit: String = "Tap back again to exit",
    val playPause: String = "Play/Pause",
    val noFavorites: String = "No favorites yet. Tap the heart on a station to save it here.",
    val changeTheme: String = "Change theme",
    val favorite: String = "Favorite",
    val slideUpForMoreStations: String = "Slide up for more stations",
    val buffering: String = "Buffering…",
    val onAir: String = "On air",
    val paused: String = "Paused",
    val playbackError: String = "Playback error",
    val sleepTimer: String = "Sleep Timer",
    val remaining: String = "%02d:%02d remaining",
    val add5Minutes: String = "+ Add 5 minutes",
    val cancelTimer: String = "Cancel Timer",
    val minutes5: String = "5 minutes",
    val minutes10: String = "10 minutes",
    val minutes15: String = "15 minutes",
    val minutes30: String = "30 minutes",
    val hour1: String = "1 hour",
    val hours2: String = "2 hours",
    val tapStationStart: String = "Tap a station to start listening",
    val goToStations: String = "Go to Stations",
    val backgroundPlay: String = "Background Play",
    val on: String = "On",
    val off: String = "Off",
    val language: String = "Language",
    val theme: String = "Theme",
    val change: String = "Change",
    val contactUs: String = "Contact Us",
    val helpFaqs: String = "Help / FAQs",
    val advertize: String = "Advertize",
    val privacyPolicy: String = "Privacy Policy",
    val quickStart: String = "Quick Start",
    val troubleshooting: String = "Troubleshooting",
    val contactText: String = "For adverts, questions or feedback contact us by sending an email to %s.",
    val mentionAppText: String = "Make sure you mention %s in your email message.",
    val advertText: String = "To place your advert, contact us by sending an email to %s.",
    val advertMentionText: String = "Kindly make sure you mention %s in your email message.",
    val quickStartBullets: List<String> = listOf(
        "Click any station in the stations list to connect to it and start playing.",
        "Tap the play button (▶︎) in the Player page to pause playing and tap it again to continue.",
        "Tap the heart icon (❤️) to add a station to your favorites.",
        "Change the color theme of the app from the Settings page or by tapping on the palette icon (🎨) on the top right of the Stations list page.",
        "Set a sleep timer to automatically turn off the radio after a specific time by clicking the moon icon (🌙) on the Player page. When a sleep timer is set, the moon icon changes to an hourglass (⌛). Tap the hourglass for options to cancel or extend the timer.",
        "Background play enables the radio to keep playing even if you leave the app or when the phone screen is turned off. You can turn this option on or off from the Settings page."
    ),
    val troubleshootingItems: List<Pair<String, String>> = listOf(
        "No station playing" to "If none of the stations in the list are playing, make sure your network is connected and internet is active, and check whether other apps are connected. You may also try restarting the app or your device. Make sure also that you have the latest version of the app. If the problem persists for up to a week, kindly email %s to notify us. Remember to mention %s in your email message.",
        "A single station not playing" to "If the station was previously playing and now has stopped while other stations are still playing, try connecting again later or restarting the app. If the problem persists for up to a month it means the station may be out of commission, kindly email %s to notify us. Remember to mention %s in your email message.",
        "Request a new station" to "If a station you want is not already in the stations list, we may be able to add it in the next version of the app. Contact us via email at %s to make a request for the new station. Remember to mention %s in your email message.",
        "Ads sound too loud" to "You can turn off the sound in an advert by tapping the loudspeaker icon in any of the corners of the advert.",
        "Is it possible to turn off ads?" to "Unfortunately at this time there is no way to turn off the adverts in the app because that is how we get the funding to continue providing the app to you for free. In the future we may consider adding an option to remove ads but it would have to be a paid option with a small monthly/yearly subscription. If you would like such an option kindly email %s to indicate your interest and let us know how much you would be willing to pay for such a service. If we get enough interest we will implement the feature. Remember to mention %s in your email message."
    ),
    val privacyIntro: String = "This Privacy Policy explains how %s handles information when you use the app. By using %s, you agree to the practices described below.",
    val policySections: List<Pair<String, String>> = listOf(
        "Information we collect" to "The app is designed for radio streaming and does not require you to create an account. We may store your app preferences on your device, including favorite stations, selected theme, background-play preference, language preference, and playback-related settings. These preferences help the app work consistently for you.",
        "Radio streaming" to "When you play a station, the audio stream is provided by the station or its streaming provider. Those providers may receive standard technical information such as your IP address, device type, app or browser user agent, playback request time, and connection diagnostics needed to deliver the stream.",
        "Advertising" to "%s is supported by adverts. Advertising partners, including Google AdMob where enabled, may collect or receive device identifiers, approximate location, ad interaction data, and other technical information to deliver, limit, measure, and improve adverts. These partners may use cookies, mobile advertising identifiers, or similar technologies subject to their own privacy policies and your device settings.",
        "Device permissions" to "The app uses network access to load station lists, play audio streams, and show adverts. If background playback is enabled, the app may continue audio playback while the app is not in the foreground or while your screen is off. The app does not request access to your contacts, photos, microphone, camera, or precise GPS location for normal radio playback.",
        "Contact" to "If you have questions about this Privacy Policy or how %s handles information, email %s and mention %s in your message."
    )
)

object I18n {
    private val english = Strings()
    private val translated = mapOf(
        AppLanguage.SPANISH to english.copy(settings = "Configuración", language = "Idioma", backgroundPlay = "Reproducción en segundo plano", on = "Activado", off = "Desactivado", change = "Cambiar"),
        AppLanguage.FRENCH to english.copy(settings = "Paramètres", language = "Langue", backgroundPlay = "Lecture en arrière-plan", on = "Activé", off = "Désactivé", change = "Changer"),
        AppLanguage.ARABIC to english.copy(settings = "الإعدادات", language = "اللغة", backgroundPlay = "التشغيل في الخلفية", on = "تشغيل", off = "إيقاف", change = "تغيير"),
        AppLanguage.ITALIAN to english.copy(settings = "Impostazioni", language = "Lingua", backgroundPlay = "Riproduzione in background", on = "Attivo", off = "Disattivo", change = "Cambia"),
        AppLanguage.GERMAN to english.copy(settings = "Einstellungen", language = "Sprache", backgroundPlay = "Hintergrundwiedergabe", on = "Ein", off = "Aus", change = "Ändern"),
        AppLanguage.PORTUGUESE to english.copy(settings = "Definições", language = "Idioma", backgroundPlay = "Reprodução em segundo plano", on = "Ligado", off = "Desligado", change = "Alterar"),
        AppLanguage.HINDI to english.copy(settings = "सेटिंग्स", language = "भाषा", backgroundPlay = "बैकग्राउंड प्ले", on = "चालू", off = "बंद", change = "बदलें"),
        AppLanguage.MANDARIN to english.copy(settings = "设置", language = "语言", backgroundPlay = "后台播放", on = "开", off = "关", change = "更改"),
        AppLanguage.KOREAN to english.copy(settings = "설정", language = "언어", backgroundPlay = "백그라운드 재생", on = "켜짐", off = "꺼짐", change = "변경"),
        AppLanguage.RUSSIAN to english.copy(settings = "Настройки", language = "Язык", backgroundPlay = "Фоновое воспроизведение", on = "Вкл", off = "Выкл", change = "Изменить"),
        AppLanguage.VIETNAMESE to english.copy(settings = "Cài đặt", language = "Ngôn ngữ", backgroundPlay = "Phát nền", on = "Bật", off = "Tắt", change = "Đổi"),
        AppLanguage.DUTCH to english.copy(settings = "Instellingen", language = "Taal", backgroundPlay = "Afspelen op achtergrond", on = "Aan", off = "Uit", change = "Wijzigen"),
        AppLanguage.SWEDISH to english.copy(settings = "Inställningar", language = "Språk", backgroundPlay = "Bakgrundsuppspelning", on = "På", off = "Av", change = "Ändra"),
        AppLanguage.NORWEGIAN to english.copy(settings = "Innstillinger", language = "Språk", backgroundPlay = "Bakgrunnsavspilling", on = "På", off = "Av", change = "Endre"),
        AppLanguage.AFRIKAANS to english.copy(settings = "Instellings", language = "Taal", backgroundPlay = "Agtergrond speel", on = "Aan", off = "Af", change = "Verander"),
        AppLanguage.ISIZULU to english.copy(settings = "Izilungiselelo", language = "Ulimi", backgroundPlay = "Ukudlala ngemuva", on = "Kuvuliwe", off = "Kuvaliwe", change = "Shintsha"),
        AppLanguage.KISWAHILI to english.copy(settings = "Mipangilio", language = "Lugha", backgroundPlay = "Cheza chinichini", on = "Washa", off = "Zima", change = "Badilisha"),
        AppLanguage.HAUSA to english.copy(settings = "Saituna", language = "Harshe", backgroundPlay = "Kunna a baya", on = "A kunne", off = "A kashe", change = "Canja"),
        AppLanguage.IGBO to english.copy(settings = "Ntọala", language = "Asụsụ", backgroundPlay = "Kpọọ n'azụ", on = "Gbanyere", off = "Gbanyụrụ", change = "Gbanwee"),
        AppLanguage.YORUBA to english.copy(settings = "Ètò", language = "Èdè", backgroundPlay = "Ṣiṣẹ́ lẹ́yìn", on = "Tan", off = "Paa", change = "Yí padà"),
        AppLanguage.TAGALOG to english.copy(settings = "Mga Setting", language = "Wika", backgroundPlay = "Background Play", on = "On", off = "Off", change = "Palitan"),
        AppLanguage.TWI to english.copy(settings = "Nhyehyɛe", language = "Kasa", backgroundPlay = "Di wɔ akyi", on = "Dum no", off = "Ɛnyɛ adwuma", change = "Sesa")
    )
    fun strings(language: AppLanguage): Strings = translated[language] ?: english
}
