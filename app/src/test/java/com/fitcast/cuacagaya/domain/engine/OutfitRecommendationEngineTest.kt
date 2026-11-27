package com.fitcast.cuacagaya.domain.engine

import com.fitcast.cuacagaya.data.local.entity.*
import org.junit.Assert.*
import org.junit.Test

class OutfitRecommendationEngineTest {

    private val engine = OutfitRecommendationEngine()

    private val sampleItems = listOf(
        WardrobeItemEntity(id = 1, name = "Kaos Katun Putih", category = WardrobeCategory.TOP, warmthLevel = WarmthLevel.LIGHT, style = OutfitStyle.CASUAL, colorHex = "#F8FAFC", colorTone = ColorTone.NEUTRAL, isClean = true),
        WardrobeItemEntity(id = 2, name = "Kemeja Flanel Tebal", category = WardrobeCategory.TOP, warmthLevel = WarmthLevel.HEAVY, style = OutfitStyle.FORMAL, colorHex = "#1D4ED8", colorTone = ColorTone.COOL, isClean = true),
        WardrobeItemEntity(id = 3, name = "Kaos Kotor", category = WardrobeCategory.TOP, warmthLevel = WarmthLevel.LIGHT, style = OutfitStyle.CASUAL, colorHex = "#000000", colorTone = ColorTone.NEUTRAL, isClean = false),
        WardrobeItemEntity(id = 4, name = "Celana Cargo Hitam", category = WardrobeCategory.BOTTOM, warmthLevel = WarmthLevel.MEDIUM, style = OutfitStyle.CASUAL, colorHex = "#0F172A", colorTone = ColorTone.NEUTRAL, isClean = true),
        WardrobeItemEntity(id = 5, name = "Hoodie Abu Tua", category = WardrobeCategory.OUTER, warmthLevel = WarmthLevel.HEAVY, style = OutfitStyle.CASUAL, colorHex = "#475569", colorTone = ColorTone.NEUTRAL, isClean = true),
        WardrobeItemEntity(id = 6, name = "Sneakers Putih", category = WardrobeCategory.SHOES, warmthLevel = WarmthLevel.LIGHT, style = OutfitStyle.CASUAL, colorHex = "#E2E8F0", colorTone = ColorTone.NEUTRAL, isClean = true)
    )

    @Test
    fun `rainy weather recommends umbrella and includes outer layer`() {
        val rainyWeather = WeatherCacheEntity(
            cityName = "Bogor",
            temperature = 24.0,
            feelsLike = 24.0,
            humidity = 88,
            windSpeed = 4.0,
            weatherMain = "Rain",
            weatherDescription = "Hujan ringan",
            rainProbability = 90
        )

        val result = engine.recommend(rainyWeather, sampleItems)

        assertTrue(result.bringUmbrella)
        assertNotNull(result.outer)
        assertNotNull(result.top)
        assertNotNull(result.bottom)
        assertNotNull(result.shoes)
    }

    @Test
    fun `hot clear weather skips outer layer and does not recommend umbrella`() {
        val hotWeather = WeatherCacheEntity(
            cityName = "Jakarta",
            temperature = 33.0,
            feelsLike = 36.0,
            humidity = 65,
            windSpeed = 2.0,
            weatherMain = "Clear",
            weatherDescription = "Cerah",
            rainProbability = 10
        )

        val result = engine.recommend(hotWeather, sampleItems)

        assertFalse(result.bringUmbrella)
        assertNull(result.outer)
        assertEquals(1L, result.top?.id)
    }

    @Test
    fun `dirty items are excluded when clean items exist`() {
        val mildWeather = WeatherCacheEntity(
            cityName = "Bandung",
            temperature = 25.0,
            feelsLike = 25.0,
            humidity = 70,
            windSpeed = 2.0,
            weatherMain = "Clouds",
            weatherDescription = "Berawan",
            rainProbability = 20
        )

        repeat(20) {
            val result = engine.recommend(mildWeather, sampleItems)
            assertNotEquals("Dirty item should not be selected", 3L, result.top?.id)
        }
    }

    @Test
    fun `locked items are always preserved even on hot weather`() {
        val hotWeather = WeatherCacheEntity(
            cityName = "Surabaya",
            temperature = 34.0,
            feelsLike = 37.0,
            humidity = 60,
            windSpeed = 3.0,
            weatherMain = "Clear",
            weatherDescription = "Cerah",
            rainProbability = 5
        )
        val lockedTop = sampleItems[1]
        val lockedOuter = sampleItems[4]

        val result = engine.recommend(
            weather = hotWeather,
            allItems = sampleItems,
            lockedItems = mapOf(
                WardrobeCategory.TOP to lockedTop,
                WardrobeCategory.OUTER to lockedOuter
            )
        )

        assertEquals(lockedTop.id, result.top?.id)
        assertEquals(lockedOuter.id, result.outer?.id)
    }
}
