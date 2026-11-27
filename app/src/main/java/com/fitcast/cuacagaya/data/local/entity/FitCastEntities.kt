package com.fitcast.cuacagaya.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

object WardrobeCategory {
    const val TOP = "TOP"
    const val BOTTOM = "BOTTOM"
    const val OUTER = "OUTER"
    const val SHOES = "SHOES"
    const val ACCESSORY = "ACCESSORY"
    val ALL = listOf(TOP, BOTTOM, OUTER, SHOES, ACCESSORY)
}

object WarmthLevel {
    const val LIGHT = "LIGHT"   // Tipis / Adem
    const val MEDIUM = "MEDIUM" // Sedang
    const val HEAVY = "HEAVY"   // Tebal / Hangat
    val ALL = listOf(LIGHT, MEDIUM, HEAVY)
}

object OutfitStyle {
    const val CASUAL = "CASUAL"
    const val FORMAL = "FORMAL"
    const val SPORT = "SPORT"
    val ALL = listOf(CASUAL, FORMAL, SPORT)
}

object ColorTone {
    const val NEUTRAL = "NEUTRAL"
    const val WARM = "WARM"
    const val COOL = "COOL"
}

@Entity(tableName = "wardrobe_items")
data class WardrobeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,          // WardrobeCategory
    val warmthLevel: String,       // WarmthLevel
    val style: String,             // OutfitStyle
    val colorHex: String,          // Contoh: "#1E293B"
    val colorTone: String,         // ColorTone
    val imagePath: String = "",    // Kosong = pakai ikon bawaan Starter Pack
    val wearCount: Int = 0,
    val maxWearBeforeWash: Int = 2,
    val isClean: Boolean = true
)

@Entity(tableName = "saved_outfits")
data class SavedOutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val outfitName: String,
    val topId: Long?,
    val bottomId: Long?,
    val outerId: Long?,
    val shoesId: Long?,
    val targetWeather: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherMain: String,        // Clear, Clouds, Rain, Drizzle, Thunderstorm
    val weatherDescription: String,
    val rainProbability: Int,       // 0 - 100
    val lastUpdated: Long = System.currentTimeMillis()
)
