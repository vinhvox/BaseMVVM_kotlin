package com.vapps.module_ads.control.helper.appoppen

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.vapps.module_ads.control.admob.AdmobFactory
import com.vapps.module_ads.control.dialog.LoadingDialog
import com.vapps.module_ads.control.helper.AdsHelper
import com.vapps.module_ads.control.helper.appoppen.params.AdAppOpenState
import com.vapps.module_ads.control.helper.appoppen.params.AppOpenAdParam
import com.vapps.module_ads.control.listener.AdCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class AppOpenAdHelper(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val config: AppOpenAdConfig
) : AdsHelper<AppOpenAdConfig, AppOpenAdParam>(activity, lifecycleOwner, config) {
    private val loadingDialog by lazy { LoadingDialog() }
    private val listAdCallback: CopyOnWriteArrayList<AdCallback> = CopyOnWriteArrayList()
    private val adAppOpenState: MutableStateFlow<AdAppOpenState> =
        MutableStateFlow(if (canRequestAds()) AdAppOpenState.None else AdAppOpenState.Fail)
    private var appOpenAdValue: AppOpenAd? = null

    private var requestTimeOutJob: Job? = null
    private var requestDelayJob: Job? = null
    private var showValid = false

    private fun showAppOpenAd(activity: Activity) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (adAppOpenState.value == AdAppOpenState.Loaded || adAppOpenState.value == AdAppOpenState.ShowFail) {
                    requestDelayJob?.cancel()
                    appOpenAdValue?.let {
                        showDialogLoading()
                        delay(800)
                        AdmobFactory.getInstance()
                            .showAppOpenAds(activity, it, invokeListenerAdCallback())
                    }
                }
            }
        }
    }

    private fun showDialogLoading() {
        try {
            val transaction: FragmentTransaction =
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            val prev = activity.supportFragmentManager.findFragmentByTag(
                LoadingDialog.TAG
            )
            if (prev != null) {
                transaction.remove(prev)
            }
            transaction.addToBackStack(null)
            loadingDialog.show(transaction, LoadingDialog.TAG)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun createAppOpenAd(activity: Activity) {
        requestTimeOutJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            AdmobFactory.getInstance()
                .requestAppOpenAds(activity, config.idAds, invokeListenerAdCallback())
            delay(config.timeOut)
            if (appOpenAdValue != null && config.showReady) {
                showAppOpenAd(activity)
            } else {
                invokeAdListener { it.onNextAction() }
                requestTimeOutJob?.cancel()
            }
        }
        requestDelayJob = lifecycleOwner.lifecycleScope.launch {
            delay(config.timeDelay)
            showValid = true
            if (appOpenAdValue != null && config.showReady) {
                showAppOpenAd(activity)
            }
        }

    }

    override fun requestAds(param: AppOpenAdParam) {
        lifecycleOwner.lifecycleScope.launch {
            if (canRequestAds()) {
                when (param) {
                    is AppOpenAdParam.Request -> {
                        flagActive.compareAndSet(false, true)
                        if (appOpenAdValue == null) {
                            adAppOpenState.emit(AdAppOpenState.Loading)
                        }
                        createAppOpenAd(activity)
                    }

                    is AppOpenAdParam.Show -> {
                        flagActive.compareAndSet(false, true)
                        adAppOpenState.emit(AdAppOpenState.Loaded)
                        showAppOpenAd(activity)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    override fun cancel() {
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

    private fun invokeAdListener(action: (adCallback: AdCallback) -> Unit) {
        listAdCallback.forEach(action)
    }

    private fun invokeListenerAdCallback(): AdCallback {
        return object : AdCallback() {
            override fun onAdFailedToLoad(i: LoadAdError?) {
                super.onAdFailedToLoad(i)
                invokeAdListener { it.onAdFailedToLoad(i) }
            }

            override fun onAppOpenAdLoaded(appOpenAd: AppOpenAd) {
                super.onAppOpenAdLoaded(appOpenAd)
                Log.e(TAG, "onAppOpenAdLoaded")
                appOpenAdValue = appOpenAd
                lifecycleOwner.lifecycleScope.launch {
                    adAppOpenState.emit(AdAppOpenState.Loaded)
                }
                if (showValid && config.showReady) {
                    showAppOpenAd(activity)
                } else {
                    invokeAdListener { it.onAppOpenAdLoaded(appOpenAd) }
                }
            }

            override fun onAdFailedToShow(adError: AdError?) {
                super.onAdFailedToShow(adError)
                loadingDialog.dismiss()
                lifecycleOwner.lifecycleScope.launch {
                    adAppOpenState.emit(AdAppOpenState.ShowFail)
                }
                invokeAdListener { it.onAdFailedToShow(adError) }
                if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    invokeAdListener { it.onNextAction() }
                }
            }

            override fun onAppOpenAdShow() {
                super.onAppOpenAdShow()
                super.onInterstitialShow()
                loadingDialog.dismiss()
                lifecycleOwner.lifecycleScope.launch {
                    adAppOpenState.emit(AdAppOpenState.Showed)
                }
                requestTimeOutJob?.cancel()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                invokeAdListener { it.onAdClicked() }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                invokeAdListener { it.onAdImpression() }
            }

        }
    }

    companion object {
        private val TAG = AppOpenAdHelper::class.simpleName
    }
}