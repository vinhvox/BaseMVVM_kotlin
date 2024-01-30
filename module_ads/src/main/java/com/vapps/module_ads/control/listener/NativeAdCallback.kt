package com.vapps.module_ads.control.listener

import com.vapps.module_ads.control.data.ContentAd

interface NativeAdCallback : AperoAdCallback<ContentAd.AdmobAd.ApNativeAd> {
    fun populateNativeAd()
}
