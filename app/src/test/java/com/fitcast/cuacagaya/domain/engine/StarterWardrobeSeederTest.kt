package com.fitcast.cuacagaya.domain.engine

import com.fitcast.cuacagaya.data.local.dao.WardrobeDao
import com.fitcast.cuacagaya.data.local.entity.SavedOutfitEntity
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StarterWardrobeSeederTest {

    private class FakeWardrobeDao : WardrobeDao {
        val inserted = mutableListOf<WardrobeItemEntity>()

        override fun getAllItems(): Flow<List<WardrobeItemEntity>> = MutableStateFlow(inserted)
        override fun getDraftItems(): Flow<List<WardrobeItemEntity>> = MutableStateFlow(inserted.filter { it.isDraft })
        override suspend fun getAllItemsOnce(): List<WardrobeItemEntity> = inserted.toList()
        override fun getDirtyItems(): Flow<List<WardrobeItemEntity>> =
            MutableStateFlow(inserted.filter { !it.isClean })
        override suspend fun getItemCount(): Int = inserted.size
        override suspend fun insertItem(item: WardrobeItemEntity): Long {
            inserted.add(item)
            return inserted.size.toLong()
        }
        override suspend fun insertAll(items: List<WardrobeItemEntity>) {
            inserted.addAll(items)
        }
        override suspend fun updateItem(item: WardrobeItemEntity) = Unit
        override suspend fun deleteItem(item: WardrobeItemEntity) = Unit
        override suspend fun publishDraft(itemId: Long) = Unit
        override suspend fun markItemWashed(itemId: Long) = Unit
        override suspend fun markAllWashed() = Unit
        override suspend fun saveOutfit(outfit: SavedOutfitEntity) = Unit
        override fun getSavedOutfits(): Flow<List<SavedOutfitEntity>> = MutableStateFlow(emptyList())
    }

    @Test
    fun `seeds 12 starter items with complete metadata and 2 laundry items`() = runBlocking {
        val dao = FakeWardrobeDao()

        StarterWardrobeSeeder.seedIfEmpty(dao)

        assertEquals(12, dao.inserted.size)
        assertEquals(10, dao.inserted.count { it.isClean })
        assertEquals(2, dao.inserted.count { !it.isClean })
        dao.inserted.forEach { item ->
            assertTrue("materialDetail blank on ${item.name}", item.materialDetail.isNotBlank())
            assertTrue("colorName blank on ${item.name}", item.colorName.isNotBlank())
            assertTrue("washCareTip blank on ${item.name}", item.washCareTip.isNotBlank())
            assertTrue("storageLocation blank on ${item.name}", item.storageLocation.isNotBlank())
            assertTrue("suitableWeathers blank on ${item.name}", item.suitableWeathers.isNotBlank())
            assertTrue("styleTags blank on ${item.name}", item.styleTags.isNotBlank())
            assertTrue("drawableResName blank on ${item.name}", item.drawableResName.isNotBlank())
            assertFalse("starter item should not be draft: ${item.name}", item.isDraft)
        }
    }

    @Test
    fun `does not duplicate items when database is already populated`() = runBlocking {
        val dao = FakeWardrobeDao()

        StarterWardrobeSeeder.seedIfEmpty(dao)
        StarterWardrobeSeeder.seedIfEmpty(dao)

        assertEquals(12, dao.inserted.size)
    }
}
