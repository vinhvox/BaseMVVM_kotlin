package com.vapps.basemvvm_kotlin.di


import com.vapps.basemvvm_kotlin.helper.ImplResourcesProvider
import com.vapps.basemvvm_kotlin.helper.ResourcesProvider
import com.vapps.database.di.coreDatabaseModule
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


/**
 * App Koin module
 *
 */
fun getModules() = listOf(
    presentationCommonModule,
    coreDatabaseModule
)

val presentationCommonModule = module {
    single<ResourcesProvider> {
        ImplResourcesProvider(androidApplication().resources)
    }
}

