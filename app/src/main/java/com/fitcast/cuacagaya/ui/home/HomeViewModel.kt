package com.fitcast.cuacagaya.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.SavedOutfitEntity
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity
import com.fitcast.cuacagaya.data.repository.WardrobeRepository
import com.fitcast.cuacagaya.data.repository.WeatherRepository
import com.fitcast.cuacagaya.domain.engine.OutfitRecommendationEngine
import com.fitcast.cuacagaya.domain.engine.RecommendedOutfit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val weather: WeatherCacheEntity? = null,
    val recommendation: RecommendedOutfit? = null,
    val wardrobeItems: List<WardrobeItemEntity> = emptyList(),
    val totalCount: Int = 0,
    val readyCount: Int = 0,
    val laundryCount: Int = 0,
    val lockedCategories: Set<String> = emptySet(),
    val selectedStyle: String? = null,
    val selectedStudioSlot: String = WardrobeCategory.OUTER,
    val combinationIndex: Int = 1,
    val totalCombinations: Int = 4,
    val isFavoriteOutfit: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val statusMessage: String? = null
)

class HomeViewModel(
    private val weatherRepo: WeatherRepository,
    private val wardrobeRepo: WardrobeRepository,
    private val engine: OutfitRecommendationEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentItems: List<WardrobeItemEntity> = emptyList()
    private val lockedItemsMap = mutableMapOf<String, WardrobeItemEntity>()

    init {
        viewModelScope.launch {
            combine(weatherRepo.cachedWeather, wardrobeRepo.allItems) { weather, items ->
                weather to items
            }.collect { (weather, items) ->
                currentItems = items
                val activeWeather = weather ?: WeatherCacheEntity(
                    cityName = "Bandung",
                    temperature = 24.0,
                    feelsLike = 24.0,
                    humidity = 78,
                    windSpeed = 3.2,
                    weatherMain = "Rain",
                    weatherDescription = "Berawan tebal siang ini",
                    rainProbability = 60
                )
                val rec = engine.recommend(activeWeather, items, lockedItemsMap, _uiState.value.selectedStyle)
                val cleanCount = items.count { it.isClean && !it.isDraft }
                val dirtyCount = items.count { !it.isClean && !it.isDraft }
                val totalNonDraft = items.count { !it.isDraft }
                val combos = computeCombinationSlots(items)

                _uiState.update { state ->
                    state.copy(
                        weather = activeWeather,
                        recommendation = rec,
                        wardrobeItems = items.filter { !it.isDraft },
                        totalCount = totalNonDraft,
                        readyCount = cleanCount,
                        laundryCount = dirtyCount,
                        totalCombinations = combos,
                        combinationIndex = state.combinationIndex.coerceIn(1, combos)
                    )
                }
            }
        }
    }

    private fun computeCombinationSlots(items: List<WardrobeItemEntity>): Int {
        val clean = items.filter { it.isClean && !it.isDraft }
        val tops = clean.count { it.category == WardrobeCategory.TOP }.coerceAtLeast(1)
        val bottoms = clean.count { it.category == WardrobeCategory.BOTTOM }.coerceAtLeast(1)
        return (tops * bottoms).coerceIn(1, 6).let { minOf(it, 4) }
    }

    fun refreshWeatherByGps() = viewModelScope.launch {
        _uiState.update { it.copy(isLoadingWeather = true) }
        val result = weatherRepo.refreshWeatherByCurrentLocation()
        _uiState.update {
            it.copy(
                isLoadingWeather = false,
                statusMessage = if (result.isFailure) "Offline: menampilkan data cuaca terakhir" else "Lokasi GPS & cuaca diperbarui"
            )
        }
    }

    fun refreshWeatherByCity(city: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoadingWeather = true) }
        val result = weatherRepo.refreshWeatherByCity(city)
        _uiState.update {
            it.copy(
                isLoadingWeather = false,
                statusMessage = if (result.isFailure) "Gagal memuat $city, memakai cache offline" else "Cuaca $city diperbarui"
            )
        }
    }

    fun toggleLock(category: String, item: WardrobeItemEntity?) {
        if (item == null) return
        if (lockedItemsMap.containsKey(category)) {
            lockedItemsMap.remove(category)
        } else {
            lockedItemsMap[category] = item
        }
        _uiState.update {
            it.copy(
                selectedStudioSlot = category,
                lockedCategories = lockedItemsMap.keys.toSet()
            )
        }
    }

    fun selectStudioSlot(category: String) {
        _uiState.update { it.copy(selectedStudioSlot = category) }
    }

    fun selectItemForOutfit(item: WardrobeItemEntity) {
        val currentRec = _uiState.value.recommendation ?: return
        val updated = when (item.category) {
            WardrobeCategory.TOP -> currentRec.copy(top = item)
            WardrobeCategory.BOTTOM -> currentRec.copy(bottom = item)
            WardrobeCategory.OUTER -> currentRec.copy(outer = item)
            WardrobeCategory.SHOES -> currentRec.copy(shoes = item)
            else -> currentRec
        }
        val harmony = resolveHarmonyLabel(updated.top, updated.bottom)
        lockedItemsMap[item.category] = item
        _uiState.update {
            it.copy(
                recommendation = updated.copy(harmonyLabel = harmony),
                selectedStudioSlot = item.category,
                lockedCategories = lockedItemsMap.keys.toSet(),
                isFavoriteOutfit = false
            )
        }
    }

    fun setStyleFilter(style: String?) {
        _uiState.update { it.copy(selectedStyle = style) }
        shuffleOutfit()
    }

    fun shuffleOutfit() {
        val weather = _uiState.value.weather ?: return
        val newRec = engine.recommend(weather, currentItems, lockedItemsMap, _uiState.value.selectedStyle)
        _uiState.update { state ->
            val nextIndex = (state.combinationIndex % state.totalCombinations.coerceAtLeast(1)) + 1
            state.copy(
                recommendation = newRec,
                combinationIndex = nextIndex,
                isFavoriteOutfit = false
            )
        }
    }

    fun toggleFavoriteOutfit() = viewModelScope.launch {
        val rec = _uiState.value.recommendation ?: return@launch
        val nextFav = !_uiState.value.isFavoriteOutfit
        if (nextFav) {
            wardrobeRepo.saveOutfit(
                SavedOutfitEntity(
                    outfitName = "Favorit ${_uiState.value.weather?.cityName ?: "Bandung"}",
                    topId = rec.top?.id,
                    bottomId = rec.bottom?.id,
                    outerId = rec.outer?.id,
                    shoesId = rec.shoes?.id,
                    targetWeather = _uiState.value.weather?.weatherMain ?: "Rain"
                )
            )
        }
        _uiState.update {
            it.copy(
                isFavoriteOutfit = nextFav,
                statusMessage = if (nextFav) "Kombinasi disimpan ke Favorit!" else "Dihapus dari Favorit"
            )
        }
    }

    fun wearCurrentOutfit() = viewModelScope.launch {
        val rec = _uiState.value.recommendation ?: return@launch
        val worn = listOfNotNull(rec.top, rec.bottom, rec.outer, rec.shoes)
        wardrobeRepo.wearItems(worn)
        wardrobeRepo.saveOutfit(
            SavedOutfitEntity(
                outfitName = "Outfit ${_uiState.value.weather?.cityName ?: "Hari Ini"}",
                topId = rec.top?.id,
                bottomId = rec.bottom?.id,
                outerId = rec.outer?.id,
                shoesId = rec.shoes?.id,
                targetWeather = _uiState.value.weather?.weatherMain ?: "Clouds"
            )
        )
        _uiState.update { it.copy(statusMessage = "Outfit dicatat dipakai hari ini!") }
    }

    fun clearMessage() = _uiState.update { it.copy(statusMessage = null) }

    private fun resolveHarmonyLabel(top: WardrobeItemEntity?, bottom: WardrobeItemEntity?): String {
        if (top == null || bottom == null) return "Lengkapi Lemari"
        return when {
            top.colorTone == ColorTone.NEUTRAL || bottom.colorTone == ColorTone.NEUTRAL -> "Harmoni Netral"
            top.colorTone != bottom.colorTone -> "Harmoni Kontras Seimbang"
            else -> "Harmoni Senada"
        }
    }
}
