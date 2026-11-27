package com.fitcast.cuacagaya.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.fitcast.cuacagaya.data.local.FitCastDatabase
import com.fitcast.cuacagaya.data.remote.WeatherApiService
import com.fitcast.cuacagaya.data.repository.WeatherRepository
import com.fitcast.cuacagaya.domain.engine.OutfitRecommendationEngine
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MorningBriefingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = FitCastDatabase.getInstance(applicationContext)
        val weatherRepo = WeatherRepository(
            applicationContext,
            WeatherApiService.create(),
            db.weatherDao()
        )

        weatherRepo.refreshWeatherByCurrentLocation()
        val weather = db.weatherDao().getCachedWeather() ?: return Result.success()
        val items = db.wardrobeDao().getAllItemsOnce()
        val engine = OutfitRecommendationEngine()
        val rec = engine.recommend(weather, items)

        val outfitSummary = listOfNotNull(rec.top?.name, rec.outer?.name, rec.bottom?.name)
            .joinToString(" + ")
            .ifBlank { "Cek lemari digitalmu sekarang" }

        val title = "FitCast Pagi: ${weather.cityName} ${weather.temperature.toInt()}°C"
        val body = "${weather.weatherDescription}. Saran: $outfitSummary. ${
            if (rec.bringUmbrella) "Jangan lupa bawa payung/jas hujan!" else ""
        }"

        showNotification(title, body)
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "fitcast_morning_briefing"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Briefing Outfit Pagi",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(1001, notification)
    }

    companion object {
        fun scheduleDailyMorning(context: Context, hour: Int = 6, minute: Int = 30) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<MorningBriefingWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FitCastMorningBriefing",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
