package com.vapps.module_ads.control.helper.appoppen

import com.vapps.module_ads.control.helper.IAdsConfig

class AppResumeAdConfig(
    override val idAds: String,
    val listClassInValid: MutableList<Class<*>> = arrayListOf(),
    override val canShowAds: Boolean = false,
    override val canReloadAds: Boolean = false
) : IAdsConfig