package com.vapps.module_ads.control.event

import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.vapps.module_ads.control.config.NetworkProvider

object FacebookEventUtils {
    @JvmStatic
    fun logEventWithAds(context: Context?, params: Bundle?) {
        AppEventsLogger.newLogger(context!!).logEvent("paid_ad_impression", params)
    }

    @JvmStatic
    fun logPaidAdImpressionValue(context: Context?, bundle: Bundle?, mediationProvider: Int) {
        if (mediationProvider == NetworkProvider.MAX)
            AppEventsLogger.newLogger(context!!).logEvent("max_paid_ad_impression_value", bundle)
        else
            AppEventsLogger.newLogger(context!!).logEvent("paid_ad_impression_value", bundle)
    }

    @JvmStatic
    fun logClickAdsEvent(context: Context?, bundle: Bundle?) {
        AppEventsLogger.newLogger(context!!).logEvent("event_user_click_ads", bundle)
    }

    @JvmStatic
    fun logCurrentTotalRevenueAd(context: Context?, eventName: String?, bundle: Bundle?) {
        AppEventsLogger.newLogger(context!!).logEvent(eventName, bundle)
    }

    @JvmStatic
    fun logTotalRevenue001Ad(context: Context?, bundle: Bundle?) {
        AppEventsLogger.newLogger(context!!).logEvent("paid_ad_impression_value_001", bundle)
    }
}