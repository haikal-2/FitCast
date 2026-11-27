package com.fitcast.cuacagaya.domain.engine

import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.OutfitStyle
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WarmthLevel
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutfitRecommendationEngineTest {

    private val engine = OutfitRecommendationEngine()

    private val sampleItems = listOf(
        WardrobeItemEntity(
            id = 1,
            name = "Kaos Katun Putih",
            category = WardrobeCategory.TOP,
            warmthLevel = WarmthLevel.LIGHT,
            style = OutfitStyle.CASUAL,
            colorHex = "#F8FAFC",
            colorTone = ColorTone.NEUTRAL,
            isClean = true
        ),
        WardrobeItemEntity(
            id = 2,
            name = "Kemeja Flanel Tebal",
            category = WardrobeCategory.TOP,
            warmthLevel = WarmthLevel.HEAVY,
            style = OutfitStyle.FORMAL,
            colorHex = "#1D4ED8",
            colorTone = ColorTone.COOL,
            isClean = true
        ),
        WardrobeItemEntity(
            id = 3,
            name = "Kaos Kotor",
            category = WardrobeCategory.TOP,
            warmthLevel = WarmthLevel.LIGHT,
            style = OutfitStyle.CASUAL,
            colorHex = "#000000",
            colorTone = ColorTone.NEUTRAL,
            isClean = false
        ),
        WardrobeItemEntity(
            id = 4,
            name = "Celana Cargo Hitam",
            category = WardrobeCategory.BOTTOM,
            warmthLevel = WarmthLevel.MEDIUM,
            style = OutfitStyle.CASUAL,
            colorHex = "#0F172A",
            colorTone = ColorTone.NEUTRAL,
            isClean = true
        ),
        WardrobeItemEntity(
            id = 5,
            name = "Hoodie Abu Tua",
            category = WardrobeCategory.OUTER,
            warmthLevel = WarmthLevel.HEAVY,
            style = OutfitStyle.CASUAL,
            colorHex = "#475569",
            colorTone = ColorTone.NEUTRAL,
            isClean = true
        ),
        WardrobeItemEntity(
            id = 6,
            name = "Sneakers Putih",
            category = WardrobeCategory.SHOES,
            warmthLevel = WarmthLevel.LIGHT,
            style = OutfitStyle.CASUAL,
            colorHex = "#E2E8F0",
            colorTone = ColorTone.NEUTRAL,
            isClean = true
        ),
        WardrobeItemEntity(
            id = 7,
            name = "Jaket Parasut Sport",
            category = WardrobeCategory.OUTER,
            warmthLevel = WarmthLevel.HEAVY,
            style = OutfitStyle.SPORT,
            colorHex = "#0F766E",
            colorTone = ColorTone.COOL,
            isClean = true
        ),
        WardrobeItemEntity(
            id = 8,
            name = "Celana Chino Formal",
            category = WardrobeCategory.BOTTOM,
            warmthLevel = WarmthLevel.MEDIUM,
            style = OutfitStyle.FORMAL,
            colorHex = "#C2B280",
            colorTone = ColorTone.WARM,
            isClean = true
        )
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
        assertTrue(result.weatherAdvice.contains("payung", ignoreCase = true))
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
        assertTrue(result.weatherAdvice.contains("terik", ignoreCase = true))
    }

    @Test
    fun `cold dry weather includes outer layer without umbrella`() {
        val coldWeather = WeatherCacheEntity(
            cityName = "Lembang",
            temperature = 18.0,
            feelsLike = 17.0,
            humidity = 75,
            windSpeed = 3.5,
            weatherMain = "Clouds",
            weatherDescription = "Sejuk berawan",
            rainProbability = 20
        )

        val result = engine.recommend(coldWeather, sampleItems)

        assertFalse(result.bringUmbrella)
        assertNotNull("Cold weather should include outer layer", result.outer)
        assertEquals("Heavy top should be prioritized when cold", 2L, result.top?.id)
        assertTrue(result.weatherAdvice.contains("sejuk", ignoreCase = true))
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
    fun `all dirty wardrobe falls back gracefully without returning null`() {
        val allDirty = sampleItems.map { it.copy(isClean = false) }
        val mildWeather = WeatherCacheEntity(
            cityName = "Bandung",
            temperature = 24.0,
            feelsLike = 24.0,
            humidity = 75,
            windSpeed = 2.0,
            weatherMain = "Rain",
            weatherDescription = "Hujan ringan",
            rainProbability = 65
        )

        val result = engine.recommend(mildWeather, allDirty)

        assertNotNull(result.top)
        assertNotNull(result.bottom)
        assertNotNull(result.outer)
        assertNotNull(result.shoes)
    }

    @Test
    fun `locked items are always preserved even on hot weather across shuffles`() {
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

        repeat(15) {
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

    @Test
    fun `style filter prioritizes matching style items`() {
        val mildWeather = WeatherCacheEntity(
            cityName = "Bandung",
            temperature = 24.0,
            feelsLike = 24.0,
            humidity = 70,
            windSpeed = 2.0,
            weatherMain = "Rain",
            weatherDescription = "Hujan ringan",
            rainProbability = 70
        )

        val formalItems = listOf(
            WardrobeItemEntity(10, "Kaos Casual", WardrobeCategory.TOP, WarmthLevel.MEDIUM, OutfitStyle.CASUAL, "#FFFFFF", ColorTone.NEUTRAL),
            WardrobeItemEntity(11, "Kemeja Formal", WardrobeCategory.TOP, WarmthLevel.MEDIUM, OutfitStyle.FORMAL, "#FFFFFF", ColorTone.NEUTRAL),
            WardrobeItemEntity(12, "Jeans Casual", WardrobeCategory.BOTTOM, WarmthLevel.MEDIUM, OutfitStyle.CASUAL, "#0F172A", ColorTone.NEUTRAL),
            WardrobeItemEntity(13, "Chino Formal", WardrobeCategory.BOTTOM, WarmthLevel.MEDIUM, OutfitStyle.FORMAL, "#0F172A", ColorTone.NEUTRAL),
            WardrobeItemEntity(14, "Jaket Sport", WardrobeCategory.OUTER, WarmthLevel.HEAVY, OutfitStyle.SPORT, "#0F766E", ColorTone.NEUTRAL),
            WardrobeItemEntity(15, "Blazer Formal", WardrobeCategory.OUTER, WarmthLevel.HEAVY, OutfitStyle.FORMAL, "#1E293B", ColorTone.NEUTRAL)
        )

        repeat(20) {
            val result = engine.recommend(
                weather = mildWeather,
                allItems = formalItems,
                preferredStyle = OutfitStyle.FORMAL
            )
            assertEquals("Formal top should win when style=FORMAL", 11L, result.top?.id)
            assertEquals("Formal bottom should win when style=FORMAL", 13L, result.bottom?.id)
            assertEquals("Formal outer should win when style=FORMAL", 15L, result.outer?.id)
        }
    }

    @Test
    fun `missing categories return null safely without crashing`() {
        val onlyTop = listOf(sampleItems.first())
        val rainyWeather = WeatherCacheEntity(
            cityName = "Bandung",
            temperature = 21.0,
            feelsLike = 21.0,
            humidity = 85,
            windSpeed = 3.0,
            weatherMain = "Rain",
            weatherDescription = "Hujan",
            rainProbability = 80
        )

        val result = engine.recommend(rainyWeather, onlyTop)

        assertNotNull(result.top)
        assertNull(result.bottom)
        assertNull(result.outer)
        assertNull(result.shoes)
        assertEquals("Lengkapi Lemari", result.harmonyLabel)
    }
}
