package com.vapps.module_ads.control.helper.interstitial.params

import com.google.android.gms.ads.interstitial.InterstitialAd

/**
 *Created by KO Huyn on 10/24/2023
 */
sealed class AdInterstitialState {
    data object None : AdInterstitialState()
    data object Fail : AdInterstitialState()
    data object Loading : AdInterstitialState()
    data object Loaded: AdInterstitialState()
    data object ShowFail: AdInterstitialState()
    data object Showed: AdInterstitialState()
    data object Cancel : AdInterstitialState()
    data class Show(val interstitialAd: InterstitialAd) : AdInterstitialState()
}