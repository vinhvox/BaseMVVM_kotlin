package com.vapps.module_ads.control.helper.adnative

import androidx.annotation.LayoutRes
import com.vapps.module_ads.control.helper.IAdsConfig

/**
 * Created by KO Huyn on 09/10/2023.
 */
class NativeAdConfig(
    override val idAds: String,
    override val canShowAds: Boolean,
    override val canReloadAds: Boolean,
    @LayoutRes val layoutId: Int,
) : IAdsConfig
