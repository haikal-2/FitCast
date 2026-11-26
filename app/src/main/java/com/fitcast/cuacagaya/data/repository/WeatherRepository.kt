package com.fitcast.cuacagaya.data.repository

import com.fitcast.cuacagaya.data.remote.dto.WeatherResponseDto

class WeatherRepository {
    suspend fun fetchCurrentWeather(city: String): WeatherResponseDto {
        return WeatherResponseDto(cityName = city, temperature = 28.0, condition = "Sunny")
    }
}
