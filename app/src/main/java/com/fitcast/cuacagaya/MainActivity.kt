package com.fitcast.cuacagaya

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitcast.cuacagaya.ui.home.HomeViewModel
import com.fitcast.cuacagaya.ui.navigation.FitCastAppRoot
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.wardrobe.WardrobeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
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
            FitCastTheme {
                FitCastAppRoot(
                    homeViewModel = homeViewModel,
                    wardrobeViewModel = wardrobeViewModel
                )
            }
        }
    }
}
