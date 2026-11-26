package com.fitcast.cuacagaya.data.local.entity

data class WardrobeItemEntity(
    val id: Long = 0,
    val name: String,
    val category: String,
    val imagePath: String? = null,
    val isClean: Boolean = true
)
