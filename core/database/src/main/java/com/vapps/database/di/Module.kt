package com.vapps.database.di

import com.vapps.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module
 */

val coreDatabaseModule = module {

    single {
        AppDatabase.buildDatabase(androidContext())
    }
}
