package com.vapps.module_ads.control.helper.interstitial.params

import com.vapps.module_ads.control.helper.params.IAdsParam
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd

/**
 *Created by KO Huyn on 10/24/2023
 */
sealed class InterstitialAdParam: IAdsParam {
    data class Show(val interstitialAd: InterstitialAd) : InterstitialAdParam()
    data object ShowAd : InterstitialAdParam()
    data object Request : InterstitialAdParam() {
        @JvmStatic
        fun create(): Request {
            return this
        }
    }

    data class Clickable(
        val minimumTimeKeepAdsDisplay: Long
    ) : InterstitialAdParam()
}