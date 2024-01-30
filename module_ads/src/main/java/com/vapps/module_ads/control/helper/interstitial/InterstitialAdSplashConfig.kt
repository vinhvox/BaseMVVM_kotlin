package com.vapps.module_ads.control.helper.interstitial

import com.vapps.module_ads.control.helper.IAdsConfig

class InterstitialAdSplashConfig(
    override val idAds: String,
    val timeOut: Long,
    val timeDelay: Long,
    val showReady: Boolean = false,
    override val canShowAds: Boolean,
    override val canReloadAds: Boolean
) : IAdsConfig