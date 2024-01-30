package com.vapps.module_ads.control.utils

import androidx.lifecycle.MutableLiveData

object AppUtil {
    var VARIANT_DEV = true

    /**
     * current total revenue for paid_ad_impression_value_0.01 event
     */
    var currentTotalRevenue001Ad = 0f
    var messageInit = MutableLiveData<String>()
}