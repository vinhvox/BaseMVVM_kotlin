package com.vapps.module_ads.control.adjust

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.vapps.module_ads.control.utils.AppUtil

object AdjustHelper {
    private val messages = StringBuilder("")
    private val TAG = AdjustHelper::class.simpleName

    fun setupAdjust(context: Context, buildProd: Boolean, adjustToken: String) {
        val environment: String =
            if (buildProd) AdjustConfig.ENVIRONMENT_PRODUCTION else AdjustConfig.ENVIRONMENT_SANDBOX
        Log.d(TAG, "setupAdjust: $environment")
        val config = AdjustConfig(context, adjustToken, environment)

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE)
        config.setPreinstallTrackingEnabled(true)
        config.setOnAttributionChangedListener { attribution ->
            Log.d(TAG, "Attribution callback called!")
            Log.d(TAG, "Attribution: $attribution")
        }

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener { eventSuccessResponseData ->
            Log.d(TAG, "Event success callback called!")
            Log.d(TAG, "Event success data: $eventSuccessResponseData")
            messages.append(eventSuccessResponseData.toString()).append("\n\n")
            AppUtil.messageInit.postValue(messages.toString())
        }
        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener { eventFailureResponseData ->
            Log.d(TAG, "Event failure callback called!")
            Log.d(TAG, "Event failure data: $eventFailureResponseData")
        }

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener { sessionSuccessResponseData ->
            Log.d(TAG, "Session success callback called!")
            val d = Log.d(TAG, "Session success data: $sessionSuccessResponseData")
        }

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener { sessionFailureResponseData ->
            Log.d(TAG, "Session failure callback called!")
            Log.d(TAG, "Session failure data: $sessionFailureResponseData")
        }
        config.setSendInBackground(true)
        Adjust.onCreate(config)
        if (config.isValid) {
            messages.append("init adjust sdk successfully").append("\n\n")
        } else {
            messages.append("init adjust sdk failed").append("\n\n")
        }
        messages.append("Adjust Token : ")
            .append(adjustToken).append("\n\n")
        messages.append("Adjust Environment : ")
            .append(environment).append("\n\n")

        Log.e(TAG, "setupAdjust: $messages")
    }

    private class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
    }

}