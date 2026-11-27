package com.fitcast.cuacagaya.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.fitcast.cuacagaya.ui.home.HomeScreen
import com.fitcast.cuacagaya.ui.home.HomeViewModel
import com.fitcast.cuacagaya.ui.onboarding.AboutScreen
import com.fitcast.cuacagaya.ui.onboarding.OnboardingScreen
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
    val bottomRoutes = listOf("home", "wardrobe", "laundry")

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Beranda") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "wardrobe",
                        onClick = { navController.navigate("wardrobe") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Checkroom, null) },
                        label = { Text("Lemari") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "laundry",
                        onClick = { navController.navigate("laundry") { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.LocalLaundryService, null) },
                        label = { Text("Laundry") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onAboutClick = { navController.navigate("about") }
                )
            }
            composable("wardrobe") {
                WardrobeScreen(
                    viewModel = wardrobeViewModel,
                    onAddClick = { navController.navigate("add_item") }
                )
            }
            composable("add_item") {
                AddItemScreen(
                    viewModel = wardrobeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("laundry") {
                LaundryScreen(viewModel = wardrobeViewModel)
            }
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
