package com.vapps.module_ads.control.helper.banner.params

import com.vapps.module_ads.control.helper.params.IAdsParam
import com.google.android.gms.ads.AdView

/**
 *Created by KO Huyn on 10/24/2023
 */
sealed class BannerAdParam: IAdsParam {
    data class Ready(val bannerAds: AdView) : BannerAdParam()
    object Request : BannerAdParam() {
        @JvmStatic
        fun create(): Request {
            return this
        }
    }

    data class Clickable(
        val minimumTimeKeepAdsDisplay: Long
    ) : BannerAdParam()
}