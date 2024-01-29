package com.vapps.basemvvm_kotlin

import android.app.Application
import com.vapps.basemvvm_kotlin.di.getModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Main application class
 *
 * Created by Dmitriy Chernysh
 *
 *
 * http://mobile-dev.pro
 *
 *
 * #MobileDevPro
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(getModules())
        }
    }
}
