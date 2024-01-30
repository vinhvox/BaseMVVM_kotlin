package com.vapps.basemvvm_kotlin.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.vapps.basemvvm_kotlin.databinding.ActivityMainBinding
import com.vapps.common.ui.base.BaseActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity :
    BaseActivity<ActivityMainBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun onViewBindingCreated(savedInstanceState: Bundle?) {
        super.onViewBindingCreated(savedInstanceState)
        lifecycleScope.launch {
            delay(1000)
            startActivity(
                Intent(
                    this@MainActivity,
                    Class.forName("com.vapps.applock.view.AppLockActivity")
                )
            )
        }
    }
}