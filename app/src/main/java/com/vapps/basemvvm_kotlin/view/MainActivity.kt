package com.vapps.basemvvm_kotlin.view

import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.vapps.basemvvm_kotlin.R
import com.vapps.common.ui.base.BaseActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(R.layout.activity_main) {

    override fun initViews(layoutView: View) {
        lifecycleScope.launch {
            delay(1000)
            startActivity(Intent(this@MainActivity, Class.forName("com.vapps.applock.view.AppLockActivity")))
        }
    }

}