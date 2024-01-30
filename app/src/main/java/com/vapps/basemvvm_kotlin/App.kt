package com.vapps.basemvvm_kotlin

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdActivity
import com.vapps.basemvvm_kotlin.di.getModules
import com.vapps.module_ads.control.admob.AdmobFactory
import com.vapps.module_ads.control.config.AdjustConfig
import com.vapps.module_ads.control.config.NetworkProvider
import com.vapps.module_ads.control.config.VioAdConfig
import com.vapps.module_ads.control.helper.appoppen.AppResumeAdConfig
import com.vapps.module_ads.control.helper.appoppen.AppResumeAdHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Main application class
 *
 * Created by Dmitriy Chernysh
 *
 *
 * http://mobile-dev.pro
 *
 *
 * #MobileDevPro
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(getModules())
        }

        configAds()
    }

    private fun configAds() {
        val adjustConfig = AdjustConfig.Builder(ADJUST_TOKEN)
            .eventNamePurchase(EVENT_PURCHASE_ADJUST)
            .eventAdImpression(EVENT_AD_IMPRESSION_ADJUST)
            .build()
        val vioAdConfig = VioAdConfig.Builder()
            .buildVariantProduce(false)
            .adjustConfig(adjustConfig)
            .mediationProvider(NetworkProvider.ADMOB)
            .listTestDevices(ArrayList())
            .build()
        AdmobFactory.getInstance().initAdmob(this, vioAdConfig)
        initAppOpenAd()
    }

    private fun initAppOpenAd(): AppResumeAdHelper {
        Log.e("TAG", "initAppOpenAd: ")
        val listClassInValid = mutableListOf<Class<*>>()
        listClassInValid.add(AdActivity::class.java)
        val config = AppResumeAdConfig(
            idAds = BuildConfig.app_open,
            listClassInValid = listClassInValid
        )
        return AppResumeAdHelper(
            application = this,
            lifecycleOwner = ProcessLifecycleOwner.get().lifecycle,
            config = config
        )
    }

    companion object {
        private const val ADJUST_TOKEN = "cc4jvudppczk"
        private const val EVENT_PURCHASE_ADJUST = "gzel1k"
        private const val EVENT_AD_IMPRESSION_ADJUST = "gzel1k"
    }
}
