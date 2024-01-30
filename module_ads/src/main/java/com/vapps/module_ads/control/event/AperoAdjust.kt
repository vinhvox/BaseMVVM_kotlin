package com.vapps.module_ads.control.event

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.applovin.mediation.MaxAd
import com.google.android.gms.ads.AdValue

object AperoAdjust {
    private var eventNamePurchase = ""
    fun setEventNamePurchase(eventNamePurchase: String) {
        AperoAdjust.eventNamePurchase = eventNamePurchase
    }

    fun trackAdRevenue(id: String?) {
        val adjustAdRevenue = AdjustAdRevenue(id)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }

    fun onTrackEvent(eventName: String?) {
        val event = AdjustEvent(eventName)
        Adjust.trackEvent(event)
    }

    fun onTrackEvent(eventName: String?, id: String?) {
        val event = AdjustEvent(eventName)
        // Assign custom identifier to event which will be reported in success/failure callbacks.
        event.setCallbackId(id)
        Adjust.trackEvent(event)
    }

    fun onTrackRevenue(eventName: String?, revenue: Float, currency: String?) {
        val event = AdjustEvent(eventName)
        // Add revenue 1 cent of an euro.
        event.setRevenue(revenue.toDouble(), currency)
        Adjust.trackEvent(event)
    }

    fun onTrackRevenuePurchase(revenue: Float, currency: String?) {
        onTrackRevenue(eventNamePurchase, revenue, currency)
    }

    fun pushTrackEventAdmob(adValue: AdValue) {
        val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
        adRevenue.setRevenue(adValue.valueMicros / 1000000.0, adValue.currencyCode)
        Adjust.trackAdRevenue(adRevenue)
    }

    fun pushTrackEventApplovin(ad: MaxAd, context: Context?) {
        val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
        adjustAdRevenue.setRevenue(ad.revenue, "USD")
        adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
        adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
        adjustAdRevenue.setAdRevenuePlacement(ad.placement)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }

    fun logPaidAdImpressionValue(revenue: Double, currency: String?) {
        /*val event = AdjustEvent(VioAd.instance?.adConfig?.adjustConfig!!.eventAdImpression)
        event.setRevenue(revenue, currency)
        Adjust.trackEvent(event)*/
    }
}