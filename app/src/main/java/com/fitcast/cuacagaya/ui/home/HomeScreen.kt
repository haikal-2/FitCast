package com.fitcast.cuacagaya.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.OutfitStyle
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WarmthLevel
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity
import com.fitcast.cuacagaya.domain.engine.RecommendedOutfit
import com.fitcast.cuacagaya.ui.components.FitCastTopBar
import com.fitcast.cuacagaya.ui.components.GarmentVisualImage
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.LaundryRed
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.SoftDangerTint
import com.fitcast.cuacagaya.ui.theme.SoftIceTint
import com.fitcast.cuacagaya.ui.theme.VoltLime

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAboutClick: () -> Unit,
    onNavigateToWardrobe: () -> Unit = {},
    onNavigateToStudio: () -> Unit = {},
    onNavigateToLaundry: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    var showCityDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var locationPermissionDenied by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            locationPermissionDenied = false
            viewModel.refreshWeatherByGps()
        } else {
            locationPermissionDenied = true
            showCityDialog = true
        }
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    HomeScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onLocationClick = { showCityDialog = true },
        onNotificationClick = { showNotificationDialog = true },
        onProfileClick = onAboutClick,
        onStyleSelected = { style ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.setStyleFilter(style)
        },
        onNavigateToWardrobe = onNavigateToWardrobe,
        onNavigateToLaundry = onNavigateToLaundry,
        onNavigateToStudio = { slot ->
            if (slot != null) viewModel.selectStudioSlot(slot)
            onNavigateToStudio()
        },
        onShuffleClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.shuffleOutfit()
        },
        onWearOutfitClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.wearCurrentOutfit()
        }
    )

    if (showCityDialog) {
        CityPickerDialog(
            currentCity = state.weather?.cityName ?: "Bandung",
            showSettingsShortcut = locationPermissionDenied,
            onUseGps = {
                showCityDialog = false
                locationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onCitySelected = { city ->
                viewModel.refreshWeatherByCity(city)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false }
        )
    }

    if (showNotificationDialog) {
        NotificationBriefingDialog(
            weather = state.weather,
            laundryCount = state.laundryCount,
            onDismiss = { showNotificationDialog = false }
        )
    }
}

