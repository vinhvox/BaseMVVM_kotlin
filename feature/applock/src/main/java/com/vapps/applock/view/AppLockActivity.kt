package com.vapps.applock.view

import android.util.Log
import android.view.View
import com.vapps.applock.R
import com.vapps.basemvvm_kotlin.BuildConfig
import com.vapps.common.ui.base.BaseActivity

class AppLockActivity : BaseActivity(R.layout.activity_app_lock) {
    override fun initViews(layoutView: View) {
        Log.e("TAG", "initViews: ${BuildConfig.app_open}", )
    }
}