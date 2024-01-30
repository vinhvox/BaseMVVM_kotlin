package com.vapps.module_ads.control.helper.adnative.params

import com.google.android.gms.ads.nativead.NativeAd
import com.vapps.module_ads.control.helper.params.IAdsParam

sealed class NativeAdParam : IAdsParam {
    data class Ready(val nativeAd: NativeAd) : NativeAdParam()
    object Request : NativeAdParam() {
        @JvmStatic
        fun create(): Request {
            return this
        }
    }
}
