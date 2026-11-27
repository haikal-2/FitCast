package com.fitcast.cuacagaya.ui.wardrobe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.repository.WardrobeRepository
import com.fitcast.cuacagaya.domain.processor.DetectedColorSwatch
import com.fitcast.cuacagaya.domain.processor.ImageProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    private val _detectedSwatches = MutableStateFlow(DEFAULT_PALETTE_SWATCHES)
    val detectedSwatches: StateFlow<List<DetectedColorSwatch>> = _detectedSwatches.asStateFlow()

    fun extractPaletteFromUri(uri: Uri?) {
        if (uri == null) {
            _detectedSwatches.value = DEFAULT_PALETTE_SWATCHES
            return
        }
        viewModelScope.launch {
            val extracted = imageProcessor.extractColorSwatches(uri)
            if (extracted.isNotEmpty()) {
                _detectedSwatches.value = extracted
            }
        }
    }

    fun resetDetectedSwatches() {
        _detectedSwatches.value = DEFAULT_PALETTE_SWATCHES
    }

    fun addClothingWithPhoto(
        name: String,
        category: String,
        warmthLevel: String,
        style: String,
        maxWear: Int,
        photoUri: Uri?,
        removeBg: Boolean,
        colorHex: String = "#3D5A40",
        colorName: String = "Olive",
        colorTone: String = ColorTone.NEUTRAL,
        materialDetail: String = "Katun Twill",
        suitableWeathers: String = "Hujan Ringan,Berangin",
        styleTags: String = "Casual,Streetwear",
        isDraft: Boolean = false,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            _isProcessingImage.value = true
            val processed = if (photoUri != null) {
                imageProcessor.processAndSaveClothingPhoto(photoUri, removeBg)
            } else null

            val defaultStorage = when (category) {
                WardrobeCategory.TOP -> "Laci Atasan"
                WardrobeCategory.BOTTOM -> "Rak Bawahan"
                WardrobeCategory.OUTER -> "Lemari Utama"
                WardrobeCategory.SHOES -> "Rak Sepatu"
                else -> "Laci Aksesoris"
            }

            val item = WardrobeItemEntity(
                name = name.trim(),
                category = category,
                warmthLevel = warmthLevel,
                style = style,
                colorHex = colorHex.ifBlank { processed?.dominantColorHex ?: "#3D5A40" },
                colorTone = colorTone.ifBlank { processed?.colorTone ?: ColorTone.NEUTRAL },
                imagePath = processed?.savedFilePath ?: "",
                maxWearBeforeWash = maxWear.coerceIn(1, 10),
                materialDetail = materialDetail,
                colorName = colorName,
                washCareTip = "Air Dingin (30°C)",
                storageLocation = defaultStorage,
                suitableWeathers = suitableWeathers,
                styleTags = styleTags,
                isDraft = isDraft
            )
            repository.addItem(item)
            _isProcessingImage.value = false
            onFinished()
        }
    }

    fun updateItem(item: WardrobeItemEntity) = viewModelScope.launch {
        repository.updateItem(item)
    }

    fun toggleLaundryStatus(item: WardrobeItemEntity) = viewModelScope.launch {
        repository.toggleLaundryStatus(item)
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

    companion object {
        val DEFAULT_PALETTE_SWATCHES = listOf(
            DetectedColorSwatch(name = "Olive", hex = "#3D5A40", tone = ColorTone.COOL),
            DetectedColorSwatch(name = "Dark Moss", hex = "#243526", tone = ColorTone.NEUTRAL),
            DetectedColorSwatch(name = "Sage", hex = "#A3B18A", tone = ColorTone.COOL),
            DetectedColorSwatch(name = "Silver", hex = "#DFDFE0", tone = ColorTone.NEUTRAL)
        )
    }
}
