package com.vapps.module_ads.control.config

import androidx.annotation.IntDef

@IntDef(NetworkProvider.ADMOB, NetworkProvider.MAX)
annotation class NetworkProvider {
    companion object {
        const val ADMOB = 0
        const val MAX = 1
    }
}