package com.malawi.radio.ui.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

const val DEFAULT_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
const val DEFAULT_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
const val INTERSTITIAL_DELAY_MINUTES = 10L

@Composable
fun HorizontalBannerAd(modifier: Modifier = Modifier, adUnitId: String = System.getenv("ADMOB_BANNER_ID") ?: DEFAULT_BANNER_AD_UNIT_ID) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxWidth().height(50.dp),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun MediumRectangleAd(modifier: Modifier = Modifier, adUnitId: String = System.getenv("ADMOB_BANNER_ID") ?: DEFAULT_BANNER_AD_UNIT_ID) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxWidth().height(250.dp),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.MEDIUM_RECTANGLE)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
