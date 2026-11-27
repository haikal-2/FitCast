package com.fitcast.cuacagaya.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.fitcast.cuacagaya.BuildConfig
import com.fitcast.cuacagaya.data.local.dao.WeatherDao
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity
import com.fitcast.cuacagaya.data.remote.WeatherApiService
import com.fitcast.cuacagaya.data.remote.dto.WeatherResponseDto
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class WeatherRepository(
    private val context: Context,
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao
) {
    val cachedWeather: Flow<WeatherCacheEntity?> = weatherDao.observeWeather()

    suspend fun refreshWeatherByCity(cityName: String): Result<WeatherCacheEntity> {
        val cleanCity = cityName.trim().ifBlank { "Bandung" }
        val apiKey = BuildConfig.WEATHER_API_KEY
        if (apiKey.isBlank()) {
            val fallback = buildOfflineCityWeather(cleanCity)
            weatherDao.saveWeather(fallback)
            return Result.failure(IllegalStateException("WEATHER_API_KEY kosong, memakai data offline"))
        }

        return try {
            val dto = apiService.getWeatherByCity(
                city = cleanCity,
                apiKey = apiKey
            )
            val entity = mapDtoToEntity(dto, fallbackCity = cleanCity)
            weatherDao.saveWeather(entity)
            Result.success(entity)
        } catch (e: Exception) {
            val existing = weatherDao.getCachedWeather()
            val fallback = if (existing != null && existing.cityName.equals(cleanCity, ignoreCase = true)) {
                existing
            } else {
                buildOfflineCityWeather(cleanCity)
            }
            weatherDao.saveWeather(fallback)
            Result.failure(e)
        }
    }

    suspend fun refreshWeatherByCurrentLocation(): Result<WeatherCacheEntity> {
        val apiKey = BuildConfig.WEATHER_API_KEY
        val location = withTimeoutOrNull(8_000L) { getCurrentLocation() }
        return if (location != null && apiKey.isNotBlank()) {
            try {
                val dto = apiService.getWeatherByCoords(
                    lat = location.latitude,
                    lon = location.longitude,
                    apiKey = apiKey
                )
                val entity = mapDtoToEntity(dto, fallbackCity = "Lokasi Saat Ini")
                weatherDao.saveWeather(entity)
                Result.success(entity)
            } catch (e: Exception) {
                val fallback = weatherDao.getCachedWeather() ?: buildOfflineCityWeather("Bandung")
                weatherDao.saveWeather(fallback)
                Result.failure(e)
            }
        } else {
            refreshWeatherByCity(weatherDao.getCachedWeather()?.cityName ?: "Bandung")
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return null

        return suspendCancellableCoroutine { cont ->
            val tokenSource = CancellationTokenSource()
            cont.invokeOnCancellation { tokenSource.cancel() }
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                    .addOnSuccessListener { location -> if (cont.isActive) cont.resume(location) }
                    .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                    .addOnCanceledListener { if (cont.isActive) cont.resume(null) }
            } catch (_: SecurityException) {
                if (cont.isActive) cont.resume(null)
            }
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

    private fun buildOfflineCityWeather(city: String): WeatherCacheEntity {
        val presetTemp = when (city.lowercase()) {
            "jakarta", "surabaya", "depok" -> 31.0
            "bogor" -> 23.0
            "yogyakarta" -> 28.0
            else -> 24.0
        }
        val presetRain = when (city.lowercase()) {
            "bogor" -> 80
            "bandung" -> 60
            else -> 30
        }
        return WeatherCacheEntity(
            id = 1,
            cityName = city,
            temperature = presetTemp,
            feelsLike = presetTemp + 1.0,
            humidity = if (presetRain >= 60) 78 else 65,
            windSpeed = 2.8,
            weatherMain = if (presetRain >= 60) "Rain" else "Clouds",
            weatherDescription = if (presetRain >= 60) "Berawan tebal (Cache Offline)" else "Berawan nyaman (Cache Offline)",
            rainProbability = presetRain,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
