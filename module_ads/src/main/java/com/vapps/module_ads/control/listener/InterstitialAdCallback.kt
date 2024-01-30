package com.vapps.module_ads.control.listener

import com.vapps.module_ads.control.data.ContentAd


interface InterstitialAdCallback : AperoAdCallback<ContentAd.AdmobAd.ApInterstitialAd> {
    fun onNextAction()
    fun onAdClose()
    fun onInterstitialShow()
}