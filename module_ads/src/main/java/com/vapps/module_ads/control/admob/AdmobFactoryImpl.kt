package com.vapps.module_ads.control.admob

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.IntDef
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.vapps.module_ads.control.config.VioAdConfig
import com.vapps.module_ads.control.helper.adnative.factory.AdmobNativeFactory
import com.vapps.module_ads.control.helper.appoppen.factory.AdmobAppOpenFactory
import com.vapps.module_ads.control.helper.banner.factory.AdmobBannerFactory
import com.vapps.module_ads.control.helper.interstitial.factory.AdmobInterstitialAdFactory
import com.vapps.module_ads.control.listener.AdCallback
import com.vapps.module_ads.control.listener.InterstitialAdCallback
import com.vapps.module_ads.control.listener.NativeAdCallback

class AdmobFactoryImpl : AdmobFactory {
    private lateinit var vioAdConfig: VioAdConfig
    override fun initAdmob(
        context: Context,
        adConfig: VioAdConfig
    ) {
        this.vioAdConfig = adConfig
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = Application.getProcessName()
            val packageName = context.packageName
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
        MobileAds.initialize(context) { initializationStatus: InitializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d(
                    TAG, String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status!!.description, status.latency
                    )
                )
            }
        }
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(adConfig.listDevices).build()
        )
    }

    override fun requestBannerAd(context: Context, adId: String, adCallback: AdCallback) {
        AdmobBannerFactory.getInstance().requestBannerAd(context, adId, adCallback)
    }

    override fun requestNativeAd(
        context: Context,
        adId: String,
        adCallback: NativeAdCallback
    ) {
        AdmobNativeFactory.getInstance().requestNativeAd(context, adId, adCallback)
    }

    override fun populateNativeAdView(
        context: Context,
        nativeAd: NativeAd,
        nativeAdViewId: Int,
        adPlaceHolder: FrameLayout,
        containerShimmerLoading: ShimmerFrameLayout?,
        adCallback: NativeAdCallback
    ) {
        AdmobNativeFactory.getInstance().populateNativeAdView(
            context,
            nativeAd,
            nativeAdViewId,
            adPlaceHolder,
            containerShimmerLoading,
            adCallback
        )
    }

    override fun requestInterstitialAds(context: Context, adId: String, adCallback: InterstitialAdCallback) {
        AdmobInterstitialAdFactory.getInstance().requestInterstitialAd(context, adId, adCallback)
    }

    override fun showInterstitial(
        context: Context,
        interstitialAd: InterstitialAd?,
        adCallback: InterstitialAdCallback
    ) {
        AdmobInterstitialAdFactory.getInstance()
            .showInterstitial(context, interstitialAd, adCallback)
    }

    override fun requestAppOpenAds(context: Context, adId: String, adCallback: AdCallback) {
        AdmobAppOpenFactory.getInstance().requestAppOpenAd(context, adId, adCallback)
    }

    override fun showAppOpenAds(activity: Activity, appOpenAd: AppOpenAd, adCallback: AdCallback) {
        AdmobAppOpenFactory.getInstance().showAppOpenAd(activity, appOpenAd, adCallback)
    }

    companion object {
        private val TAG = AdmobFactoryImpl::class.simpleName
    }
}

@IntDef(BannerInlineStyle.SMALL_STYLE, BannerInlineStyle.LARGE_STYLE)
annotation class BannerInlineStyle {
    companion object {
        const val SMALL_STYLE = 0
        const val LARGE_STYLE = 1
    }
}