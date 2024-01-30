package com.vapps.module_ads.control.listener

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

open class AdCallback {
    open fun onNextAction() {}
    open fun onAdClosed() {}
    open fun onAdFailedToLoad(i: LoadAdError?) {}
    open fun onAdFailedToShow(adError: AdError?) {}
    open fun onAdLeftApplication() {}
    open fun onAdLoaded() {}
    open fun onAdSplashReady() {}
    open fun onInterstitialLoad(interstitialAd: InterstitialAd?) {}
    open fun onAdClicked() {}
    open fun onAdImpression() {}
    open fun onRewardAdLoaded(rewardedAd: RewardedAd?) {}
    open fun onRewardAdLoaded(rewardedAd: RewardedInterstitialAd?) {}
    open fun onUnifiedNativeAdLoaded(unifiedNativeAd: NativeAd) {}
    open fun onInterstitialShow() {}
    open fun onBannerLoaded(adView: AdView?) {}
    open fun onAppOpenAdShow() {}
    open fun onAppOpenAdLoaded(appOpenAd: AppOpenAd) {}
}