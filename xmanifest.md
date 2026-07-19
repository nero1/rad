# App Template Manifest

Edit this file first when cloning the app for another country, market, or niche. Android build-time values can be supplied with environment variables (see `.env.example`); runtime content such as stations still lives in `app/src/main/assets/stations.json`.

## Identity
- `app_name`: Malawi Radio
- `package_name`: com.malawi.radio
- `version_name_start`: 1.01
- `version_increment`: 0.01 per Play Store build
- `android_target_api`: 36

## Theme
- `default_theme`: dark_mode
- Available themes: `dark_mode`, `light_mode`, `rose_wind`, `moonlight`, `purple_hibiscus`, `midnight`, `blue_skies`, `desert_heat`, `snow`, `green_glory`.

## Ads
- `admob_app_id`: ca-app-pub-3940256099942544~3347511713
- `admob_banner_id`: ca-app-pub-3940256099942544/6300978111
- `admob_interstitial_id`: ca-app-pub-3940256099942544/1033173712
- `interstitial_delay_mins`: 2

Production IDs should be set with environment variables before release. Missing environment values fall back to the test IDs above.

## Icons
- `app_icon_big`: icons/512.png
- `app_icon_small`: icons/128.png

Recommended source files are square PNG images: 512×512 px for Play/App Store style artwork and 128×128 px for quick template previews. Android launcher icons are generated from `app/src/main/res/mipmap-*`/`drawable` resources; if you do not replace them, the current default icons are used.

## Playback defaults
- `background_play_default`: true
- `sleep_timer_options`: 5 minutes, 10 minutes, 15 minutes, 30 minutes, 1 hour, 2 hours
