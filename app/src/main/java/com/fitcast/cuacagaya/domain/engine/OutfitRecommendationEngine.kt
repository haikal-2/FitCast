package com.fitcast.cuacagaya.domain.engine

import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WarmthLevel
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity

data class RecommendedOutfit(
    val top: WardrobeItemEntity?,
    val bottom: WardrobeItemEntity?,
    val outer: WardrobeItemEntity?,
    val shoes: WardrobeItemEntity?,
    val weatherAdvice: String,
    val bringUmbrella: Boolean,
    val harmonyLabel: String
)

class OutfitRecommendationEngine {

    fun recommend(
        weather: WeatherCacheEntity,
        allItems: List<WardrobeItemEntity>,
        lockedItems: Map<String, WardrobeItemEntity> = emptyMap(),
        preferredStyle: String? = null
    ): RecommendedOutfit {
        val activeItems = allItems.filter { !it.isDraft }
        val cleanItems = activeItems.filter { it.isClean }
        val pool = if (cleanItems.isNotEmpty()) cleanItems else activeItems

        val isRainy = weather.rainProbability >= 50 ||
            weather.weatherMain.contains("Rain", true) ||
            weather.weatherMain.contains("Drizzle", true) ||
            weather.weatherMain.contains("Thunderstorm", true)
        val isHot = weather.temperature >= 30.0
        val isCold = weather.temperature <= 22.0
        val isWindy = weather.windSpeed >= 4.0

        val targetWarmth = when {
            isHot -> WarmthLevel.LIGHT
            isCold || isRainy -> WarmthLevel.HEAVY
            else -> WarmthLevel.MEDIUM
        }

        fun pickCategory(cat: String, anchorColorTone: String? = null): WardrobeItemEntity? {
            lockedItems[cat]?.let { return it }
            val candidates = pool.filter { it.category == cat }
            if (candidates.isEmpty()) return null

            val scored = candidates.map { item ->
                var score = 0
                if (item.warmthLevel == targetWarmth) score += 30
                else if (item.warmthLevel == WarmthLevel.MEDIUM) score += 15

                if (preferredStyle != null && item.style == preferredStyle) score += 25

                if (matchesSuitableWeather(item.suitableWeathers, isRainy, isHot, isCold, isWindy)) {
                    score += 15
                }

                if (anchorColorTone != null) {
                    score += colorHarmonyScore(anchorColorTone, item.colorTone)
                }
                score += (0..12).random()
                item to score
            }
            return scored.maxByOrNull { it.second }?.first
        }

        val top = pickCategory(WardrobeCategory.TOP)
        val bottom = pickCategory(WardrobeCategory.BOTTOM, anchorColorTone = top?.colorTone)
        val outer = if (isCold || isRainy || lockedItems.containsKey(WardrobeCategory.OUTER)) {
            pickCategory(WardrobeCategory.OUTER, anchorColorTone = top?.colorTone)
        } else null
        val shoes = pickCategory(WardrobeCategory.SHOES, anchorColorTone = bottom?.colorTone)

        val advice = when {
            isRainy -> "Potensi hujan ${weather.rainProbability}%. Pakai jaket tahan angin & siapkan payung atau jas hujan!"
            isHot -> "Cuaca terik (${weather.temperature.toInt()}°C). Utamakan bahan adem menyerap keringat."
            isCold -> "Udara sejuk (${weather.temperature.toInt()}°C). Cocok pakai layering jaket atau hoodie."
            else -> "Cuaca nyaman (${weather.temperature.toInt()}°C). Kombinasi kasual seimbang sangat pas hari ini."
        }

        val harmony = if (top != null && bottom != null) {
            when (colorHarmonyScore(top.colorTone, bottom.colorTone)) {
                30 -> "Harmoni Netral (Aman & Rapi)"
                25 -> "Harmoni Kontras Seimbang"
                else -> "Harmoni Senada"
            }
        } else "Lengkapi Lemari"

        return RecommendedOutfit(
            top = top,
            bottom = bottom,
            outer = outer,
            shoes = shoes,
            weatherAdvice = advice,
            bringUmbrella = isRainy,
            harmonyLabel = harmony
        )
    }

    private fun matchesSuitableWeather(
        suitableWeathers: String,
        isRainy: Boolean,
        isHot: Boolean,
        isCold: Boolean,
        isWindy: Boolean
    ): Boolean {
        if (suitableWeathers.isBlank()) return false
        return when {
            isRainy && suitableWeathers.contains("Hujan", ignoreCase = true) -> true
            isHot && suitableWeathers.contains("Panas", ignoreCase = true) -> true
            isCold && (suitableWeathers.contains("Dingin", ignoreCase = true) || suitableWeathers.contains("Berangin", ignoreCase = true)) -> true
            isWindy && suitableWeathers.contains("Berangin", ignoreCase = true) -> true
            !isRainy && !isHot && suitableWeathers.contains("Berawan", ignoreCase = true) -> true
            else -> false
        }
    }

    private fun colorHarmonyScore(toneA: String, toneB: String): Int {
        return when {
            toneA == ColorTone.NEUTRAL || toneB == ColorTone.NEUTRAL -> 30
            toneA != toneB -> 25
            else -> 15
        }
    }
}
