package com.fitcast.cuacagaya.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitcast.cuacagaya.ui.components.FitCastFloatingBottomNav
import com.fitcast.cuacagaya.ui.components.FitCastTab
import com.fitcast.cuacagaya.ui.home.HomeScreen
import com.fitcast.cuacagaya.ui.home.HomeViewModel
import com.fitcast.cuacagaya.ui.onboarding.AboutScreen
import com.fitcast.cuacagaya.ui.onboarding.OnboardingScreen
import com.fitcast.cuacagaya.ui.studio.StudioScreen
import com.fitcast.cuacagaya.ui.wardrobe.AddItemScreen
import com.fitcast.cuacagaya.ui.wardrobe.LaundryScreen
import com.fitcast.cuacagaya.ui.wardrobe.WardrobeScreen
import com.fitcast.cuacagaya.ui.wardrobe.WardrobeViewModel

@Composable
fun FitCastAppRoot(
    homeViewModel: HomeViewModel,
    wardrobeViewModel: WardrobeViewModel
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fitcast_prefs", Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_done", false)) }

    if (showOnboarding) {
        OnboardingScreen(
            onFinish = {
                prefs.edit().putBoolean("onboarding_done", true).apply()
                showOnboarding = false
            }
        )
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val dirtyItems by wardrobeViewModel.dirtyItems.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val currentTab = FitCastTab.entries.find { it.route == currentRoute }

    fun navigateToTab(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = FitCastTab.HOME.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(FitCastTab.HOME.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onAboutClick = { navController.navigate("about") },
                    onNavigateToWardrobe = { navigateToTab(FitCastTab.WARDROBE.route) },
                    onNavigateToStudio = { navigateToTab(FitCastTab.STUDIO.route) },
                    onNavigateToLaundry = { navigateToTab(FitCastTab.LAUNDRY.route) }
                )
            }
            composable(FitCastTab.STUDIO.route) {
                StudioScreen(
                    viewModel = homeViewModel,
                    onNavigateToWardrobe = { navigateToTab(FitCastTab.WARDROBE.route) },
                    onAboutClick = { navController.navigate("about") }
                )
            }
            composable(FitCastTab.WARDROBE.route) {
                WardrobeScreen(
                    viewModel = wardrobeViewModel,
                    onAddClick = { navController.navigate("add_item") },
                    weather = homeState.weather,
                    onAboutClick = { navController.navigate("about") },
                    onRefreshGps = homeViewModel::refreshWeatherByGps,
                    onSelectCity = homeViewModel::refreshWeatherByCity
                )
            }
            composable("add_item") {
                AddItemScreen(
                    viewModel = wardrobeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(FitCastTab.LAUNDRY.route) {
                LaundryScreen(
                    viewModel = wardrobeViewModel,
                    weather = homeState.weather,
                    onAboutClick = { navController.navigate("about") },
                    onRefreshGps = homeViewModel::refreshWeatherByGps,
                    onSelectCity = homeViewModel::refreshWeatherByCity
                )
            }
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }

        if (currentTab != null) {
            FitCastFloatingBottomNav(
                selectedTab = currentTab,
                onTabSelected = { tab -> navigateToTab(tab.route) },
                laundryCount = dirtyItems.size,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
