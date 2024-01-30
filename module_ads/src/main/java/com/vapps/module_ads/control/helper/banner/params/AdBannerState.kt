package com.vapps.module_ads.control.helper.banner.params

import com.google.android.gms.ads.AdView

/**
 *Created by KO Huyn on 10/24/2023
 */
sealed class AdBannerState {
    object None : AdBannerState()
    object Fail : AdBannerState()
    object Loading : AdBannerState()
    object Cancel : AdBannerState()
    data class Loaded(val adBanner: AdView) : AdBannerState()
}