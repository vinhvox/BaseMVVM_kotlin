package com.vapps.module_ads.control.helper.appoppen

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppResumeAdHelper(
    application: Application,
    lifecycleOwner: Lifecycle,
    config: AppResumeAdConfig
) : LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    init {
        lifecycleOwner.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
        appOpenAdManager.setAdUnitId(config.idAds)
        application.registerActivityLifecycleCallbacks(this)
    }


    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        currentActivity?.let {
            appOpenAdManager.showAdIfAvailable(it)
        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        Log.e("TAG", "onActivityStarted: ")
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }
}