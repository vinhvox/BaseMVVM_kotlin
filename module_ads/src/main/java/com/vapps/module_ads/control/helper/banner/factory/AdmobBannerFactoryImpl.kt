package com.vapps.module_ads.control.helper.banner.factory

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.vapps.module_ads.control.admob.BannerInlineStyle
import com.vapps.module_ads.control.admob.getAdRequest
import com.vapps.module_ads.control.admob.getAdSize
import com.vapps.module_ads.control.billing.AppPurchase
import com.vapps.module_ads.control.event.VioLogEventManager
import com.vapps.module_ads.control.listener.AdCallback
import com.vapps.module_ads.control.utils.AdType

class AdmobBannerFactoryImpl : AdmobBannerFactory {
    override fun requestBannerAd(context: Context, adId: String, adCallback: AdCallback) {
        if (AppPurchase.instance?.isPurchased == true) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    1999,
                    "App isPurchased",
                    "",
                    null,
                    null
                )
            )
            return
        }
        try {
            val adView = AdView(context)
            adView.adUnitId = adId
            val adSize = getAdSize(context as Activity, false, BannerInlineStyle.LARGE_STYLE)
            adView.setAdSize(adSize)
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adCallback.onAdFailedToLoad(loadAdError)
                }

                override fun onAdLoaded() {
                    adCallback.onBannerLoaded(adView)
                    adView.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        VioLogEventManager.logPaidAdImpression(
                            context,
                            adValue,
                            adView.adUnitId,
                            adView.responseInfo!!
                                .mediationAdapterClassName!!, AdType.BANNER
                        )
                    }
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback.onAdClicked()
                    VioLogEventManager.logClickAdsEvent(context, adId)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback.onAdImpression()
                }
            }
            adView.loadAd(getAdRequest())
        } catch (ex: Exception) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    1991,
                    ex.message.toString(),
                    "",
                    null,
                    null
                )
            )
        }
    }
}