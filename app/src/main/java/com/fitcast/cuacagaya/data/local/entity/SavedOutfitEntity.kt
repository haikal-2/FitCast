package com.fitcast.cuacagaya.data.local.entity

data class SavedOutfitEntity(
    val id: Long = 0,
    val name: String,
    val dateSaved: Long,
    val itemIds: String
)
