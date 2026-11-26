package com.fitcast.cuacagaya.data.local.entity

data class WeatherCacheEntity(
    val id: Int = 1,
    val cityName: String,
    val tempCelsius: Double,
    val condition: String,
    val timestamp: Long
)
