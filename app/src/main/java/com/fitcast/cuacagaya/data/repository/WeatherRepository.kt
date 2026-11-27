package com.fitcast.cuacagaya.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.fitcast.cuacagaya.BuildConfig
import com.fitcast.cuacagaya.data.local.dao.WeatherDao
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity
import com.fitcast.cuacagaya.data.remote.WeatherApiService
import com.fitcast.cuacagaya.data.remote.dto.WeatherResponseDto
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WeatherRepository(
    private val context: Context,
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao
) {
    val cachedWeather: Flow<WeatherCacheEntity?> = weatherDao.observeWeather()

    suspend fun refreshWeatherByCity(cityName: String): Result<WeatherCacheEntity> {
        return try {
            val dto = apiService.getWeatherByCity(
                city = cityName,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
            val entity = mapDtoToEntity(dto, fallbackCity = cityName)
            weatherDao.saveWeather(entity)
            Result.success(entity)
        } catch (e: Exception) {
            val fallback = weatherDao.getCachedWeather() ?: defaultOfflineWeather(cityName)
            weatherDao.saveWeather(fallback)
            Result.failure(e)
        }
    }

    suspend fun refreshWeatherByCurrentLocation(): Result<WeatherCacheEntity> {
        val location = getCurrentLocation()
        return if (location != null) {
            try {
                val dto = apiService.getWeatherByCoords(
                    lat = location.latitude,
                    lon = location.longitude,
                    apiKey = BuildConfig.WEATHER_API_KEY
                )
                val entity = mapDtoToEntity(dto, fallbackCity = "Lokasi Saat Ini")
                weatherDao.saveWeather(entity)
                Result.success(entity)
            } catch (e: Exception) {
                val fallback = weatherDao.getCachedWeather() ?: defaultOfflineWeather("Bandung")
                weatherDao.saveWeather(fallback)
                Result.failure(e)
            }
        } else {
            refreshWeatherByCity("Bandung")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location -> if (cont.isActive) cont.resume(location) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        } catch (_: SecurityException) {
            if (cont.isActive) cont.resume(null)
        }
    }

    private fun mapDtoToEntity(dto: WeatherResponseDto, fallbackCity: String): WeatherCacheEntity {
        val mainCondition = dto.weather?.firstOrNull()?.main ?: "Clouds"
        val clouds = dto.clouds?.all ?: 40
        val rainProb = when {
            mainCondition.contains("Rain", true) || mainCondition.contains("Thunderstorm", true) -> 90
            mainCondition.contains("Drizzle", true) -> 70
            clouds >= 80 -> 55
            else -> (clouds / 3).coerceIn(5, 40)
        }

        return WeatherCacheEntity(
            id = 1,
            cityName = dto.cityName ?: fallbackCity,
            temperature = dto.main?.temp ?: 25.0,
            feelsLike = dto.main?.feelsLike ?: 26.0,
            humidity = dto.main?.humidity ?: 70,
            windSpeed = dto.wind?.speed ?: 3.0,
            weatherMain = mainCondition,
            weatherDescription = dto.weather?.firstOrNull()?.description
                ?.replaceFirstChar { it.uppercase() } ?: "Berawan",
            rainProbability = rainProb,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun defaultOfflineWeather(city: String) = WeatherCacheEntity(
        id = 1,
        cityName = "$city (Mode Offline)",
        temperature = 24.0,
        feelsLike = 25.0,
        humidity = 75,
        windSpeed = 2.5,
        weatherMain = "Clouds",
        weatherDescription = "Berawan (Data Cache)",
        rainProbability = 40
    )
}
