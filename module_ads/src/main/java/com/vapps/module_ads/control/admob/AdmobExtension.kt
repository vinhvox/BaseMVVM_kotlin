package com.vapps.module_ads.control.admob

import android.app.Activity
import android.util.DisplayMetrics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize

private const val MAX_SMALL_INLINE_BANNER_HEIGHT = 50


fun getAdRequest(): AdRequest {
    val builder = AdRequest.Builder()
    return builder.build()
}

fun getAdSize(
    mActivity: Activity,
    useInlineAdaptive: Boolean,
    inlineStyle: Int
): AdSize {

    // Step 2 - Determine the screen width (less decorations) to use for the ad width.
    val display = mActivity.windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    val widthPixels = outMetrics.widthPixels.toFloat()
    val density = outMetrics.density
    val adWidth = (widthPixels / density).toInt()

    // Step 3 - Get adaptive ad size and return for setting on the ad view.
    return if (useInlineAdaptive) {
        if (inlineStyle == BannerInlineStyle.LARGE_STYLE) {
            AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                mActivity,
                adWidth
            )
        } else {
            AdSize.getInlineAdaptiveBannerAdSize(
                adWidth,
                MAX_SMALL_INLINE_BANNER_HEIGHT
            )
        }
    } else AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        mActivity,
        adWidth
    )
}