package com.vapps.applock.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.vapps.applock.databinding.ActivityAppLockBinding
import com.vapps.basemvvm_kotlin.BuildConfig
import com.vapps.common.ui.base.BaseActivity

class AppLockActivity : BaseActivity<ActivityAppLockBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityAppLockBinding
        get() = ActivityAppLockBinding::inflate

    override fun onViewBindingCreated(savedInstanceState: Bundle?) {
        super.onViewBindingCreated(savedInstanceState)
        Log.e("TAG", "initViews: ${BuildConfig.app_open}")
    }
}