package com.fitcast.cuacagaya.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitcast.cuacagaya.data.local.entity.SavedOutfitEntity
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WardrobeDao {
    @Query("SELECT * FROM wardrobe_items WHERE isDraft = 0 ORDER BY id DESC")
    fun getAllItems(): Flow<List<WardrobeItemEntity>>

    @Query("SELECT * FROM wardrobe_items WHERE isDraft = 0")
    suspend fun getAllItemsOnce(): List<WardrobeItemEntity>

    @Query("SELECT * FROM wardrobe_items WHERE isDraft = 0 AND isClean = 0 ORDER BY wearCount DESC")
    fun getDirtyItems(): Flow<List<WardrobeItemEntity>>

    @Query("SELECT COUNT(*) FROM wardrobe_items WHERE isDraft = 0")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WardrobeItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WardrobeItemEntity>)

    @Update
    suspend fun updateItem(item: WardrobeItemEntity)

    @Delete
    suspend fun deleteItem(item: WardrobeItemEntity)

    @Query("UPDATE wardrobe_items SET isClean = 1, wearCount = 0 WHERE id = :itemId")
    suspend fun markItemWashed(itemId: Long)

    @Query("UPDATE wardrobe_items SET isClean = 1, wearCount = 0 WHERE isClean = 0 AND isDraft = 0")
    suspend fun markAllWashed()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOutfit(outfit: SavedOutfitEntity)

    @Query("SELECT * FROM saved_outfits ORDER BY createdAt DESC")
    fun getSavedOutfits(): Flow<List<SavedOutfitEntity>>
}

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE id = 1")
    fun observeWeather(): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    suspend fun getCachedWeather(): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeather(weather: WeatherCacheEntity)
}
