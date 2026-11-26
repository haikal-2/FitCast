package com.fitcast.cuacagaya.data.local.dao

import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity

interface WeatherDao {
    fun getWeatherCache(): WeatherCacheEntity?
    fun insertWeatherCache(cache: WeatherCacheEntity)
}
