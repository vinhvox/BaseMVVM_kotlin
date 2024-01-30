package com.vapps.module_ads.control.event

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.applovin.mediation.MaxAd
import com.google.android.gms.ads.AdValue
import com.vapps.module_ads.control.config.NetworkProvider
import com.vapps.module_ads.control.utils.AdType
import com.vapps.module_ads.control.utils.AppUtil.currentTotalRevenue001Ad
import com.vapps.module_ads.control.utils.SharePreferenceUtils.getCurrentTotalRevenueAd
import com.vapps.module_ads.control.utils.SharePreferenceUtils.getInstallTime
import com.vapps.module_ads.control.utils.SharePreferenceUtils.isPushRevenue3Day
import com.vapps.module_ads.control.utils.SharePreferenceUtils.isPushRevenue7Day
import com.vapps.module_ads.control.utils.SharePreferenceUtils.setPushedRevenue3Day
import com.vapps.module_ads.control.utils.SharePreferenceUtils.setPushedRevenue7Day
import com.vapps.module_ads.control.utils.SharePreferenceUtils.updateCurrentTotalRevenue001Ad
import com.vapps.module_ads.control.utils.SharePreferenceUtils.updateCurrentTotalRevenueAd

object VioLogEventManager {
    private val TAG = VioLogEventManager::class.simpleName
    fun logPaidAdImpression(
        context: Context,
        adValue: AdValue,
        adUnitId: String,
        mediationAdapterClassName: String,
        adType: AdType?
    ) {
        logEventWithAds(
            context,
            adValue.valueMicros.toFloat(),
            adValue.precisionType,
            adUnitId,
            mediationAdapterClassName,
            NetworkProvider.ADMOB
        )
        AperoAdjust.pushTrackEventAdmob(adValue)
    }

    fun logPaidAdImpression(context: Context, adValue: MaxAd, adType: AdType?) {
        logEventWithAds(
            context,
            adValue.revenue.toFloat(),
            0,
            adValue.adUnitId,
            adValue.networkName,
            NetworkProvider.MAX
        )
        AperoAdjust.pushTrackEventApplovin(adValue, context)
    }

    private fun logEventWithAds(
        context: Context,
        revenue: Float,
        precision: Int,
        adUnitId: String,
        network: String,
        mediationProvider: Int
    ) {
        Log.d(
            TAG, String.format(
                "Paid event of value %.0f microcents in currency USD of precision %s%n occurred for ad unit %s from ad network %s.mediation provider: %s%n",
                revenue,
                precision,
                adUnitId,
                network, mediationProvider
            )
        )
        val params = Bundle() // Log ad value in micros.
        params.putDouble("valuemicros", revenue.toDouble())
        params.putString("currency", "USD")
        // These values below won’t be used in ROAS recipe.
        // But log for purposes of debugging and future reference.
        params.putInt("precision", precision)
        params.putString("adunitid", adUnitId)
        params.putString("network", network)

        // log revenue this ad
        logPaidAdImpressionValue(
            context,
            revenue / 1000000.0,
            precision,
            adUnitId,
            network,
            mediationProvider
        )
        FirebaseAnalyticsUtil.logEventWithAds(context, params)
        FacebookEventUtils.logEventWithAds(context, params)
        // update current tota
        // l revenue ads
        updateCurrentTotalRevenueAd(context, revenue)
        logCurrentTotalRevenueAd(context, "event_current_total_revenue_ad")

        // update current total revenue ads for event paid_ad_impression_value_0.01
        currentTotalRevenue001Ad = currentTotalRevenue001Ad + revenue
        updateCurrentTotalRevenue001Ad(context, currentTotalRevenue001Ad)
        logTotalRevenue001Ad(context)
        logTotalRevenueAdIn3DaysIfNeed(context)
        logTotalRevenueAdIn7DaysIfNeed(context)
    }

