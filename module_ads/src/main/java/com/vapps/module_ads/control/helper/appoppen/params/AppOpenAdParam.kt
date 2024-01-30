package com.vapps.module_ads.control.helper.appoppen.params

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.vapps.module_ads.control.helper.params.IAdsParam

open class AppOpenAdParam : IAdsParam{
    data object Show : AppOpenAdParam()
    data object Request : AppOpenAdParam() {
        @JvmStatic
        fun create(): Request {
            return this
        }
    }

    data class Clickable(
        val minimumTimeKeepAdsDisplay: Long
    ) : AppOpenAdParam()
}