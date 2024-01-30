package com.vapps.module_ads.control.helper.interstitial.factory

import android.content.Context
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.vapps.module_ads.control.listener.AdCallback
import com.vapps.module_ads.control.listener.InterstitialAdCallback

interface AdmobInterstitialAdFactory {
    fun requestInterstitialAd(context: Context, adId: String, adCallback: InterstitialAdCallback)
    fun showInterstitial(context: Context, interstitialAd: InterstitialAd?, adCallback: InterstitialAdCallback)

    companion object {
        @JvmStatic
        fun getInstance(): AdmobInterstitialAdFactory = AdmobInterstitialAdFactoryImpl()
    }
}