    private fun logPaidAdImpressionValue(
        context: Context,
        value: Double,
        precision: Int,
        adunitid: String,
        network: String,
        mediationProvider: Int
    ) {
        val params = Bundle()
        params.putDouble("value", value)
        params.putString("currency", "USD")
        params.putInt("precision", precision)
        params.putString("adunitid", adunitid)
        params.putString("network", network)
        AperoAdjust.logPaidAdImpressionValue(value, "USD")
        FirebaseAnalyticsUtil.logPaidAdImpressionValue(context, params, mediationProvider)
        FacebookEventUtils.logPaidAdImpressionValue(context, params, mediationProvider)
    }

    fun logClickAdsEvent(context: Context?, adUnitId: String?) {
        Log.d(
            TAG, String.format(
                "User click ad for ad unit %s.",
                adUnitId
            )
        )
        val bundle = Bundle()
        bundle.putString("ad_unit_id", adUnitId)
        FirebaseAnalyticsUtil.logClickAdsEvent(context, bundle)
        FacebookEventUtils.logClickAdsEvent(context, bundle)
    }

    fun logCurrentTotalRevenueAd(context: Context?, eventName: String?) {
        val currentTotalRevenue = getCurrentTotalRevenueAd(context!!)
        val bundle = Bundle()
        bundle.putFloat("value", currentTotalRevenue)
        FirebaseAnalyticsUtil.logCurrentTotalRevenueAd(context, eventName, bundle)
        FacebookEventUtils.logCurrentTotalRevenueAd(context, eventName, bundle)
    }

    fun logTotalRevenue001Ad(context: Context?) {
        val revenue = currentTotalRevenue001Ad
        if (revenue / 1000000 >= 0.01) {
            currentTotalRevenue001Ad = 0F
            updateCurrentTotalRevenue001Ad(context!!, 0f)
            val bundle = Bundle()
            bundle.putFloat("value", revenue / 1000000)
            FirebaseAnalyticsUtil.logTotalRevenue001Ad(context, bundle)
            FacebookEventUtils.logTotalRevenue001Ad(context, bundle)
        }
    }

    fun logTotalRevenueAdIn3DaysIfNeed(context: Context?) {
        val installTime = getInstallTime(context!!)
        if (!isPushRevenue3Day(context) && System.currentTimeMillis() - installTime >= 3L * 24 * 60 * 60 * 1000) {
            Log.d(TAG, "logTotalRevenueAdAt3DaysIfNeed: ")
            logCurrentTotalRevenueAd(context, "event_total_revenue_ad_in_3_days")
            setPushedRevenue3Day(context)
        }
    }

    fun logTotalRevenueAdIn7DaysIfNeed(context: Context?) {
        val installTime = getInstallTime(context!!)
        if (!isPushRevenue7Day(context) && System.currentTimeMillis() - installTime >= 7L * 24 * 60 * 60 * 1000) {
            Log.d(TAG, "logTotalRevenueAdAt7DaysIfNeed: ")
            logCurrentTotalRevenueAd(context, "event_total_revenue_ad_in_7_days")
            setPushedRevenue7Day(context)
        }
    }

    fun setEventNamePurchaseAdjust(eventNamePurchase: String?) {
        eventNamePurchase?.let { AperoAdjust.setEventNamePurchase(it) }
    }

    fun trackAdRevenue(id: String?) {
        AperoAdjust.trackAdRevenue(id)
    }

    fun onTrackEvent(eventName: String?) {
        AperoAdjust.onTrackEvent(eventName)
    }

    fun onTrackEvent(eventName: String?, id: String?) {
        AperoAdjust.onTrackEvent(eventName, id)
    }

    fun onTrackRevenue(eventName: String?, revenue: Float, currency: String?) {
        AperoAdjust.onTrackRevenue(eventName, revenue, currency)
    }

    fun onTrackRevenuePurchase(
        revenue: Float,
        currency: String?,
        idPurchase: String?,
        typeIAP: Int
    ) {
        AperoAdjust.onTrackRevenuePurchase(revenue, currency)
    }

    fun pushTrackEventAdmob(adValue: AdValue?) {
        if (adValue != null) {
            AperoAdjust.pushTrackEventAdmob(adValue)
        }
    }

    fun pushTrackEventApplovin(ad: MaxAd?, context: Context?) {
        if (ad != null) {
            AperoAdjust.pushTrackEventApplovin(ad, context)
        }
    }
}