plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}


fun xmanifestValue(key: String): String? {
    val manifestFile = rootProject.file("xmanifest.md")
    if (!manifestFile.isFile) return null
    val pattern = Regex("""- `\Q$key\E`:\s*(.+)""")
    return manifestFile.readLines()
        .firstNotNullOfOrNull { line -> pattern.find(line)?.groupValues?.get(1)?.trim() }
}

fun xmanifestBoolean(key: String, defaultValue: Boolean): Boolean =
    xmanifestValue(key)?.lowercase()?.let { value ->
        when (value) {
            "true", "yes", "on", "enabled", "1" -> true
            "false", "no", "off", "disabled", "0" -> false
            else -> defaultValue
        }
    } ?: defaultValue

fun quotedBuildConfigString(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.malawi.radio"
    compileSdk = 36

    defaultConfig {
        val configuredAppName = System.getenv("APP_NAME") ?: xmanifestValue("app_name") ?: "Malawi Radio"
        val configuredApplicationId = System.getenv("APPLICATION_ID") ?: xmanifestValue("package_name") ?: "com.malawi.radio"
        val configuredAdMobAppId = System.getenv("ADMOB_APP_ID") ?: xmanifestValue("admob_app_id") ?: "ca-app-pub-3940256099942544~3347511713"
        val configuredBannerAdId = System.getenv("ADMOB_BANNER_ID") ?: xmanifestValue("admob_banner_id") ?: "ca-app-pub-3940256099942544/6300978111"
        val configuredInterstitialAdId = System.getenv("ADMOB_INTERSTITIAL_ID") ?: xmanifestValue("admob_interstitial_id") ?: "ca-app-pub-3940256099942544/1033173712"
        val configuredInterstitialDelay = System.getenv("INTERSTITIAL_DELAY_MINS") ?: xmanifestValue("interstitial_delay_mins") ?: "7"

        applicationId = configuredApplicationId
        minSdk = 24
        targetSdk = (System.getenv("ANDROID_TARGET_API") ?: xmanifestValue("android_target_api") ?: "36").toInt()
        versionCode = (System.getenv("VERSION_CODE") ?: "100").toInt()
        versionName = System.getenv("VERSION_NAME") ?: xmanifestValue("version_name_start") ?: "1.00"

        resValue("string", "app_name", configuredAppName)
        manifestPlaceholders["admobAppId"] = configuredAdMobAppId
        buildConfigField("String", "APP_NAME", quotedBuildConfigString(configuredAppName))
        buildConfigField("String", "DEFAULT_THEME", quotedBuildConfigString(xmanifestValue("default_theme") ?: "dark_mode"))
        buildConfigField("String", "DEFAULT_LANGUAGE", quotedBuildConfigString(xmanifestValue("default_language") ?: "en"))
        buildConfigField("String", "ADMOB_BANNER_ID", quotedBuildConfigString(configuredBannerAdId))
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", quotedBuildConfigString(configuredInterstitialAdId))
        buildConfigField("Long", "INTERSTITIAL_DELAY_MINUTES", "${configuredInterstitialDelay.toLong()}L")
        buildConfigField("Boolean", "BACKGROUND_PLAY_DEFAULT", xmanifestBoolean("background_play_default", true).toString())
        buildConfigField("Boolean", "SCROLLING_MARQUEE_ENABLED", xmanifestBoolean("scrolling_marquee_enabled", false).toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Compose (BOM manages versions)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Media3 / ExoPlayer for streaming playback + media session + notification
    implementation("androidx.media3:media3-exoplayer:1.4.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.0")
    implementation("androidx.media3:media3-session:1.4.0")
    implementation("androidx.media3:media3-common:1.4.0")

    // Room for favorites / local persistence
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore for lightweight prefs (last played station, volume)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // JSON parsing for bundled stations.json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Coil for station logos
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Google Mobile Ads (AdMob)
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
