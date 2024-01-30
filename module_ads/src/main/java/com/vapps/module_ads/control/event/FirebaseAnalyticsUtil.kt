package com.vapps.module_ads.control.event

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.vapps.module_ads.control.config.NetworkProvider

object FirebaseAnalyticsUtil {
    private const val TAG = "FirebaseAnalyticsUtil"
    private var fireBaseAnalytics: FirebaseAnalytics? = null
    fun init(context: Context?) {
        fireBaseAnalytics = FirebaseAnalytics.getInstance(context!!)
    }

    /*  public static void logPaidAdImpression(Context context, AdValue adValue, String adUnitId, String mediationAdapterClassName) {
        logEventWithAds(context, (float) adValue.getValueMicros(), adValue.getPrecisionType(), adUnitId, mediationAdapterClassName);
    }

    public static void logPaidAdImpression(Context context, MaxAd adValue) {
        logEventWithAds(context, (float) adValue.getRevenue(), 0, adValue.getAdUnitId(), adValue.getNetworkName());
    }
*/
    @JvmStatic
    fun logEventWithAds(context: Context?, params: Bundle?) {
        FirebaseAnalytics.getInstance(context!!).logEvent("paid_ad_impression", params)
    }

    @JvmStatic
    fun logPaidAdImpressionValue(context: Context?, bundle: Bundle?, mediationProvider: Int) {
        if (mediationProvider == NetworkProvider.MAX) FirebaseAnalytics.getInstance(context!!)
            .logEvent("max_paid_ad_impression_value", bundle) else FirebaseAnalytics.getInstance(
            context!!
        ).logEvent("paid_ad_impression_value", bundle)
    }

    @JvmStatic
    fun logClickAdsEvent(context: Context?, bundle: Bundle?) {
        FirebaseAnalytics.getInstance(context!!).logEvent("event_user_click_ads", bundle)
    }

    @JvmStatic
    fun logCurrentTotalRevenueAd(context: Context?, eventName: String?, bundle: Bundle?) {
        FirebaseAnalytics.getInstance(context!!).logEvent(eventName!!, bundle)
    }

    @JvmStatic
    fun logTotalRevenue001Ad(context: Context?, bundle: Bundle?) {
        FirebaseAnalytics.getInstance(context!!).logEvent("paid_ad_impression_value_001", bundle)
    }

    fun logConfirmPurchaseGoogle(
        orderId: String?,
        purchaseId: String?,
        purchaseToken: String
    ) {
        val tokenPart1: String
        val tokenPart2: String
        if (purchaseToken.length > 100) {
            tokenPart1 = purchaseToken.substring(0, 100)
            tokenPart2 = purchaseToken.substring(100)
        } else {
            tokenPart1 = purchaseToken
            tokenPart2 = "EMPTY"
        }
        val bundle = Bundle()
        bundle.putString("purchase_order_id", orderId)
        bundle.putString("purchase_package_id", purchaseId)
        bundle.putString("purchase_token_part_1", tokenPart1)
        bundle.putString("purchase_token_part_2", tokenPart2)
        fireBaseAnalytics!!.logEvent("confirm_purchased_with_google", bundle)
        Log.d(TAG, "logConfirmPurchaseGoogle: tracked")
    }

    fun logRevenuePurchase(value: Double) {
        val bundle = Bundle()
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, value)
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
        fireBaseAnalytics!!.logEvent("user_purchased_value", bundle)
    }
}