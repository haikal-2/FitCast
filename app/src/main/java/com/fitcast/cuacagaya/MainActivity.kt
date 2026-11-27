package com.fitcast.cuacagaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitcast.cuacagaya.ui.home.HomeViewModel
import com.fitcast.cuacagaya.ui.navigation.FitCastAppRoot
import com.fitcast.cuacagaya.ui.wardrobe.WardrobeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as FitCastApplication).container

        val homeViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    container.weatherRepository,
                    container.wardrobeRepository,
                    container.recommendationEngine
                ) as T
            }
        })[HomeViewModel::class.java]

        val wardrobeViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WardrobeViewModel(
                    container.wardrobeRepository,
                    container.imageProcessor
                ) as T
            }
        })[WardrobeViewModel::class.java]

        setContent {
            val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colors) {
                FitCastAppRoot(
                    homeViewModel = homeViewModel,
                    wardrobeViewModel = wardrobeViewModel
                )
            }
        }
    }
}
