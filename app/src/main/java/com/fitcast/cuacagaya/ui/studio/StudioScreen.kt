package com.fitcast.cuacagaya.ui.studio

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Sync
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.fitcast.cuacagaya.ui.components.parseGarmentColor
import com.fitcast.cuacagaya.ui.home.CityPickerDialog
import com.fitcast.cuacagaya.ui.home.HomeUiState
import com.fitcast.cuacagaya.ui.home.HomeViewModel
import com.fitcast.cuacagaya.ui.home.NotificationBriefingDialog
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.LaundryRed
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.SurfaceCardDark
import com.fitcast.cuacagaya.ui.theme.VoltLime

private val StudioPanelDark = Color(0xFF141A18)

@Composable
fun StudioScreen(
    viewModel: HomeViewModel,
    onNavigateToWardrobe: () -> Unit,
    onAboutClick: () -> Unit
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

    StudioScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onLocationClick = { showCityDialog = true },
        onNotificationClick = { showNotificationDialog = true },
        onProfileClick = onAboutClick,
        onShuffleClick = viewModel::shuffleOutfit,
        onToggleLock = viewModel::toggleLock,
        onToggleFavorite = viewModel::toggleFavoriteOutfit,
        onQuickSwapItem = viewModel::selectItemForOutfit,
        onWearOutfit = viewModel::wearCurrentOutfit,
        onSeeAllWardrobe = onNavigateToWardrobe
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
private fun StudioScreenContent(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onLocationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onToggleLock: (String, WardrobeItemEntity?) -> Unit,
    onToggleFavorite: () -> Unit,
    onQuickSwapItem: (WardrobeItemEntity) -> Unit,
    onWearOutfit: () -> Unit,
    onSeeAllWardrobe: () -> Unit
) {
    val weather = state.weather
    val rec = state.recommendation
    val tempInt = weather?.temperature?.toInt() ?: 24
    val isRainy = rec?.bringUmbrella == true || (weather?.rainProbability ?: 0) >= 50
    val weatherEditionLabel = when {
        isRainy -> "HUJAN RINGAN"
        tempInt >= 30 -> "CERAH TERIK"
        tempInt <= 21 -> "SEJUK BERANGIN"
        else -> "BERAWAN NYAMAN"
    }
    val weatherPillLabel = when {
        isRainy -> "Hujan Ringan"
        tempInt >= 30 -> "Cerah Terik"
        tempInt <= 21 -> "Sejuk"
        else -> "Berawan"
    }

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
                temperatureCelsius = tempInt,
                onLocationClick = onLocationClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                hasUnreadNotification = true,
                notificationBadgeColor = VoltLime
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sub-header: Mix & Match Studio + Acak button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Mix & Match Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "EDISI $weatherEditionLabel • ${tempInt}°C",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = FitCastTheme.colors.mutedText
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, FitCastTheme.colors.subtleBorder),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onShuffleClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            tint = ElectricCobalt,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Acak",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Editorial Preview Canvas with Interactive Hotspots & Floating Dock
            StudioEditorialCanvas(
                recommendation = rec,
                tempInt = tempInt,
                weatherPillLabel = weatherPillLabel,
                lockedCategories = state.lockedCategories,
                selectedSlot = state.selectedStudioSlot,
                combinationIndex = state.combinationIndex,
                totalCombinations = state.totalCombinations,
                isFavorite = state.isFavoriteOutfit,
                onHotspotTap = { category, item -> onToggleLock(category, item) },
                onDockLockClick = {
                    val targetItem = when (state.selectedStudioSlot) {
                        WardrobeCategory.TOP -> rec?.top
                        WardrobeCategory.BOTTOM -> rec?.bottom
                        WardrobeCategory.SHOES -> rec?.shoes
                        else -> rec?.outer ?: rec?.top
                    }
                    val targetCat = targetItem?.category ?: WardrobeCategory.OUTER
                    onToggleLock(targetCat, targetItem)
                },
                onDockShuffleClick = onShuffleClick,
                onDockFavoriteClick = onToggleFavorite
            )

            // Bottom Dark Editorial Panel
            StudioDarkControlPanel(
                isRainy = isRainy,
                recommendation = rec,
                wardrobeItems = state.wardrobeItems,
                onWearOutfit = onWearOutfit,
                onQuickSwapItem = onQuickSwapItem,
                onSeeAllWardrobe = onSeeAllWardrobe
            )
        }
    }
}

