package com.vapps.module_ads.control.config

import android.app.Application


class VioAdConfig private constructor(
    val application: Application? = null,
    val isVariantProduce: Boolean = false,
    val provider: Int = NetworkProvider.ADMOB,
    val adjustConfig: AdjustConfig? = null,
    val listDevices: List<String> = arrayListOf(),
    val disableAdsResumeByAd: Boolean = true
) {

    class Builder(
        var application: Application? = null,
        private var isBuildVariantProduce: Boolean = false,
        private var mediationProvider: Int = NetworkProvider.ADMOB,
        private var adjustConfig: AdjustConfig? = null,
        private var listTestDevices: List<String> = arrayListOf(),
        private var disableAdsResumeByAd: Boolean = true
    ) {
        fun application(application: Application) = apply { this.application = application }

        fun buildVariantProduce(variantProduce: Boolean) =
            apply { this.isBuildVariantProduce = variantProduce }

        fun mediationProvider(mediation: Int) = apply { this.mediationProvider = mediation }

        fun adjustConfig(config: AdjustConfig) = apply { this.adjustConfig = config }

        fun listTestDevices(listDevices: List<String>) =
            apply { this.listTestDevices = listDevices }

        fun disableAdsResumeByAd(isDisabled: Boolean) =
            apply { this.disableAdsResumeByAd = isDisabled }

        fun build() =
            VioAdConfig(
                application,
                isBuildVariantProduce,
                mediationProvider,
                adjustConfig,
                listTestDevices,
                disableAdsResumeByAd
            )
    }
}
