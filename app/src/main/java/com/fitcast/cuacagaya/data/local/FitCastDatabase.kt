package com.fitcast.cuacagaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fitcast.cuacagaya.data.local.dao.WardrobeDao
import com.fitcast.cuacagaya.data.local.dao.WeatherDao
import com.fitcast.cuacagaya.data.local.entity.SavedOutfitEntity
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity

@Database(
    entities = [WardrobeItemEntity::class, SavedOutfitEntity::class, WeatherCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FitCastDatabase : RoomDatabase() {
    abstract fun wardrobeDao(): WardrobeDao
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: FitCastDatabase? = null

        fun getInstance(context: Context): FitCastDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FitCastDatabase::class.java,
                    "fitcast_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
