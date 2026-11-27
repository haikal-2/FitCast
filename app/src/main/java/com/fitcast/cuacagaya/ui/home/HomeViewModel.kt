package com.fitcast.cuacagaya.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitcast.cuacagaya.data.local.entity.*
import com.fitcast.cuacagaya.data.repository.WardrobeRepository
import com.fitcast.cuacagaya.data.repository.WeatherRepository
import com.fitcast.cuacagaya.domain.engine.OutfitRecommendationEngine
import com.fitcast.cuacagaya.domain.engine.RecommendedOutfit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val weather: WeatherCacheEntity? = null,
    val recommendation: RecommendedOutfit? = null,
    val lockedCategories: Set<String> = emptySet(),
    val selectedStyle: String? = null,
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
                val w = weather ?: WeatherCacheEntity(
                    cityName = "Bandung",
                    temperature = 24.0,
                    feelsLike = 25.0,
                    humidity = 72,
                    windSpeed = 2.0,
                    weatherMain = "Clouds",
                    weatherDescription = "Berawan",
                    rainProbability = 35
                )
                val rec = engine.recommend(w, items, lockedItemsMap, _uiState.value.selectedStyle)
                _uiState.update { it.copy(weather = w, recommendation = rec) }
            }
        }
    }

    fun refreshWeatherByGps() = viewModelScope.launch {
        _uiState.update { it.copy(isLoadingWeather = true) }
        val result = weatherRepo.refreshWeatherByCurrentLocation()
        _uiState.update {
            it.copy(
                isLoadingWeather = false,
                statusMessage = if (result.isFailure) "Offline: menampilkan data cuaca terakhir" else null
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
        _uiState.update { it.copy(lockedCategories = lockedItemsMap.keys.toSet()) }
    }

    fun setStyleFilter(style: String?) {
        _uiState.update { it.copy(selectedStyle = style) }
        shuffleOutfit()
    }

    fun shuffleOutfit() {
        val weather = _uiState.value.weather ?: return
        val newRec = engine.recommend(weather, currentItems, lockedItemsMap, _uiState.value.selectedStyle)
        _uiState.update { it.copy(recommendation = newRec) }
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
}
