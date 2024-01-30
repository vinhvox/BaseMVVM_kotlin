package com.vapps.module_ads.control.helper.banner

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.vapps.module_ads.R
import com.vapps.module_ads.control.admob.AdmobFactory
import com.vapps.module_ads.control.helper.AdsHelper
import com.vapps.module_ads.control.helper.banner.params.AdBannerState
import com.vapps.module_ads.control.helper.banner.params.BannerAdParam
import com.vapps.module_ads.control.listener.AdCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 *Created by KO Huyn on 10/24/2023
 */
class BannerAdHelper(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val config: BannerAdConfig,
) : AdsHelper<BannerAdConfig, BannerAdParam>(activity, lifecycleOwner, config) {

    private val adBannerState: MutableStateFlow<AdBannerState> =
        MutableStateFlow(if (canRequestAds()) AdBannerState.None else AdBannerState.Fail)
    private var timeShowAdImpression: Long = 0
    private val listAdCallback: CopyOnWriteArrayList<AdCallback> = CopyOnWriteArrayList()
    private val resumeCount: AtomicInteger = AtomicInteger(0)
    private var shimmerLayoutView: ShimmerFrameLayout? = null
    private var bannerContentView: FrameLayout? = null
    var bannerAdView: AdView? = null
        private set

    init {
        registerAdListener(getDefaultCallback())
        lifecycleEventState.onEach {
            if (it == Lifecycle.Event.ON_CREATE) {
                if (!canRequestAds()) {
                    bannerContentView?.isVisible = false
                    shimmerLayoutView?.isVisible = false
                }
            }

            if (it == Lifecycle.Event.ON_RESUME) {
                if (!canShowAds() && isActiveState()) {
                    cancel()
                }
            }
        }.launchIn(lifecycleOwner.lifecycleScope)
        //Request when resume
        lifecycleEventState.debounce(300).onEach { event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeCount.incrementAndGet()
                logZ("Resume repeat ${resumeCount.get()} times")
                if (!isActiveState()) {
                    logInterruptExecute("Request when resume")
                }
            }
            if (event == Lifecycle.Event.ON_RESUME && resumeCount.get() > 1 && bannerAdView != null && canRequestAds() && canReloadAd() && isActiveState()) {
                logZ("requestAds on resume")
                requestAds(BannerAdParam.Request)
            }
        }.launchIn(lifecycleOwner.lifecycleScope)
        //for action resume or init
        adBannerState
            .onEach { logZ("adBannerState(${it::class.java.simpleName})") }
            .launchIn(lifecycleOwner.lifecycleScope)
        adBannerState.onEach { adsParam ->
            handleShowAds(adsParam)
        }.launchIn(lifecycleOwner.lifecycleScope)
    }

    fun getBannerState(): Flow<AdBannerState> {
        return adBannerState.asStateFlow()
    }

    private fun handleShowAds(adsParam: AdBannerState) {
        bannerContentView?.isGone = adsParam is AdBannerState.Cancel || !canShowAds()
        shimmerLayoutView?.isVisible = adsParam is AdBannerState.Loading
        when (adsParam) {
            is AdBannerState.Loaded -> {
                val bannerContentView = bannerContentView
                val shimmerLayoutView = shimmerLayoutView
                if (bannerContentView != null && shimmerLayoutView != null) {
                    bannerContentView.setBackgroundColor(Color.WHITE)
                    val view = View(bannerContentView.context)
                    val oldHeight = bannerContentView.height
                    bannerContentView.let {
                        it.removeAllViews()
                        it.addView(view, 0, oldHeight)
                        it.addView(adsParam.adBanner)
                    }
                }
            }

            else -> Unit
        }
    }

    override fun requestAds(param: BannerAdParam) {
        logZ("requestAds with param:${param::class.java.simpleName}")
        if (canRequestAds()) {
            lifecycleOwner.lifecycleScope.launch {
                when (param) {
                    is BannerAdParam.Request -> {
                        flagActive.compareAndSet(false, true)
                        if (bannerAdView == null) {
                            adBannerState.emit(AdBannerState.Loading)
                        }
                        loadBannerAd()
                    }

                    is BannerAdParam.Ready -> {
                        flagActive.compareAndSet(false, true)
                        adBannerState.emit(AdBannerState.Loaded(param.bannerAds))
                    }

                    is BannerAdParam.Clickable -> {
                        if (isActiveState()) {
                            if (timeShowAdImpression + param.minimumTimeKeepAdsDisplay < System.currentTimeMillis()) {
                                loadBannerAd()
                            }
                        } else {
                            logInterruptExecute("requestAds Clickable")
                        }
                    }
                }
            }
        } else {
            if (!isOnline() && bannerAdView == null) {
                cancel()
            }
        }
    }

    override fun cancel() {
        logZ("cancel() called")
        flagActive.compareAndSet(true, false)
        bannerAdView = null
        lifecycleOwner.lifecycleScope.launch { adBannerState.emit(AdBannerState.Cancel) }
    }

    private fun loadBannerAd() {
        if (canRequestAds()) {
            AdmobFactory.getInstance()
                .requestBannerAd(activity, config.idAds, invokeListenerAdCallback())
        }
    }

    fun setShimmerLayoutView(shimmerLayoutView: ShimmerFrameLayout) = apply {
        kotlin.runCatching {
            this.shimmerLayoutView = shimmerLayoutView
            if (lifecycleOwner.lifecycle.currentState in Lifecycle.State.CREATED..Lifecycle.State.RESUMED) {
                if (!canRequestAds()) {
                    shimmerLayoutView.isVisible = false
                }
            }
        }
    }

    fun setBannerContentView(nativeContentView: FrameLayout) = apply {
        kotlin.runCatching {
            this.bannerContentView = nativeContentView
            this.shimmerLayoutView =
                nativeContentView.findViewById(R.id.shimmer_container_banner)
            if (lifecycleOwner.lifecycle.currentState in Lifecycle.State.CREATED..Lifecycle.State.RESUMED) {
                if (!canRequestAds()) {
                    nativeContentView.isVisible = false
                    shimmerLayoutView?.isVisible = false
                }
            }
        }
    }

    private fun getDefaultCallback(): AdCallback {
        return object : AdCallback() {
            override fun onAdImpression() {
                super.onAdImpression()
                timeShowAdImpression = System.currentTimeMillis()
                logZ("timeShowAdImpression:$timeShowAdImpression")
            }

            override fun onBannerLoaded(adView: AdView?) {
                super.onBannerLoaded(adView)
                if (isActiveState()) {
                    lifecycleOwner.lifecycleScope.launch {
                        bannerAdView = adView
                        if (adView != null) {
                            adBannerState.emit(AdBannerState.Loaded(adView))
                        }
                    }
                    logZ("onBannerLoaded()")
                } else {
                    logInterruptExecute("onBannerLoaded")
                }
            }

            override fun onAdFailedToLoad(i: LoadAdError?) {
                super.onAdFailedToLoad(i)
                if (isActiveState()) {
                    lifecycleOwner.lifecycleScope.launch {
                        adBannerState.emit(AdBannerState.Fail)
                    }
                    logZ("onAdFailedToLoad()")
                } else {
                    logInterruptExecute("onAdFailedToLoad")
                }
            }
        }
    }

    fun registerAdListener(adCallback: AdCallback) {
        this.listAdCallback.add(adCallback)
    }

    fun unregisterAdListener(adCallback: AdCallback) {
        this.listAdCallback.remove(adCallback)
    }

    fun unregisterAllAdListener() {
        this.listAdCallback.clear()
    }

    private fun invokeListenerAdCallback(): AdCallback {
        return object : AdCallback() {
            override fun onNextAction() {
                super.onNextAction()
                invokeAdListener { it.onNextAction() }
            }

            override fun onAdClosed() {
                super.onAdClosed()
                invokeAdListener { it.onAdClosed() }
            }

            override fun onAdFailedToLoad(i: LoadAdError?) {
                super.onAdFailedToLoad(i)
                invokeAdListener { it.onAdFailedToLoad(i) }
            }

            override fun onAdFailedToShow(adError: AdError?) {
                super.onAdFailedToShow(adError)
                invokeAdListener { it.onAdFailedToShow(adError) }
            }

            override fun onAdLeftApplication() {
                super.onAdLeftApplication()
                invokeAdListener { it.onAdLeftApplication() }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                invokeAdListener { it.onAdLoaded() }
            }

            override fun onAdSplashReady() {
                super.onAdSplashReady()
                invokeAdListener { it.onAdSplashReady() }
            }

            override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                super.onInterstitialLoad(interstitialAd)
                invokeAdListener { it.onInterstitialLoad(interstitialAd) }
            }

            override fun onAdClicked() {
                super.onAdClicked()
                invokeAdListener { it.onAdClicked() }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                invokeAdListener { it.onAdImpression() }
            }

            override fun onRewardAdLoaded(rewardedAd: RewardedAd?) {
                super.onRewardAdLoaded(rewardedAd)
                invokeAdListener { it.onRewardAdLoaded(rewardedAd) }
            }

            override fun onRewardAdLoaded(rewardedAd: RewardedInterstitialAd?) {
                super.onRewardAdLoaded(rewardedAd)
                invokeAdListener { it.onRewardAdLoaded(rewardedAd) }
            }

            override fun onUnifiedNativeAdLoaded(unifiedNativeAd: NativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd)
                invokeAdListener { it.onUnifiedNativeAdLoaded(unifiedNativeAd) }
            }

            override fun onInterstitialShow() {
                super.onInterstitialShow()
                invokeAdListener { it.onInterstitialShow() }
            }

            override fun onBannerLoaded(adView: AdView?) {
                super.onBannerLoaded(adView)
                invokeAdListener { it.onBannerLoaded(adView) }
            }
        }
    }

    private fun invokeAdListener(action: (adCallback: AdCallback) -> Unit) {
        listAdCallback.forEach(action)
    }
}