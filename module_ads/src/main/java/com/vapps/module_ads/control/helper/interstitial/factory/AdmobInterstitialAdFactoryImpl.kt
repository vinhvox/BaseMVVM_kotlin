package com.vapps.module_ads.control.helper.interstitial.factory

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.vapps.module_ads.control.admob.getAdRequest
import com.vapps.module_ads.control.billing.AppPurchase
import com.vapps.module_ads.control.config.ErrorCode
import com.vapps.module_ads.control.data.ContentAd
import com.vapps.module_ads.control.listener.InterstitialAdCallback

class AdmobInterstitialAdFactoryImpl : AdmobInterstitialAdFactory {
    override fun requestInterstitialAd(context: Context, adId: String, adCallback: InterstitialAdCallback) {
        if (AppPurchase.instance?.isPurchased == true) {
            adCallback.onAdFailedToLoad(LoadAdError(ErrorCode.PURCHASED, "", "", null, null))
            return
        }
        InterstitialAd.load(
            context,
            adId,
            getAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adCallback.onAdFailedToLoad(adError)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    adCallback.onAdLoaded(ContentAd.AdmobAd.ApInterstitialAd(ad))
                }
            }
        )
    }

    override fun showInterstitial(
        context: Context,
        interstitialAd: InterstitialAd?,
        adCallback: InterstitialAdCallback
    ) {
        if (AppPurchase.instance?.isPurchased == true || interstitialAd == null) {
            adCallback.onNextAction()
            return
        }

        interstitialAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    adCallback.onAdClose()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    adCallback.onAdFailedToShow(adError)
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is dismissed.
                    adCallback.onInterstitialShow()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback.onAdImpression()
                }
            }
        interstitialAd.show(context as Activity)
    }
}