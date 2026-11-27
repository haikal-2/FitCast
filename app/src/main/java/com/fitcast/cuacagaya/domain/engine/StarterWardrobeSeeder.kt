package com.fitcast.cuacagaya.domain.engine

import com.fitcast.cuacagaya.data.local.dao.WardrobeDao
import com.fitcast.cuacagaya.data.local.entity.*

object StarterWardrobeSeeder {
    suspend fun seedIfEmpty(dao: WardrobeDao) {
        if (dao.getItemCount() > 0) return

        val starterItems = listOf(
            WardrobeItemEntity(name = "Kaos Katun Putih", category = WardrobeCategory.TOP, warmthLevel = WarmthLevel.LIGHT, style = OutfitStyle.CASUAL, colorHex = "#F8FAFC", colorTone = ColorTone.NEUTRAL, maxWearBeforeWash = 1),
            WardrobeItemEntity(name = "Kaos Hitam Basic", category = WardrobeCategory.TOP, warmthLevel = WarmthLevel.LIGHT, style = OutfitStyle.CASUAL, colorHex = "#1E293B", colorTone = ColorTone.NEUTRAL, maxWearBeforeWash = 1),
            WardrobeItemEntity(name = "Kemeja Flanel Navy", category = WardrobeCategory.TOP, warmthLevel = WarmthLevel.MEDIUM, style = OutfitStyle.FORMAL, colorHex = "#1D4ED8", colorTone = ColorTone.COOL, maxWearBeforeWash = 2),
            WardrobeItemEntity(name = "Kemeja Linen Beige", category = WardrobeCategory.TOP, warmthLevel = WarmthLevel.LIGHT, style = OutfitStyle.FORMAL, colorHex = "#D6C5B3", colorTone = ColorTone.WARM, maxWearBeforeWash = 2),
            WardrobeItemEntity(name = "Celana Chino Cream", category = WardrobeCategory.BOTTOM, warmthLevel = WarmthLevel.MEDIUM, style = OutfitStyle.FORMAL, colorHex = "#C2B280", colorTone = ColorTone.WARM, maxWearBeforeWash = 3),
            WardrobeItemEntity(name = "Celana Jeans Dark Blue", category = WardrobeCategory.BOTTOM, warmthLevel = WarmthLevel.HEAVY, style = OutfitStyle.CASUAL, colorHex = "#1E3A8A", colorTone = ColorTone.COOL, maxWearBeforeWash = 4),
            WardrobeItemEntity(name = "Celana Cargo Hitam", category = WardrobeCategory.BOTTOM, warmthLevel = WarmthLevel.MEDIUM, style = OutfitStyle.CASUAL, colorHex = "#0F172A", colorTone = ColorTone.NEUTRAL, maxWearBeforeWash = 3),
            WardrobeItemEntity(name = "Jaket Parasit Anti-Angin", category = WardrobeCategory.OUTER, warmthLevel = WarmthLevel.HEAVY, style = OutfitStyle.SPORT, colorHex = "#0F766E", colorTone = ColorTone.COOL, maxWearBeforeWash = 5),
            WardrobeItemEntity(name = "Hoodie Abu Tua", category = WardrobeCategory.OUTER, warmthLevel = WarmthLevel.HEAVY, style = OutfitStyle.CASUAL, colorHex = "#475569", colorTone = ColorTone.NEUTRAL, maxWearBeforeWash = 4),
            WardrobeItemEntity(name = "Cardigan Rajut Coklat", category = WardrobeCategory.OUTER, warmthLevel = WarmthLevel.MEDIUM, style = OutfitStyle.FORMAL, colorHex = "#92400E", colorTone = ColorTone.WARM, maxWearBeforeWash = 4),
            WardrobeItemEntity(name = "Sneakers Putih Klasik", category = WardrobeCategory.SHOES, warmthLevel = WarmthLevel.LIGHT, style = OutfitStyle.CASUAL, colorHex = "#E2E8F0", colorTone = ColorTone.NEUTRAL, maxWearBeforeWash = 10),
            WardrobeItemEntity(name = "Sepatu Boots Tahan Air", category = WardrobeCategory.SHOES, warmthLevel = WarmthLevel.HEAVY, style = OutfitStyle.CASUAL, colorHex = "#3F2E26", colorTone = ColorTone.WARM, maxWearBeforeWash = 10)
        )
        dao.insertAll(starterItems)
    }
}
