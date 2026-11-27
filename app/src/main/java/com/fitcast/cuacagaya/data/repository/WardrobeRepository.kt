package com.fitcast.cuacagaya.data.repository

import com.fitcast.cuacagaya.data.local.dao.WardrobeDao
import com.fitcast.cuacagaya.data.local.entity.SavedOutfitEntity
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import kotlinx.coroutines.flow.Flow

class WardrobeRepository(private val dao: WardrobeDao) {
    val allItems: Flow<List<WardrobeItemEntity>> = dao.getAllItems()
    val dirtyItems: Flow<List<WardrobeItemEntity>> = dao.getDirtyItems()
    val savedOutfits: Flow<List<SavedOutfitEntity>> = dao.getSavedOutfits()

    suspend fun addItem(item: WardrobeItemEntity) = dao.insertItem(item)
    suspend fun deleteItem(item: WardrobeItemEntity) = dao.deleteItem(item)
    suspend fun markItemWashed(id: Long) = dao.markItemWashed(id)
    suspend fun markAllWashed() = dao.markAllWashed()
    suspend fun saveOutfit(outfit: SavedOutfitEntity) = dao.saveOutfit(outfit)

    suspend fun wearItems(items: List<WardrobeItemEntity>) {
        items.forEach { item ->
            val nextWear = item.wearCount + 1
            val stillClean = nextWear < item.maxWearBeforeWash
            dao.updateItem(item.copy(wearCount = nextWear, isClean = stillClean))
        }
    }
}
