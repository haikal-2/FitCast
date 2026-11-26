package com.fitcast.cuacagaya.data.local

import com.fitcast.cuacagaya.data.local.dao.WardrobeDao
import com.fitcast.cuacagaya.data.local.dao.WeatherDao

abstract class FitCastDatabase {
    abstract fun wardrobeDao(): WardrobeDao
    abstract fun weatherDao(): WeatherDao
}
