package com.fitcast.cuacagaya.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto(
    @SerializedName("name") val cityName: String?,
    @SerializedName("main") val main: MainDto?,
    @SerializedName("weather") val weather: List<WeatherInfoDto>?,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("clouds") val clouds: CloudsDto?
)

data class MainDto(
    @SerializedName("temp") val temp: Double?,
    @SerializedName("feels_like") val feelsLike: Double?,
    @SerializedName("humidity") val humidity: Int?
)

data class WeatherInfoDto(
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?
)

data class WindDto(
    @SerializedName("speed") val speed: Double?
)

data class CloudsDto(
    @SerializedName("all") val all: Int?
)
