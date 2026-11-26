package com.fitcast.cuacagaya.data.local.dao

import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity

interface WardrobeDao {
    fun getAllItems(): List<WardrobeItemEntity>
    fun insertItem(item: WardrobeItemEntity)
    fun deleteItem(item: WardrobeItemEntity)
}
