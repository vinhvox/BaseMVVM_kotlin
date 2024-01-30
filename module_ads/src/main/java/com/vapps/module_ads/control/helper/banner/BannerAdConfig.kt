package com.vapps.module_ads.control.helper.banner

import com.vapps.module_ads.control.helper.IAdsConfig

/**
 * Created by KO Huyn on 09/10/2023.
 */
data class BannerAdConfig(
    override val idAds: String,
    override val canShowAds: Boolean,
    override val canReloadAds: Boolean,
) : IAdsConfig