package com.vapps.module_ads.control.utils

import android.content.Context

object SharePreferenceUtils {
    private const val PREF_NAME = "apero_ad_pref"
    private const val KEY_INSTALL_TIME = "KEY_INSTALL_TIME"
    private const val KEY_CURRENT_TOTAL_REVENUE_AD = "KEY_CURRENT_TOTAL_REVENUE_AD"
    private const val KEY_CURRENT_TOTAL_REVENUE_001_AD = "KEY_CURRENT_TOTAL_REVENUE_001_AD"
    private const val KEY_PUSH_EVENT_REVENUE_3_DAY = "KEY_PUSH_EVENT_REVENUE_3_DAY"
    private const val KEY_PUSH_EVENT_REVENUE_7_DAY = "KEY_PUSH_EVENT_REVENUE_7_DAY"
    private const val KEY_LAST_IMPRESSION_INTERSTITIAL_TIME =
        "KEY_LAST_IMPRESSION_INTERSTITIAL_TIME"
    private const val COMPLETE_RATED = "COMPLETE_RATED"
    fun getInstallTime(context: Context): Long {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getLong(KEY_INSTALL_TIME, 0)
    }

    fun setInstallTime(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putLong(KEY_INSTALL_TIME, System.currentTimeMillis()).apply()
    }

    fun getCurrentTotalRevenueAd(context: Context): Float {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getFloat(KEY_CURRENT_TOTAL_REVENUE_AD, 0f)
    }

    fun updateCurrentTotalRevenueAd(context: Context, revenue: Float) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var currentTotalRevenue = pre.getFloat(KEY_CURRENT_TOTAL_REVENUE_AD, 0f)
        currentTotalRevenue += (revenue / 1000000.0).toFloat()
        pre.edit().putFloat(KEY_CURRENT_TOTAL_REVENUE_AD, currentTotalRevenue).apply()
    }

    fun getCurrentTotalRevenue001Ad(context: Context): Float {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getFloat(KEY_CURRENT_TOTAL_REVENUE_001_AD, 0f)
    }

    fun updateCurrentTotalRevenue001Ad(context: Context, revenue: Float) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putFloat(KEY_CURRENT_TOTAL_REVENUE_001_AD, revenue).apply()
    }

    fun isPushRevenue3Day(context: Context): Boolean {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getBoolean(KEY_PUSH_EVENT_REVENUE_3_DAY, false)
    }

    fun setPushedRevenue3Day(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putBoolean(KEY_PUSH_EVENT_REVENUE_3_DAY, true).apply()
    }

    fun isPushRevenue7Day(context: Context): Boolean {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getBoolean(KEY_PUSH_EVENT_REVENUE_7_DAY, false)
    }

    fun setPushedRevenue7Day(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putBoolean(KEY_PUSH_EVENT_REVENUE_7_DAY, true).apply()
    }

    fun getLastImpressionInterstitialTime(context: Context): Long {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getLong(KEY_LAST_IMPRESSION_INTERSTITIAL_TIME, 0)
    }

    fun setLastImpressionInterstitialTime(context: Context) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putLong(KEY_LAST_IMPRESSION_INTERSTITIAL_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getCompleteRated(context: Context): Boolean {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getBoolean(COMPLETE_RATED, false)
    }

    fun setCompleteRated(context: Context, isCompleteRated: Boolean) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pre.edit().putBoolean(COMPLETE_RATED, isCompleteRated).apply()
    }
}