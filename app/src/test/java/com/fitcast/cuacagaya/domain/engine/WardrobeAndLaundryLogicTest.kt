package com.fitcast.cuacagaya.domain.engine

import com.fitcast.cuacagaya.data.local.dao.WardrobeDao
import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.OutfitStyle
import com.fitcast.cuacagaya.data.local.entity.SavedOutfitEntity
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WarmthLevel
import com.fitcast.cuacagaya.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WardrobeAndLaundryLogicTest {

    private class InMemoryWardrobeDao(initial: List<WardrobeItemEntity> = emptyList()) : WardrobeDao {
        private val itemsFlow = MutableStateFlow(initial)
        private val outfitsFlow = MutableStateFlow<List<SavedOutfitEntity>>(emptyList())
        private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1L

        override fun getAllItems(): Flow<List<WardrobeItemEntity>> = itemsFlow
        override suspend fun getAllItemsOnce(): List<WardrobeItemEntity> = itemsFlow.value
        override fun getDirtyItems(): Flow<List<WardrobeItemEntity>> =
            itemsFlow.map { list -> list.filter { !it.isClean }.sortedByDescending { it.wearCount } }

        override suspend fun getItemCount(): Int = itemsFlow.value.size

        override suspend fun insertItem(item: WardrobeItemEntity): Long {
            val assigned = if (item.id == 0L) item.copy(id = nextId++) else item
            itemsFlow.value = itemsFlow.value.filterNot { it.id == assigned.id } + assigned
            return assigned.id
        }

        override suspend fun insertAll(items: List<WardrobeItemEntity>) {
            items.forEach { insertItem(it) }
        }

        override suspend fun updateItem(item: WardrobeItemEntity) {
            itemsFlow.value = itemsFlow.value.map { if (it.id == item.id) item else it }
        }

        override suspend fun deleteItem(item: WardrobeItemEntity) {
            itemsFlow.value = itemsFlow.value.filterNot { it.id == item.id }
        }

        override suspend fun markItemWashed(itemId: Long) {
            itemsFlow.value = itemsFlow.value.map {
                if (it.id == itemId) it.copy(isClean = true, wearCount = 0) else it
            }
        }

        override suspend fun markAllWashed() {
            itemsFlow.value = itemsFlow.value.map {
                if (!it.isClean) it.copy(isClean = true, wearCount = 0) else it
            }
        }

        override suspend fun saveOutfit(outfit: SavedOutfitEntity) {
            outfitsFlow.value = outfitsFlow.value + outfit
        }

        override fun getSavedOutfits(): Flow<List<SavedOutfitEntity>> = outfitsFlow
    }

    private fun sampleWardrobe(): List<WardrobeItemEntity> = listOf(
        WardrobeItemEntity(
            id = 1,
            name = "Kaos Katun Putih",
            category = WardrobeCategory.TOP,
            warmthLevel = WarmthLevel.LIGHT,
            style = OutfitStyle.CASUAL,
            colorHex = "#F8FAFC",
            colorTone = ColorTone.NEUTRAL,
            wearCount = 0,
            maxWearBeforeWash = 1,
            isClean = true,
            materialDetail = "Katun Combed 24s",
            colorName = "White"
        ),
        WardrobeItemEntity(
            id = 2,
            name = "Celana Chino Cream",
            category = WardrobeCategory.BOTTOM,
            warmthLevel = WarmthLevel.MEDIUM,
            style = OutfitStyle.FORMAL,
            colorHex = "#C2B280",
            colorTone = ColorTone.WARM,
            wearCount = 1,
            maxWearBeforeWash = 3,
            isClean = true,
            materialDetail = "Katun Twill Stretch",
            colorName = "Cream"
        ),
        WardrobeItemEntity(
            id = 3,
            name = "Jaket Parasut Teal",
            category = WardrobeCategory.OUTER,
            warmthLevel = WarmthLevel.HEAVY,
            style = OutfitStyle.SPORT,
            colorHex = "#0F766E",
            colorTone = ColorTone.COOL,
            wearCount = 4,
            maxWearBeforeWash = 5,
            isClean = true,
            materialDetail = "Nilon Ripstop Anti-Air",
            colorName = "Teal"
        ),
        WardrobeItemEntity(
            id = 4,
            name = "Hoodie Abu Tua",
            category = WardrobeCategory.OUTER,
            warmthLevel = WarmthLevel.HEAVY,
            style = OutfitStyle.CASUAL,
            colorHex = "#475569",
            colorTone = ColorTone.NEUTRAL,
            wearCount = 4,
            maxWearBeforeWash = 4,
            isClean = false,
            materialDetail = "Fleece Tebal 360gsm",
            colorName = "Slate"
        )
    )

    @Test
    fun `wearing items increments wearCount and marks dirty once maxWearBeforeWash is reached`() = runBlocking {
        val dao = InMemoryWardrobeDao(sampleWardrobe())
        val repo = WardrobeRepository(dao)

        val initial = dao.getAllItemsOnce()
        // Wear Kaos (0/1 -> 1/1 dirty), Chino (1/3 -> 2/3 still clean), Jaket (4/5 -> 5/5 dirty)
        repo.wearItems(listOf(initial[0], initial[1], initial[2]))

        val updated = repo.allItems.first().associateBy { it.id }
        assertEquals(1, updated[1]?.wearCount)
        assertFalse("Kaos should become dirty at 1/1", updated[1]!!.isClean)

        assertEquals(2, updated[2]?.wearCount)
        assertTrue("Chino should remain clean at 2/3", updated[2]!!.isClean)

        assertEquals(5, updated[3]?.wearCount)
        assertFalse("Jaket should become dirty at 5/5", updated[3]!!.isClean)

        val dirtyIds = repo.dirtyItems.first().map { it.id }.toSet()
        assertEquals(setOf(1L, 3L, 4L), dirtyIds)
    }

    @Test
    fun `washing single item resets only that item while keeping others dirty`() = runBlocking {
        val dao = InMemoryWardrobeDao(sampleWardrobe())
        val repo = WardrobeRepository(dao)
        repo.toggleLaundryStatus(dao.getAllItemsOnce().first { it.id == 1L })

        assertEquals(2, repo.dirtyItems.first().size)

        repo.markItemWashed(4L)

        val hoodie = repo.allItems.first().first { it.id == 4L }
        assertTrue(hoodie.isClean)
        assertEquals(0, hoodie.wearCount)
        assertEquals(listOf(1L), repo.dirtyItems.first().map { it.id })
    }

    @Test
    fun `wash all cleans entire laundry queue and resets wear counts`() = runBlocking {
        val dao = InMemoryWardrobeDao(sampleWardrobe())
        val repo = WardrobeRepository(dao)
        repo.toggleLaundryStatus(dao.getAllItemsOnce().first { it.id == 1L })
        repo.toggleLaundryStatus(dao.getAllItemsOnce().first { it.id == 2L })

        assertEquals(3, repo.dirtyItems.first().size)

        repo.markAllWashed()

        assertTrue(repo.dirtyItems.first().isEmpty())
        val all = repo.allItems.first()
        assertTrue(all.all { it.isClean })
        assertEquals(0, all.first { it.id == 4L }.wearCount)
    }

    @Test
    fun `toggleLaundryStatus switches clean to dirty at maxWear and back to clean`() = runBlocking {
        val dao = InMemoryWardrobeDao(sampleWardrobe())
        val repo = WardrobeRepository(dao)

        val chino = dao.getAllItemsOnce().first { it.id == 2L }
        repo.toggleLaundryStatus(chino)

        val dirtyChino = repo.allItems.first().first { it.id == 2L }
        assertFalse(dirtyChino.isClean)
        assertEquals(chino.maxWearBeforeWash, dirtyChino.wearCount)

        repo.toggleLaundryStatus(dirtyChino)
        val cleanChino = repo.allItems.first().first { it.id == 2L }
        assertTrue(cleanChino.isClean)
        assertEquals(0, cleanChino.wearCount)
    }

    @Test
    fun `category and search filter match by name, colorName, material, and style`() {
        val items = sampleWardrobe()

        fun filterItems(category: String, query: String): List<WardrobeItemEntity> {
            return items
                .filter { category == "ALL" || it.category == category }
                .filter { item ->
                    query.isBlank() ||
                        item.name.contains(query, ignoreCase = true) ||
                        item.colorName.contains(query, ignoreCase = true) ||
                        item.materialDetail.contains(query, ignoreCase = true) ||
                        item.style.contains(query, ignoreCase = true)
                }
        }

        assertEquals(2, filterItems(WardrobeCategory.OUTER, "").size)
        assertEquals(listOf(3L), filterItems("ALL", "ripstop").map { it.id })
        assertEquals(listOf(2L), filterItems("ALL", "cream").map { it.id })
        assertEquals(listOf(3L), filterItems(WardrobeCategory.OUTER, "sport").map { it.id })
        assertTrue(filterItems("ALL", "jas hujan kuning").isEmpty())
    }
}
