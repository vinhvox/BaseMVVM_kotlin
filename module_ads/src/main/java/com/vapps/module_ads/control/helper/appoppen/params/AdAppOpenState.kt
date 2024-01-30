package com.vapps.module_ads.control.helper.appoppen.params


open class AdAppOpenState {
    data object None : AdAppOpenState()
    data object Fail : AdAppOpenState()
    data object Loading : AdAppOpenState()
    data object Loaded : AdAppOpenState()
    data object ShowFail : AdAppOpenState()
    data object Showed : AdAppOpenState()
    data object Cancel : AdAppOpenState()
}