@Composable
private fun StudioEditorialCanvas(
    recommendation: RecommendedOutfit?,
    tempInt: Int,
    weatherPillLabel: String,
    lockedCategories: Set<String>,
    selectedSlot: String,
    combinationIndex: Int,
    totalCombinations: Int,
    isFavorite: Boolean,
    onHotspotTap: (String, WardrobeItemEntity?) -> Unit,
    onDockLockClick: () -> Unit,
    onDockShuffleClick: () -> Unit,
    onDockFavoriteClick: () -> Unit
) {
    val activeSlotLocked = lockedCategories.contains(selectedSlot) || lockedCategories.isNotEmpty()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF4D565C),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(376.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF566066),
                            Color(0xFF6B757B),
                            Color(0xFF8C9499)
                        )
                    )
                )
                .drawBehind {
                    // Soft studio concrete light pool behind garments
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(PureWhite.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * 0.48f, size.height * 0.45f),
                            radius = size.width * 0.62f
                        )
                    )
                }
        ) {
            // Layered editorial flat-lay composition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 34.dp),
                contentAlignment = Alignment.Center
            ) {
                // Top (inner shirt layer)
                GarmentVisualImage(
                    item = recommendation?.top,
                    modifier = Modifier
                        .size(132.dp)
                        .offset(x = (-10).dp, y = (-26).dp)
                        .graphicsLayer { rotationZ = -3f }
                )
                // Outer jacket framing top
                if (recommendation?.outer != null) {
                    GarmentVisualImage(
                        item = recommendation.outer,
                        modifier = Modifier
                            .size(164.dp)
                            .offset(x = (-32).dp, y = (-52).dp)
                            .graphicsLayer { rotationZ = -6f }
                    )
                }
                // Bottom folded trousers angled below
                GarmentVisualImage(
                    item = recommendation?.bottom,
                    modifier = Modifier
                        .size(144.dp)
                        .offset(x = 18.dp, y = 76.dp)
                        .graphicsLayer { rotationZ = 10f }
                )
            }

            // Top-left white weather pill
            Surface(
                shape = CircleShape,
                color = PureWhite.copy(alpha = 0.94f),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = ElectricCobalt,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = ObsidianDark)) {
                                append("${tempInt}°C")
                            }
                            withStyle(SpanStyle(color = Color(0xFF6B7280))) {
                                append(" • $weatherPillLabel")
                            }
                        },
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp
                    )
                }
            }

            // Interactive Hotspot Pin 1: Outer
            val outerItem = recommendation?.outer
            if (outerItem != null) {
                HotspotPinLabel(
                    name = outerItem.name,
                    trait = extractShortTrait(outerItem, "Anti-Air"),
                    isLocked = lockedCategories.contains(WardrobeCategory.OUTER),
                    onClick = { onHotspotTap(WardrobeCategory.OUTER, outerItem) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 76.dp)
                )
            }

            // Interactive Hotspot Pin 2: Atasan
            val topItem = recommendation?.top
            if (topItem != null) {
                HotspotPinLabel(
                    name = topItem.name,
                    trait = extractShortTrait(topItem, "Katun Lembut"),
                    isLocked = lockedCategories.contains(WardrobeCategory.TOP),
                    onClick = { onHotspotTap(WardrobeCategory.TOP, topItem) },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 42.dp, top = 8.dp, end = 72.dp)
                )
            }

            // Interactive Hotspot Pin 3: Bawahan
            val bottomItem = recommendation?.bottom
            if (bottomItem != null) {
                HotspotPinLabel(
                    name = bottomItem.name,
                    trait = extractShortTrait(bottomItem, "Relaxed Fit"),
                    isLocked = lockedCategories.contains(WardrobeCategory.BOTTOM),
                    onClick = { onHotspotTap(WardrobeCategory.BOTTOM, bottomItem) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 68.dp, end = 72.dp)
                )
            }

            // Right floating vertical controls dock (Lock / Shuffle / Favorite)
            Surface(
                shape = CircleShape,
                color = ObsidianDark.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val lockBg by animateColorAsState(
                        targetValue = if (activeSlotLocked) VoltLime else SurfaceCardDark,
                        animationSpec = tween(200),
                        label = "lockDockBg"
                    )
                    val lockTint by animateColorAsState(
                        targetValue = if (activeSlotLocked) ObsidianDark else PureWhite,
                        animationSpec = tween(200),
                        label = "lockDockTint"
                    )

                    StudioDockCircleButton(
                        containerColor = lockBg,
                        contentDescription = if (activeSlotLocked) "Buka kunci item" else "Kunci item terpilih",
                        onClick = onDockLockClick
                    ) {
                        Icon(
                            imageVector = if (activeSlotLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            contentDescription = null,
                            tint = lockTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    StudioDockCircleButton(
                        containerColor = SurfaceCardDark,
                        contentDescription = "Acak kombinasi studio",
                        onClick = onDockShuffleClick
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    StudioDockCircleButton(
                        containerColor = SurfaceCardDark,
                        contentDescription = if (isFavorite) "Hapus dari favorit" else "Simpan ke favorit",
                        onClick = onDockFavoriteClick
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) LaundryRed else PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom overlay bar: Preview Kombinasi (left) & Kombinasi X / Y (right)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.94f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CenterFocusWeak,
                            contentDescription = null,
                            tint = ElectricCobalt,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Preview Kombinasi",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ObsidianDark
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = ObsidianDark.copy(alpha = 0.86f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f))
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.86f), fontWeight = FontWeight.SemiBold)) {
                                append("Kombinasi ")
                            }
                            withStyle(SpanStyle(color = VoltLime, fontWeight = FontWeight.ExtraBold)) {
                                append("$combinationIndex / $totalCombinations")
                            }
                        },
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HotspotPinLabel(
    name: String,
    trait: String,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(CircleShape)
            .clickable(
                role = Role.Button,
                onClickLabel = if (isLocked) "Buka kunci $name" else "Kunci $name",
                onClick = onClick
            )
            .semantics {
                contentDescription = "$name, $trait, ${if (isLocked) "terkunci" else "tidak terkunci"}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pin target circle
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isLocked) VoltLime.copy(alpha = 0.34f) else PureWhite.copy(alpha = 0.32f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isLocked) VoltLime else PureWhite),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(ObsidianDark)
                )
            }
        }

        // Dark pill label
        Surface(
            shape = CircleShape,
            color = ObsidianDark.copy(alpha = 0.88f),
            border = BorderStroke(
                width = 1.dp,
                color = if (isLocked) VoltLime else PureWhite.copy(alpha = 0.16f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = trait,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = if (isLocked) VoltLime else PureWhite.copy(alpha = 0.70f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StudioDockCircleButton(
    containerColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun StudioDarkControlPanel(
    isRainy: Boolean,
    recommendation: RecommendedOutfit?,
    wardrobeItems: List<WardrobeItemEntity>,
    onWearOutfit: () -> Unit,
    onQuickSwapItem: (WardrobeItemEntity) -> Unit,
    onSeeAllWardrobe: () -> Unit
) {
    val activeIds = remember(recommendation) {
        setOfNotNull(
            recommendation?.outer?.id,
            recommendation?.top?.id,
            recommendation?.bottom?.id,
            recommendation?.shoes?.id
        )
    }

    val paletteColors = remember(recommendation) {
        listOf(
            parseGarmentColor(recommendation?.outer?.colorHex, Color(0xFF0F766E)),
            parseGarmentColor(recommendation?.top?.colorHex, Color(0xFFF8FAFC)),
            parseGarmentColor(recommendation?.bottom?.colorHex, Color(0xFFC2B280)),
            parseGarmentColor(recommendation?.shoes?.colorHex, Color(0xFF1E293B))
        )
    }

    val panelTitle = if (isRainy) "Smart Rainy Layering" else "Balanced Daily Layering"
    val panelSubtitle = if (isRainy) {
        "Cocok untuk cuaca sejuk & berangin"
    } else {
        recommendation?.harmonyLabel ?: "Kombinasi seimbang untuk aktivitas harian"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = StudioPanelDark,
        contentColor = PureWhite,
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header row + Pakai button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = panelTitle,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        lineHeight = 24.sp,
                        color = PureWhite
                    )
                    Text(
                        text = panelSubtitle,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFA6B2AC)
                    )
                }

                Button(
                    onClick = onWearOutfit,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = ObsidianDark
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pakai",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // 4-color palette dots
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    paletteColors.forEachIndexed { idx, swatchColor ->
                        Box(
                            modifier = Modifier
                                .offset(x = (-6 * idx).dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(1.5.dp, StudioPanelDark, CircleShape)
                        )
                    }
                }
                Text(
                    text = "PALET 4 WARNA",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    color = Color(0xFF8C9993)
                )
            }

            HorizontalDivider(color = PureWhite.copy(alpha = 0.08f))

            // Quick-swap header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(VoltLime.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                            tint = VoltLime,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "Ganti Cepat Koleksi Lemari",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PureWhite
                    )
                }

                Text(
                    text = "LIHAT SEMUA",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = VoltLime,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onSeeAllWardrobe)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            // Quick-swap horizontal carousel
            val carouselItems = remember(wardrobeItems) {
                wardrobeItems.filter { it.isClean }.ifEmpty { wardrobeItems }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = carouselItems,
                    key = { it.id },
                    contentType = { it.category }
                ) { item ->
                    val isSelected = activeIds.contains(item.id)
                    StudioQuickSwapTile(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onQuickSwapItem(item) }
                    )
                }
            }

            // Bottom technical material info strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceCardDark,
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = ElectricCobalt,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isRainy) {
                            "Tahan cipratan air hujan & angin"
                        } else {
                            "Sirkulasi udara sejuk sepanjang hari"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = PureWhite.copy(alpha = 0.86f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isRainy) "Water-Repellent" else "Breathable",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFF93A19B),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioQuickSwapTile(
    item: WardrobeItemEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(76.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF232A27))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) VoltLime else PureWhite.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                GarmentVisualImage(
                    item = item,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(VoltLime)
                        .border(1.5.dp, StudioPanelDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Terpilih",
                        tint = ObsidianDark,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Text(
            text = shortGarmentLabel(item.name),
            fontFamily = PlusJakartaSans,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = if (isSelected) VoltLime else Color(0xFFB5C0BA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun shortGarmentLabel(fullName: String): String {
    val words = fullName.trim().split(" ").filter { it.isNotBlank() }
    return if (words.size >= 2) "${words[0]} ${words[1]}" else fullName
}

private fun extractShortTrait(item: WardrobeItemEntity, fallback: String): String {
    val afterBullet = item.materialDetail.substringAfter("•", "").trim()
    if (afterBullet.isNotBlank()) return afterBullet.split("&").first().trim()
    return item.materialDetail.split(" ").take(2).joinToString(" ").ifBlank { fallback }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 880)
@Composable
private fun StudioScreenPreview() {
    val outer = WardrobeItemEntity(
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
    val top = WardrobeItemEntity(
        id = 2,
        name = "Kaos Katun Putih",
        category = WardrobeCategory.TOP,
        warmthLevel = WarmthLevel.LIGHT,
        style = OutfitStyle.CASUAL,
        colorHex = "#F8FAFC",
        colorTone = ColorTone.NEUTRAL,
        materialDetail = "Katun Lembut • Breathable",
        colorName = "Putih"
    )
    val bottom = WardrobeItemEntity(
        id = 3,
        name = "Chino Cream",
        category = WardrobeCategory.BOTTOM,
        warmthLevel = WarmthLevel.MEDIUM,
        style = OutfitStyle.FORMAL,
        colorHex = "#C2B280",
        colorTone = ColorTone.WARM,
        materialDetail = "Twill • Relaxed Fit",
        colorName = "Cream"
    )
    val extraTop = WardrobeItemEntity(
        id = 4,
        name = "Kemeja Linen Beige",
        category = WardrobeCategory.TOP,
        warmthLevel = WarmthLevel.LIGHT,
        style = OutfitStyle.FORMAL,
        colorHex = "#D6C5B3",
        colorTone = ColorTone.WARM,
        materialDetail = "Linen Blend",
        colorName = "Beige"
    )
    val previewState = HomeUiState(
        weather = WeatherCacheEntity(
            cityName = "Bandung",
            temperature = 24.0,
            feelsLike = 24.0,
            humidity = 78,
            windSpeed = 3.2,
            weatherMain = "Rain",
            weatherDescription = "Hujan ringan",
            rainProbability = 60
        ),
        recommendation = RecommendedOutfit(
            top = top,
            bottom = bottom,
            outer = outer,
            shoes = null,
            weatherAdvice = "Siapkan payung",
            bringUmbrella = true,
            harmonyLabel = "Harmoni Netral"
        ),
        wardrobeItems = listOf(outer, top, bottom, extraTop),
        lockedCategories = setOf(WardrobeCategory.OUTER),
        combinationIndex = 1,
        totalCombinations = 4
    )

    FitCastTheme(darkTheme = false) {
        StudioScreenContent(
            state = previewState,
            snackbarHostState = remember { SnackbarHostState() },
            onLocationClick = {},
            onNotificationClick = {},
            onProfileClick = {},
            onShuffleClick = {},
            onToggleLock = { _, _ -> },
            onToggleFavorite = {},
            onQuickSwapItem = {},
            onWearOutfit = {},
            onSeeAllWardrobe = {}
        )
    }
}
