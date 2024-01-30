package com.vapps.module_ads.control.helper.appoppen.factory

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.vapps.module_ads.control.listener.AdCallback

class AdmobAppOpenFactoryImpl : AdmobAppOpenFactory {
    override fun requestAppOpenAd(context: Context, adId: String, adCallback: AdCallback) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            adId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                /**
                 * Called when an app open ad has loaded.
                 *
                 * @param ad the loaded app open ad.
                 */
                override fun onAdLoaded(ad: AppOpenAd) {
                    adCallback.onAppOpenAdLoaded(ad)
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adCallback.onAdFailedToLoad(loadAdError)
                }
            }
        )
    }

    override fun showAppOpenAd(activity: Activity, appOpenAd: AppOpenAd, adCallback: AdCallback) {
        appOpenAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    adCallback.onNextAction()
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    adCallback.onAdFailedToShow(adError)
                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {
                    adCallback.onAppOpenAdShow()
                }
            }
        appOpenAd.show(activity)
    }
}