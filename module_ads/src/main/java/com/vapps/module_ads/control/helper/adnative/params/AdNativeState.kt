package com.vapps.module_ads.control.helper.adnative.params

import com.google.android.gms.ads.nativead.NativeAd


/**
 * Created by KO Huyn on 09/10/2023.
 */
sealed class AdNativeState {
    object None : AdNativeState()
    object Fail : AdNativeState()
    object Loading : AdNativeState()
    object Cancel : AdNativeState()
    data class Loaded(val adNative: NativeAd) : AdNativeState()
}