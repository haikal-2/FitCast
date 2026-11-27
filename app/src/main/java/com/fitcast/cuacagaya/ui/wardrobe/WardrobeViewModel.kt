package com.fitcast.cuacagaya.ui.wardrobe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.repository.WardrobeRepository
import com.fitcast.cuacagaya.domain.processor.ImageProcessor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WardrobeViewModel(
    private val repository: WardrobeRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    val allItems: StateFlow<List<WardrobeItemEntity>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dirtyItems: StateFlow<List<WardrobeItemEntity>> = repository.dirtyItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isProcessingImage = MutableStateFlow(false)
    val isProcessingImage: StateFlow<Boolean> = _isProcessingImage.asStateFlow()

    fun addClothingWithPhoto(
        name: String,
        category: String,
        warmthLevel: String,
        style: String,
        maxWear: Int,
        photoUri: Uri?,
        removeBg: Boolean,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            _isProcessingImage.value = true
            val processed = if (photoUri != null) {
                imageProcessor.processAndSaveClothingPhoto(photoUri, removeBg)
            } else null

            val item = WardrobeItemEntity(
                name = name.trim(),
                category = category,
                warmthLevel = warmthLevel,
                style = style,
                colorHex = processed?.dominantColorHex ?: "#475569",
                colorTone = processed?.colorTone ?: "NEUTRAL",
                imagePath = processed?.savedFilePath ?: "",
                maxWearBeforeWash = maxWear.coerceIn(1, 10)
            )
            repository.addItem(item)
            _isProcessingImage.value = false
            onFinished()
        }
    }

    fun deleteItem(item: WardrobeItemEntity) = viewModelScope.launch {
        repository.deleteItem(item)
    }

    fun washItem(itemId: Long) = viewModelScope.launch {
        repository.markItemWashed(itemId)
    }

    fun washAll() = viewModelScope.launch {
        repository.markAllWashed()
    }
}
