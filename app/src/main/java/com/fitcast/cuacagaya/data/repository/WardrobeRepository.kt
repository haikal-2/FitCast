package com.fitcast.cuacagaya.data.repository

import com.fitcast.cuacagaya.data.local.dao.WardrobeDao
import com.fitcast.cuacagaya.data.local.entity.SavedOutfitEntity
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class WardrobeRepository(private val dao: WardrobeDao) {
    val allItems: Flow<List<WardrobeItemEntity>> = dao.getAllItems()
    val dirtyItems: Flow<List<WardrobeItemEntity>> = dao.getDirtyItems()
    val savedOutfits: Flow<List<SavedOutfitEntity>> = dao.getSavedOutfits()

    suspend fun addItem(item: WardrobeItemEntity): Long = dao.insertItem(item)
    suspend fun updateItem(item: WardrobeItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(item: WardrobeItemEntity) {
        dao.deleteItem(item)
        if (item.imagePath.isNotBlank()) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(item.imagePath)
                    if (file.exists()) file.delete()
                }
            }
        }
    }

    suspend fun markItemWashed(id: Long) = dao.markItemWashed(id)
    suspend fun markAllWashed() = dao.markAllWashed()
    suspend fun saveOutfit(outfit: SavedOutfitEntity) = dao.saveOutfit(outfit)

    suspend fun toggleLaundryStatus(item: WardrobeItemEntity) {
        val updated = if (item.isClean) {
            item.copy(isClean = false, wearCount = item.maxWearBeforeWash)
        } else {
            item.copy(isClean = true, wearCount = 0)
        }
        dao.updateItem(updated)
    }

    suspend fun wearItems(items: List<WardrobeItemEntity>) {
        items.forEach { item ->
            val nextWear = item.wearCount + 1
            val stillClean = nextWear < item.maxWearBeforeWash
            dao.updateItem(item.copy(wearCount = nextWear, isClean = stillClean))
        }
    }
}
