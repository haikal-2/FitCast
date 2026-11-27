package com.fitcast.cuacagaya

import android.app.Application
import com.fitcast.cuacagaya.data.local.FitCastDatabase
import com.fitcast.cuacagaya.data.remote.WeatherApiService
import com.fitcast.cuacagaya.data.repository.WardrobeRepository
import com.fitcast.cuacagaya.data.repository.WeatherRepository
import com.fitcast.cuacagaya.domain.engine.OutfitRecommendationEngine
import com.fitcast.cuacagaya.domain.engine.StarterWardrobeSeeder
import com.fitcast.cuacagaya.domain.processor.ImageProcessor
import com.fitcast.cuacagaya.worker.MorningBriefingWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FitCastApplication : Application() {
    lateinit var container: AppContainer
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        applicationScope.launch {
            runCatching {
                StarterWardrobeSeeder.seedIfEmpty(container.database.wardrobeDao())
            }
        }
        MorningBriefingWorker.scheduleDailyMorning(this)
    }
}

class AppContainer(application: Application) {
    val database: FitCastDatabase by lazy { FitCastDatabase.getInstance(application) }
    private val weatherApi: WeatherApiService by lazy { WeatherApiService.create() }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(application, weatherApi, database.weatherDao())
    }
    val wardrobeRepository: WardrobeRepository by lazy {
        WardrobeRepository(database.wardrobeDao())
    }
    val imageProcessor: ImageProcessor by lazy { ImageProcessor(application) }
    val recommendationEngine: OutfitRecommendationEngine by lazy { OutfitRecommendationEngine() }
}
