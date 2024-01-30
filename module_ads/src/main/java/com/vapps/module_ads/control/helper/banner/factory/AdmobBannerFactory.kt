package com.vapps.module_ads.control.helper.banner.factory

import android.content.Context
import com.vapps.module_ads.control.listener.AdCallback

interface AdmobBannerFactory {
    fun requestBannerAd(context: Context, adId: String, adCallback: AdCallback)

    companion object {
        @JvmStatic
        fun getInstance(): AdmobBannerFactory = AdmobBannerFactoryImpl()
    }
}