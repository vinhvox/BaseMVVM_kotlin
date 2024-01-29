package com.vapps.common.ui.lifecycle

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class RuntimePermissionObserver(
    private val activity: FragmentActivity
) : LifecycleObserver {

    private var onGranted: () -> Unit = {}
    private var onDenied: () -> Unit = {}
    private var onShouldShowRationale: () -> Unit = {}

    private lateinit var launcher: ActivityResultLauncher<Array<String>>

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        launcher = activity
            .activityResultRegistry
            .register(this.javaClass.name, ActivityResultContracts.RequestMultiplePermissions()) {

                var isGranted = false

                for (result in it) {
                    isGranted = result.value
                }

                if (isGranted)
                    onGranted()
                else
                    onDenied()

            }
    }

    fun launch(
        permissions: Array<String>,
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {},
        onShouldShowRationale: () -> Unit = {}
    ) {
        this.onGranted = onGranted
        this.onDenied = onDenied
        this.onShouldShowRationale = onShouldShowRationale

        when {
            // You can use the API that requires the permission.
            permissions.checkSelfPermission() -> onGranted()
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            permissions.checkShouldShowRationale(activity) -> onShouldShowRationale()

            else -> launcher.launch(permissions)
        }
    }

    private fun Array<String>.checkSelfPermission(): Boolean {

        var isGranted = false

        for (permission in this) {

            isGranted = ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED

            //if at least one permission is not granted, stop checking
            if (!isGranted) break
        }

        return isGranted
    }

    private fun Array<String>.checkShouldShowRationale(
        activity: FragmentActivity
    ): Boolean {

        var isShouldShow = false

        for (permission in this) {
            isShouldShow = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

            //if at least one permission should be rationale, stop checking
            if (isShouldShow) break
        }

        return isShouldShow
    }
}