package com.vapps.module_ads.control.helper.appoppen

import com.vapps.module_ads.control.helper.IAdsConfig

class AppOpenAdConfig(
    override val idAds: String,
    val timeOut: Long,
    val timeDelay: Long,
    val showReady: Boolean = false,
    override val canShowAds: Boolean = false,
    override val canReloadAds: Boolean = false
) : IAdsConfig