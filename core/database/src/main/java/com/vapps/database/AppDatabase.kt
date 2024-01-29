package com.vapps.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vapps.database.entity.UserEntity

/**
 * Room Database
 */

@Database(
    entities = [
        UserEntity::class
    ],
    version = BuildConfig.DB_VERSION,
    exportSchema = true
)
internal abstract class AppDatabase : RoomDatabase() {
    //internal abstract val userDao: UserDao


    companion object {

        fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                BuildConfig.DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}