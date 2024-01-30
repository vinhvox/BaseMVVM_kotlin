package com.vapps.module_ads.control.listener

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.vapps.module_ads.control.data.ContentAd

interface AperoAdCallback<T : ContentAd> {
    fun onAdLoaded(data: T)
    fun onAdFailedToLoad(loadAdError: LoadAdError)
    fun onAdClicked()
    fun onAdImpression()
    fun onAdFailedToShow(adError: AdError)
}