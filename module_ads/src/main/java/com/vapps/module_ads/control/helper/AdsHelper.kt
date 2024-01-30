package com.vapps.module_ads.control.helper

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.vapps.module_ads.control.billing.AppPurchase
import com.vapps.module_ads.control.helper.params.IAdsParam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by KO Huyn on 09/10/2023.
 */
abstract class AdsHelper<C : IAdsConfig, P : IAdsParam>(
    private val context: Context,
    private val lifecycleOwner:LifecycleOwner,
    private val config: C
) {
    private var tag: String = context::class.java.simpleName
    internal val flagActive: AtomicBoolean = AtomicBoolean(false)
    internal val lifecycleEventState = MutableStateFlow(Lifecycle.Event.ON_ANY)
    var flagUserEnableReload = true
        set(value) {
            field = value
            logZ("setFlagUserEnableReload($field)")
        }

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                lifecycleEventState.update { event }
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> {
                        lifecycleOwner.lifecycle.removeObserver(this)
                    }
                    else -> Unit
                }
            }
        })
    }

    open fun canShowAds(): Boolean {
        return !AppPurchase.instance!!.isPurchased && config.canShowAds
    }

    open fun canRequestAds(): Boolean {
        return canShowAds() && isOnline()
    }

    abstract fun requestAds(param: P)

    abstract fun cancel()

    fun setTagForDebug(tag: String) {
        this.tag = tag
    }

    fun isActiveState(): Boolean {
        return flagActive.get()
    }

    fun canReloadAd(): Boolean {
        return config.canReloadAds && flagUserEnableReload
    }

    internal fun logZ(message: String) {
        Log.d(this::class.java.simpleName, "${tag}: $message")
    }

    internal fun logInterruptExecute(message: String) {
        logZ("$message not execute because has called cancel()")
    }

    internal fun isOnline(): Boolean {
        val netInfo = kotlin.runCatching {
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        }.getOrNull()
        return netInfo != null && netInfo.isConnected
    }
}

interface IAdsConfig {
    val idAds: String
    val canShowAds: Boolean
    val canReloadAds: Boolean
}