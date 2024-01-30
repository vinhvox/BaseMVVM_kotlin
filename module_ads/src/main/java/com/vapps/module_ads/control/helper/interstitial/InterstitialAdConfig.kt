package com.vapps.module_ads.control.helper.interstitial

import com.vapps.module_ads.control.helper.IAdsConfig

class InterstitialAdConfig (
    override val idAds: String,
    val showByTime: Int = 1,
    override val canShowAds: Boolean,
    override val canReloadAds: Boolean
): IAdsConfig