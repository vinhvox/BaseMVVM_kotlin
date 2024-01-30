package com.vapps.module_ads.control.helper.appoppen.factory

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.appopen.AppOpenAd
import com.vapps.module_ads.control.listener.AdCallback

interface AdmobAppOpenFactory {
    fun requestAppOpenAd(context: Context, adId: String, adCallback: AdCallback)
    fun showAppOpenAd(activity: Activity, appOpenAd: AppOpenAd, adCallback: AdCallback)

    companion object {
        @JvmStatic
        fun getInstance(): AdmobAppOpenFactory = AdmobAppOpenFactoryImpl()
    }
}