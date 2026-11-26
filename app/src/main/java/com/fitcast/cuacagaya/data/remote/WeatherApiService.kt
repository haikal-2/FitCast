package com.fitcast.cuacagaya.data.remote

import com.fitcast.cuacagaya.data.remote.dto.WeatherResponseDto

interface WeatherApiService {
    suspend fun getWeather(city: String, apiKey: String): WeatherResponseDto
}
