package com.fitcast.cuacagaya.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showCityDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            viewModel.refreshWeatherByGps()
        } else {
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
        onStyleSelected = viewModel::setStyleFilter,
        onNavigateToWardrobe = onNavigateToWardrobe,
        onNavigateToLaundry = onNavigateToLaundry,
        onNavigateToStudio = { slot ->
            if (slot != null) viewModel.selectStudioSlot(slot)
            onNavigateToStudio()
        },
        onShuffleClick = viewModel::shuffleOutfit,
        onWearOutfitClick = viewModel::wearCurrentOutfit
    )

    if (showCityDialog) {
        CityPickerDialog(
            currentCity = state.weather?.cityName ?: "Bandung",
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

            // Main Hero Card: Rekomendasi Hari Ini
            DailyRecommendationCard(
                weather = weather,
                recommendation = rec,
                selectedStyle = state.selectedStyle,
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

            // Primary Full-Width Volt Lime CTA
            Button(
                onClick = onWearOutfitClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = fitColors.voltLime,
                    contentColor = fitColors.onVoltLime
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(54.dp)
            ) {
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
                border = if (isSelected) null else BorderStroke(1.dp, FitCastTheme.colors.subtleBorder),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Total Baju (Electric Cobalt)
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

        // Card 2: Siap Pakai (Obsidian Black)
        BentoStatCard(
            label = "SIAP\nPAKAI",
            value = readyCount.toString(),
            containerColor = Color(0xFF181A1B),
            labelColor = PureWhite.copy(alpha = 0.84f),
            valueColor = VoltLime,
            unitColor = Color(0xFF9EA8A3),
            icon = Icons.Outlined.CheckCircle,
            iconTint = VoltLime,
            iconBg = VoltLime.copy(alpha = 0.16f),
            border = null,
            onClick = onWardrobeClick,
            modifier = Modifier.weight(1f)
        )

        // Card 3: Laundry (Pure White / Surface)
        BentoStatCard(
            label = "LAUNDRY",
            value = laundryCount.toString(),
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = FitCastTheme.colors.mutedText,
            valueColor = MaterialTheme.colorScheme.onSurface,
            unitColor = FitCastTheme.colors.mutedText,
            icon = Icons.Outlined.LocalLaundryService,
            iconTint = LaundryRed,
            iconBg = SoftDangerTint,
            border = BorderStroke(1.dp, FitCastTheme.colors.subtleBorder),
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
            .height(114.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
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
                    fontSize = 27.sp,
                    lineHeight = 30.sp,
                    color = valueColor
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

    val slots = remember(recommendation) {
        listOf(
            Triple(WardrobeCategory.OUTER, "Outer", recommendation?.outer ?: recommendation?.shoes),
            Triple(WardrobeCategory.TOP, "Atasan", recommendation?.top),
            Triple(WardrobeCategory.BOTTOM, "Bawahan", recommendation?.bottom)
        )
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
            // Top label + dynamic Obsidian/VoltLime pill
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

            // Title & subtitle
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

                    // Circular dark Shuffle button
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

                    // White pill "Edit Mix" button
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
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
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

            // 3 Side-by-side garment cards
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

            HorizontalDivider(color = fitColors.subtleBorder)

            // Footer row
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
                        color = MaterialTheme.colorScheme.onSurface,
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
        color = Color(0xFF181A1B),
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
    onDismiss: () -> Unit
) {
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
                Spacer(modifier = Modifier.height(4.dp))
                cities.forEach { city ->
                    val isCurrent = city.equals(currentCity, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) SoftIceTint else Color.Transparent,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