@Composable
private fun HomeScreenContent(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onLocationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onStyleSelected: (String?) -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToLaundry: () -> Unit,
    onNavigateToStudio: (String?) -> Unit,
    onShuffleClick: () -> Unit,
    onWearOutfitClick: () -> Unit
) {
    val weather = state.weather
    val rec = state.recommendation
    val fitColors = FitCastTheme.colors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 92.dp)
            )
        },
        topBar = {
            FitCastTopBar(
                cityName = weather?.cityName ?: "Bandung",
                temperatureCelsius = weather?.temperature?.toInt() ?: 24,
                onLocationClick = onLocationClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                isLoadingWeather = state.isLoadingWeather,
                hasUnreadNotification = state.laundryCount > 0 || (rec?.bringUmbrella == true),
                notificationBadgeColor = ElectricCobalt
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            StyleFilterRow(
                selectedStyle = state.selectedStyle,
                onStyleSelected = onStyleSelected
            )

            // Wardrobe statistics header & 3 Bento cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Statistik Lemari",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onNavigateToWardrobe)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lihat Semua",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ElectricCobalt
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = ElectricCobalt,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                BentoStatsRow(
                    totalCount = state.totalCount,
                    readyCount = state.readyCount,
                    laundryCount = state.laundryCount,
                    onWardrobeClick = onNavigateToWardrobe,
                    onLaundryClick = onNavigateToLaundry
                )
            }

            // Edge state alert when every garment in the wardrobe is in laundry
            if (state.totalCount > 0 && state.readyCount == 0) {
                AllClothesDirtyBanner(
                    laundryCount = state.laundryCount,
                    onOpenLaundry = onNavigateToLaundry,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Main Hero Card: Rekomendasi Hari Ini
            DailyRecommendationCard(
                weather = weather,
                recommendation = rec,
                selectedStyle = state.selectedStyle,
                shuffleNonce = state.shuffleNonce,
                onShuffleClick = onShuffleClick,
                onEditMixClick = { onNavigateToStudio(null) },
                onSlotClick = { slot -> onNavigateToStudio(slot) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Dark Weather Alert Banner
            WeatherAlertDarkBanner(
                weather = weather,
                recommendation = rec,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Primary Full-Width Volt Lime CTA with anti double-tap guard
            Button(
                onClick = onWearOutfitClick,
                enabled = !state.isWearingOutfit && rec != null,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = fitColors.voltLime,
                    contentColor = fitColors.onVoltLime
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 54.dp)
            ) {
                if (state.isWearingOutfit) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = fitColors.onVoltLime
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mencatat Outfit...",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pakai Outfit Ini Hari Ini",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AllClothesDirtyBanner(
    laundryCount: Int,
    onOpenLaundry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fitColors = FitCastTheme.colors
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = fitColors.softDangerTint,
        border = BorderStroke(1.dp, LaundryRed.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalLaundryService,
                contentDescription = null,
                tint = LaundryRed,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Semua pakaian perlu dicuci ($laundryCount item)",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LaundryRed
                )
                Text(
                    text = "Rekomendasi sementara menampilkan koleksi terakhir. Selesaikan cucian untuk reset.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                shape = CircleShape,
                color = LaundryRed,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onOpenLaundry)
            ) {
                Text(
                    text = "Laundry",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun StyleFilterRow(
    selectedStyle: String?,
    onStyleSelected: (String?) -> Unit
) {
    val options: List<Pair<String?, String>> = remember {
        listOf(
            null to "Semua Gaya",
            OutfitStyle.CASUAL to "Casual",
            OutfitStyle.FORMAL to "Formal",
            OutfitStyle.SPORT to "Sport"
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options, key = { it.first ?: "ALL" }, contentType = { "styleChip" }) { (styleKey, label) ->
            val isSelected = selectedStyle == styleKey
            Surface(
                shape = CircleShape,
                color = if (isSelected) ObsidianDark else MaterialTheme.colorScheme.surface,
                contentColor = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface,
                border = if (isSelected) BorderStroke(1.dp, VoltLime.copy(alpha = 0.25f)) else BorderStroke(1.dp, FitCastTheme.colors.subtleBorder),
                modifier = Modifier
                    .defaultMinSize(minHeight = 42.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Tab, onClick = { onStyleSelected(styleKey) })
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoStatsRow(
    totalCount: Int,
    readyCount: Int,
    laundryCount: Int,
    onWardrobeClick: () -> Unit,
    onLaundryClick: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BentoStatCard(
            label = "TOTAL\nBAJU",
            value = totalCount.toString(),
            containerColor = ElectricCobalt,
            labelColor = PureWhite.copy(alpha = 0.86f),
            valueColor = PureWhite,
            unitColor = PureWhite.copy(alpha = 0.80f),
            icon = Icons.Outlined.Checkroom,
            iconTint = PureWhite,
            iconBg = PureWhite.copy(alpha = 0.22f),
            border = null,
            onClick = onWardrobeClick,
            modifier = Modifier.weight(1f)
        )

        BentoStatCard(
            label = "SIAP\nPAKAI",
            value = readyCount.toString(),
            containerColor = fitColors.darkCardSurface,
            labelColor = PureWhite.copy(alpha = 0.84f),
            valueColor = VoltLime,
            unitColor = Color(0xFF9EA8A3),
            icon = Icons.Outlined.CheckCircle,
            iconTint = VoltLime,
            iconBg = VoltLime.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, fitColors.subtleBorder.copy(alpha = 0.45f)),
            onClick = onWardrobeClick,
            modifier = Modifier.weight(1f)
        )

        BentoStatCard(
            label = "LAUNDRY",
            value = laundryCount.toString(),
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = fitColors.mutedText,
            valueColor = MaterialTheme.colorScheme.onSurface,
            unitColor = fitColors.mutedText,
            icon = Icons.Outlined.LocalLaundryService,
            iconTint = LaundryRed,
            iconBg = SoftDangerTint,
            border = BorderStroke(1.dp, fitColors.subtleBorder),
            showAlertDot = laundryCount > 0,
            onClick = onLaundryClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BentoStatCard(
    label: String,
    value: String,
    containerColor: Color,
    labelColor: Color,
    valueColor: Color,
    unitColor: Color,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    border: BorderStroke?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAlertDot: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = border,
        shadowElevation = if (border != null) 2.dp else 4.dp,
        modifier = modifier
            .heightIn(min = 114.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 0.4.sp,
                    color = labelColor,
                    maxLines = 2
                )
                Box {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (showAlertDot) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LaundryRed)
                                .border(1.5.dp, containerColor, CircleShape)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    color = valueColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "pcs",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = unitColor,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyRecommendationCard(
    weather: WeatherCacheEntity?,
    recommendation: RecommendedOutfit?,
    selectedStyle: String?,
    shuffleNonce: Int,
    onShuffleClick: () -> Unit,
    onEditMixClick: () -> Unit,
    onSlotClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fitColors = FitCastTheme.colors
    val tempInt = weather?.temperature?.toInt() ?: 24
    val isRainy = recommendation?.bringUmbrella == true || (weather?.rainProbability ?: 0) >= 50
    val styleBadgeLabel = (selectedStyle ?: OutfitStyle.CASUAL).uppercase()
    val weatherTagLabel = when {
        isRainy -> "HUJAN RINGAN"
        tempInt >= 30 -> "CERAH TERIK"
        tempInt <= 21 -> "SEJUK"
        else -> "BERAWAN"
    }
    val weatherHeadline = when {
        isRainy -> "Potensi Hujan"
        tempInt >= 30 -> "Cerah Terik"
        tempInt <= 21 -> "Udara Sejuk"
        else -> "Berawan Nyaman"
    }
    val footerTrait = when {
        isRainy -> "Ringan & Tahan Gerimis"
        tempInt >= 30 -> "Adem & Menyerap Keringat"
        else -> "Seimbang & Nyaman"
    }

    val animatedOutfit = remember(recommendation, shuffleNonce) {
        shuffleNonce to recommendation
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, fitColors.subtleBorder),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REKOMENDASI CUACA",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.7.sp,
                    color = fitColors.mutedText
                )
                Surface(
                    shape = CircleShape,
                    color = ObsidianDark
                ) {
                    Text(
                        text = "$styleBadgeLabel • $weatherTagLabel",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = VoltLime,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Rekomendasi Hari Ini",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Dirancang sesuai cuaca ${weather?.cityName ?: "Bandung"} & mobilitas harianmu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = fitColors.mutedText
                )
            }

            // Soft weather sub-card with Shuffle & Edit Mix
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(fitColors.softIceTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isRainy -> Icons.Outlined.WaterDrop
                                tempInt >= 30 -> Icons.Outlined.WbSunny
                                else -> Icons.Outlined.Cloud
                            },
                            contentDescription = null,
                            tint = ElectricCobalt,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "${tempInt}°C • $weatherHeadline",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = weather?.weatherDescription ?: "Berawan tebal siang ini",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = fitColors.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ObsidianDark)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Acak rekomendasi outfit",
                                onClick = onShuffleClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = "Acak outfit",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, fitColors.subtleBorder),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onEditMixClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Edit Mix",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Animated 3 side-by-side garment cards on Shuffle
            AnimatedContent(
                targetState = animatedOutfit,
                transitionSpec = {
                    (fadeIn(tween(240)) + scaleIn(initialScale = 0.96f, animationSpec = tween(240))) togetherWith
                        fadeOut(tween(140))
                },
                label = "dailyOutfitShuffle"
            ) { (_, targetRec) ->
                val slots = listOf(
                    Triple(WardrobeCategory.OUTER, "Outer", targetRec?.outer ?: targetRec?.shoes),
                    Triple(WardrobeCategory.TOP, "Atasan", targetRec?.top),
                    Triple(WardrobeCategory.BOTTOM, "Bawahan", targetRec?.bottom)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    slots.forEach { (categoryKey, badgeLabel, item) ->
                        DailyOutfitItemMiniCard(
                            badgeLabel = badgeLabel,
                            item = item,
                            onClick = { onSlotClick(categoryKey) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider(color = fitColors.subtleBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(fitColors.successGreen)
                    )
                    Text(
                        text = "Sesuai ${tempInt}°C • $footerTrait",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = fitColors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = fitColors.softIceTint,
                    border = BorderStroke(1.dp, fitColors.subtleBorder)
                ) {
                    Text(
                        text = "Pilihan Utama",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = fitColors.onSoftIceTint,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyOutfitItemMiniCard(
    badgeLabel: String,
    item: WardrobeItemEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fitColors = FitCastTheme.colors
    val subtitleText = remember(item) {
        if (item == null) {
            "Tambah item"
        } else {
            val shortTrait = item.materialDetail
                .substringAfter("•", "")
                .trim()
                .ifBlank { item.materialDetail.split(" ").take(2).joinToString(" ") }
            val colorText = item.colorName.ifBlank { item.style.lowercase().replaceFirstChar { it.uppercase() } }
            if (shortTrait.isNotBlank()) "$colorText • $shortTrait" else colorText
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, fitColors.subtleBorder.copy(alpha = 0.7f)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fitColors.garmentBackdrop)
            ) {
                GarmentVisualImage(
                    item = item,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ObsidianDark.copy(alpha = 0.82f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item?.name ?: "Belum Ada",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitleText,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    color = fitColors.mutedText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WeatherAlertDarkBanner(
    weather: WeatherCacheEntity?,
    recommendation: RecommendedOutfit?,
    modifier: Modifier = Modifier
) {
    val fitColors = FitCastTheme.colors
    val rainProb = weather?.rainProbability ?: 60
    val isRainy = recommendation?.bringUmbrella == true || rainProb >= 50
    val tempInt = weather?.temperature?.toInt() ?: 24

    val title = when {
        isRainy -> "Siapkan Payung!"
        tempInt >= 30 -> "Cuaca Terik Hari Ini!"
        else -> "Udara Nyaman untuk Layering"
    }

    val bodyText = buildAnnotatedString {
        when {
            isRainy -> {
                withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.86f))) {
                    append("Potensi hujan $rainProb% siang nanti. FitCast menambahkan ")
                }
                withStyle(SpanStyle(color = VoltLime, fontWeight = FontWeight.Bold)) {
                    append("jaket tahan air")
                }
                withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.86f))) {
                    append(" ke rekomendasimu.")
                }
            }

            tempInt >= 30 -> {
                withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.86f))) {
                    append("Suhu mencapai ${tempInt}°C. FitCast memprioritaskan ")
                }
                withStyle(SpanStyle(color = VoltLime, fontWeight = FontWeight.Bold)) {
                    append("bahan katun adem")
                }
                withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.86f))) {
                    append(" agar tetap nyaman sepanjang hari.")
                }
            }

            else -> {
                withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.86f))) {
                    append("Suhu ${tempInt}°C terasa sejuk. Kombinasi ")
                }
                withStyle(SpanStyle(color = VoltLime, fontWeight = FontWeight.Bold)) {
                    append("harmoni netral")
                }
                withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.86f))) {
                    append(" siap menemani aktivitas harianmu.")
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = fitColors.darkCardSurface,
        border = BorderStroke(1.dp, fitColors.subtleBorder.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = VoltLime
            )
            Text(
                text = bodyText,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
internal fun CityPickerDialog(
    currentCity: String,
    onUseGps: () -> Unit,
    onCitySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    showSettingsShortcut: Boolean = false
) {
    val context = LocalContext.current
    val cities = remember { listOf("Bandung", "Bogor", "Jakarta", "Depok", "Surabaya", "Yogyakarta") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Lokasi Cuaca",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onUseGps,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = ObsidianDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gunakan GPS Otomatis", fontWeight = FontWeight.Bold)
                }
                if (showSettingsShortcut) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            runCatching { context.startActivity(intent) }
                            onDismiss()
                        },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Pengaturan Izin Lokasi", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                cities.forEach { city ->
                    val isCurrent = city.equals(currentCity, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) FitCastTheme.colors.softIceTint else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCitySelected(city) }
                    ) {
                        Text(
                            text = if (isCurrent) "$city (Aktif)" else city,
                            fontFamily = PlusJakartaSans,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) ElectricCobalt else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
internal fun NotificationBriefingDialog(
    weather: WeatherCacheEntity?,
    laundryCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var permissionRequestedNow by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionRequestedNow = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ringkasan FitCast Pagi",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${weather?.cityName ?: "Bandung"} ${weather?.temperature?.toInt() ?: 24}°C • Peluang hujan ${weather?.rainProbability ?: 60}%.",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (laundryCount > 0) {
                        "Ada $laundryCount pakaian di keranjang Laundry yang perlu segera dicuci."
                    } else {
                        "Semua pakaian di lemari dalam kondisi bersih dan siap pakai."
                    },
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionRequestedNow) {
                    val notifMissing = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                    if (notifMissing) {
                        Button(
                            onClick = {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VoltLime,
                                contentColor = ObsidianDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aktifkan Notifikasi Pagi 06:30", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Mengerti")
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun HomeScreenPreview() {
    val sampleOuter = WardrobeItemEntity(
        id = 1,
        name = "Jaket Parasut Teal",
        category = WardrobeCategory.OUTER,
        warmthLevel = WarmthLevel.HEAVY,
        style = OutfitStyle.SPORT,
        colorHex = "#0F766E",
        colorTone = ColorTone.COOL,
        materialDetail = "Nilon Ripstop • Anti-Air",
        colorName = "Teal"
    )
    val sampleTop = WardrobeItemEntity(
        id = 2,
        name = "Kaos Katun Putih",
        category = WardrobeCategory.TOP,
        warmthLevel = WarmthLevel.LIGHT,
        style = OutfitStyle.CASUAL,
        colorHex = "#F8FAFC",
        colorTone = ColorTone.NEUTRAL,
        materialDetail = "Katun Combed 24s • Breathable",
        colorName = "Putih"
    )
    val sampleBottom = WardrobeItemEntity(
        id = 3,
        name = "Chino Relaxed",
        category = WardrobeCategory.BOTTOM,
        warmthLevel = WarmthLevel.MEDIUM,
        style = OutfitStyle.CASUAL,
        colorHex = "#C2B280",
        colorTone = ColorTone.WARM,
        materialDetail = "Katun Twill • Nyaman",
        colorName = "Sand Beige"
    )
    val previewState = HomeUiState(
        weather = WeatherCacheEntity(
            cityName = "Bandung",
            temperature = 24.0,
            feelsLike = 24.0,
            humidity = 78,
            windSpeed = 3.0,
            weatherMain = "Rain",
            weatherDescription = "Berawan tebal siang ini",
            rainProbability = 60
        ),
        recommendation = RecommendedOutfit(
            top = sampleTop,
            bottom = sampleBottom,
            outer = sampleOuter,
            shoes = null,
            weatherAdvice = "Potensi hujan 60%",
            bringUmbrella = true,
            harmonyLabel = "Harmoni Netral"
        ),
        totalCount = 12,
        readyCount = 10,
        laundryCount = 2
    )

    FitCastTheme(darkTheme = false) {
        HomeScreenContent(
            state = previewState,
            snackbarHostState = remember { SnackbarHostState() },
            onLocationClick = {},
            onNotificationClick = {},
            onProfileClick = {},
            onStyleSelected = {},
            onNavigateToWardrobe = {},
            onNavigateToLaundry = {},
            onNavigateToStudio = {},
            onShuffleClick = {},
            onWearOutfitClick = {}
        )
    }
}
