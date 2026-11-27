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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MlScanPhase {
    EMPTY,
    SCANNING,
    SCANNED
}

data class AddItemScanUiState(
    val phase: MlScanPhase = MlScanPhase.EMPTY,
    val sourceUri: Uri? = null,
    val cutoutPreviewPath: String? = null,
    val scanProgressPercent: Int = 0,
    val resolutionLabel: String = "1024 × 1024 px",
    val backgroundRemoved: Boolean = false,
    val showExtractedToast: Boolean = false
)

data class RecentlyAddedHighlight(
    val itemId: Long,
    val itemName: String,
    val category: String
)

class WardrobeViewModel(
    private val repository: WardrobeRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    val allItems: StateFlow<List<WardrobeItemEntity>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val draftItems: StateFlow<List<WardrobeItemEntity>> = repository.draftItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dirtyItems: StateFlow<List<WardrobeItemEntity>> = repository.dirtyItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isProcessingImage = MutableStateFlow(false)
    val isProcessingImage: StateFlow<Boolean> = _isProcessingImage.asStateFlow()

    private val _detectedSwatches = MutableStateFlow(DEFAULT_PALETTE_SWATCHES)
    val detectedSwatches: StateFlow<List<DetectedColorSwatch>> = _detectedSwatches.asStateFlow()

    private val _scanState = MutableStateFlow(AddItemScanUiState())
    val scanState: StateFlow<AddItemScanUiState> = _scanState.asStateFlow()

    private val _recentlyAdded = MutableStateFlow<RecentlyAddedHighlight?>(null)
    val recentlyAdded: StateFlow<RecentlyAddedHighlight?> = _recentlyAdded.asStateFlow()

    private var scanJob: Job? = null

    fun startPhotoScan(uri: Uri?, removeBg: Boolean = true) {
        scanJob?.cancel()
        if (uri == null) {
            resetScanState()
            return
        }

        scanJob = viewModelScope.launch {
            _scanState.value = AddItemScanUiState(
                phase = MlScanPhase.SCANNING,
                sourceUri = uri,
                cutoutPreviewPath = null,
                scanProgressPercent = 12,
                resolutionLabel = "Memindai...",
                backgroundRemoved = false,
                showExtractedToast = false
            )

            // Smooth staged progress alongside real ML Kit & Palette API inference.
            val progressJob = launch {
                delay(320L)
                _scanState.update { if (it.phase == MlScanPhase.SCANNING) it.copy(scanProgressPercent = 45) else it }
                delay(480L)
                _scanState.update { if (it.phase == MlScanPhase.SCANNING) it.copy(scanProgressPercent = 78) else it }
                delay(520L)
                _scanState.update { if (it.phase == MlScanPhase.SCANNING) it.copy(scanProgressPercent = 94) else it }
            }

            val analyzed = imageProcessor.analyzeAndCutoutPreview(uri, removeBackground = removeBg)
            delay(1550L)
            progressJob.cancel()

            if (analyzed != null && analyzed.swatches.isNotEmpty()) {
                _detectedSwatches.value = analyzed.swatches
            }

            val resLabel = if (analyzed != null && analyzed.widthPx > 0 && analyzed.heightPx > 0) {
                "${analyzed.widthPx} × ${analyzed.heightPx} px"
            } else {
                "1024 × 1024 px"
            }

            _scanState.value = AddItemScanUiState(
                phase = MlScanPhase.SCANNED,
                sourceUri = uri,
                cutoutPreviewPath = analyzed?.savedFilePath,
                scanProgressPercent = 100,
                resolutionLabel = resLabel,
                backgroundRemoved = removeBg,
                showExtractedToast = true
            )
        }
    }

    fun replayCurrentScan(removeBg: Boolean = true) {
        val currentUri = _scanState.value.sourceUri
        if (currentUri != null) {
            startPhotoScan(currentUri, removeBg)
        } else {
            // Allow interactive scan demonstration even before choosing a device photo.
            scanJob?.cancel()
            scanJob = viewModelScope.launch {
                _scanState.value = AddItemScanUiState(
                    phase = MlScanPhase.SCANNING,
                    sourceUri = null,
                    cutoutPreviewPath = null,
                    scanProgressPercent = 15,
                    resolutionLabel = "1024 × 1024 px",
                    backgroundRemoved = false,
                    showExtractedToast = false
                )
                delay(450L)
                _scanState.update { it.copy(scanProgressPercent = 48) }
                delay(520L)
                _scanState.update { it.copy(scanProgressPercent = 82) }
                delay(580L)
                _scanState.update {
                    it.copy(
                        phase = MlScanPhase.SCANNED,
                        scanProgressPercent = 100,
                        backgroundRemoved = removeBg,
                        showExtractedToast = true
                    )
                }
            }
        }
    }

    fun dismissScanToast() {
        _scanState.update { it.copy(showExtractedToast = false) }
    }

    fun resetScanState() {
        scanJob?.cancel()
        _scanState.value = AddItemScanUiState(phase = MlScanPhase.EMPTY)
        _detectedSwatches.value = DEFAULT_PALETTE_SWATCHES
    }

    fun addCustomSwatch(swatch: DetectedColorSwatch) {
        val current = _detectedSwatches.value
        if (current.none { it.hex.equals(swatch.hex, ignoreCase = true) }) {
            _detectedSwatches.value = current + swatch
        }
    }

    fun clearRecentlyAdded() {
        _recentlyAdded.value = null
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
        onSaved: (WardrobeItemEntity) -> Unit
    ) {
        if (_isProcessingImage.value) return
        viewModelScope.launch {
            _isProcessingImage.value = true
            val currentPreview = _scanState.value.cutoutPreviewPath
            val processed = if (photoUri != null) {
                imageProcessor.processAndSaveClothingPhoto(
                    uri = photoUri,
                    removeBackground = removeBg,
                    existingPreviewPath = currentPreview
                )
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
            val insertedId = repository.addItem(item)
            val savedEntity = item.copy(id = insertedId)
            if (!isDraft) {
                _recentlyAdded.value = RecentlyAddedHighlight(
                    itemId = insertedId,
                    itemName = savedEntity.name,
                    category = savedEntity.category
                )
            }
            _isProcessingImage.value = false
            onSaved(savedEntity)
        }
    }

    fun updateItem(item: WardrobeItemEntity) = viewModelScope.launch {
        repository.updateItem(item)
    }

    fun publishDraft(item: WardrobeItemEntity) = viewModelScope.launch {
        repository.publishDraft(item.id)
        _recentlyAdded.value = RecentlyAddedHighlight(
            itemId = item.id,
            itemName = item.name,
            category = item.category
        )
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
