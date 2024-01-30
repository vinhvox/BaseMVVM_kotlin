package com.vapps.module_ads.control.config

/**
 * Created by Vio on 26/11/2023.
 */
class AdjustConfig private constructor(
    val adjustConfigToken: String,
    val configEventNamePurchase: String = "",
    val configEventAdImpression: String = ""
) {
    class Builder(private var adjustToken: String) {
        private var eventNamePurchase: String = ""
        private var eventAdImpression: String = ""

        fun eventNamePurchase(eventName: String) = apply { this.eventNamePurchase = eventName }

        fun eventAdImpression(eventAd: String) = apply { this.eventAdImpression = eventAd }

        fun build() = AdjustConfig(adjustToken, eventNamePurchase, eventAdImpression)
    }
}