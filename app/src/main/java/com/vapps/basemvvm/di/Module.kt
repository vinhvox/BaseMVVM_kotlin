package com.vapps.basemvvm.di


import com.vapps.basemvvm.helper.ImplResourcesProvider
import com.vapps.basemvvm.helper.ResourcesProvider
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


///**
// * App Koin module
// *
// */
//fun getModules() = listOf(
//    presentationCommonModule,
//    coreDatabaseModule
//)
//
//val presentationCommonModule = module {
//    single<ResourcesProvider> {
//        ImplResourcesProvider(androidApplication().resources)
//    }
//}