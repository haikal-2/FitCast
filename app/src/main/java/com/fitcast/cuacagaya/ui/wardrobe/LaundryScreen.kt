package com.fitcast.cuacagaya.ui.wardrobe

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.fitcast.cuacagaya.ui.components.FitCastTopBar
import com.fitcast.cuacagaya.ui.components.GarmentVisualImage
import com.fitcast.cuacagaya.ui.home.CityPickerDialog
import com.fitcast.cuacagaya.ui.home.NotificationBriefingDialog
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.LaundryRed
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.SuccessGreen
import com.fitcast.cuacagaya.ui.theme.VoltLime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class LaundryFilterTab(val label: String) {
    ALL("Semua Perlu Dicuci"),
    HIGH_PRIORITY("Prioritas Tinggi"),
    DELICATE("Bahan Halus")
}

@Composable
fun LaundryScreen(
    viewModel: WardrobeViewModel,
    weather: WeatherCacheEntity? = null,
    onAboutClick: () -> Unit = {},
    onRefreshGps: () -> Unit = {},
    onSelectCity: (String) -> Unit = {}
) {
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val dirtyItems by viewModel.dirtyItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCityDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    val recentlyWashedIds = remember { mutableStateListOf<Long>() }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) onRefreshGps() else showCityDialog = true
    }

    LaundryScreenContent(
        allItems = allItems,
        dirtyItems = dirtyItems,
        recentlyWashedIds = recentlyWashedIds,
        weather = weather,
        snackbarHostState = snackbarHostState,
        onLocationClick = { showCityDialog = true },
        onNotificationClick = { showNotificationDialog = true },
        onProfileClick = onAboutClick,
        onMarkItemWashed = { item ->
            if (!recentlyWashedIds.contains(item.id)) {
                recentlyWashedIds.add(0, item.id)
            }
            viewModel.washItem(item.id)
            scope.launch {
                snackbarHostState.showSnackbar("${item.name} selesai dicuci & siap dipakai!")
            }
        },
        onMarkAllWashed = {
            dirtyItems.forEach { item ->
                if (!recentlyWashedIds.contains(item.id)) {
                    recentlyWashedIds.add(0, item.id)
                }
            }
            viewModel.washAll()
            scope.launch {
                snackbarHostState.showSnackbar("Semua cucian selesai dicuci!")
            }
        }
    )

    if (showCityDialog) {
        CityPickerDialog(
            currentCity = weather?.cityName ?: "Bandung",
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
                onSelectCity(city)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false }
        )
    }

    if (showNotificationDialog) {
        NotificationBriefingDialog(
            weather = weather,
            laundryCount = dirtyItems.size,
            onDismiss = { showNotificationDialog = false }
        )
    }
}

@Composable
private fun LaundryScreenContent(
    allItems: List<WardrobeItemEntity>,
    dirtyItems: List<WardrobeItemEntity>,
    recentlyWashedIds: List<Long>,
    weather: WeatherCacheEntity?,
    snackbarHostState: SnackbarHostState,
    onLocationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMarkItemWashed: (WardrobeItemEntity) -> Unit,
    onMarkAllWashed: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf(LaundryFilterTab.ALL) }
    var showFullHistory by remember { mutableStateOf(false) }
    var confirmingIds by remember { mutableStateOf(emptySet<Long>()) }
    var collapsingIds by remember { mutableStateOf(emptySet<Long>()) }

    val filteredDirty = remember(dirtyItems, selectedFilter) {
        when (selectedFilter) {
            LaundryFilterTab.ALL -> dirtyItems
            LaundryFilterTab.HIGH_PRIORITY -> dirtyItems.filter {
                it.maxWearBeforeWash <= 2 || it.wearCount >= it.maxWearBeforeWash
            }

            LaundryFilterTab.DELICATE -> dirtyItems.filter { item ->
                item.washCareTip.contains("Lembut", ignoreCase = true) ||
                    item.washCareTip.contains("Tangan", ignoreCase = true) ||
                    item.materialDetail.contains("Fleece", ignoreCase = true) ||
                    item.materialDetail.contains("Linen", ignoreCase = true) ||
                    item.materialDetail.contains("Rajut", ignoreCase = true)
            }
        }
    }

    val cleanItems = remember(allItems, recentlyWashedIds.toList()) {
        val clean = allItems.filter { it.isClean && !it.isDraft }
        val orderMap = recentlyWashedIds.withIndex().associate { it.value to it.index }
        clean.sortedWith(
            compareBy<WardrobeItemEntity> { orderMap[it.id] ?: Int.MAX_VALUE }
                .thenByDescending { it.id }
        )
    }

    val visibleCleanItems = remember(cleanItems, showFullHistory) {
        if (showFullHistory) cleanItems else cleanItems.take(4)
    }

    fun triggerAnimatedWash(item: WardrobeItemEntity) {
        if (confirmingIds.contains(item.id) || collapsingIds.contains(item.id)) return
        scope.launch {
            confirmingIds = confirmingIds + item.id
            delay(280L)
            collapsingIds = collapsingIds + item.id
            delay(300L)
            onMarkItemWashed(item)
            confirmingIds = confirmingIds - item.id
            collapsingIds = collapsingIds - item.id
        }
    }

    fun triggerAnimatedWashAll() {
        if (dirtyItems.isEmpty()) return
        val targets = dirtyItems.map { it.id }.toSet()
        scope.launch {
            confirmingIds = confirmingIds + targets
            delay(280L)
            collapsingIds = collapsingIds + targets
            delay(320L)
            onMarkAllWashed()
            confirmingIds = confirmingIds - targets
            collapsingIds = collapsingIds - targets
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 96.dp)
            )
        },
        topBar = {
            FitCastTopBar(
                cityName = weather?.cityName ?: "Bandung",
                temperatureCelsius = weather?.temperature?.toInt() ?: 24,
                onLocationClick = onLocationClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                hasUnreadNotification = dirtyItems.isNotEmpty(),
                notificationBadgeColor = LaundryRed,
                weatherIconTint = PureWhite.copy(alpha = 0.85f)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 112.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: LAUNDRY\nTRACKER + Cuci Semua CTA
            item(key = "laundry_header", contentType = "header") {
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
                            text = "LAUNDRY\nTRACKER",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 27.sp,
                            lineHeight = 30.sp,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Kelola siklus & antrean cuci pakaian",
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = fitColors.mutedText
                        )
                    }

                    Button(
                        onClick = { triggerAnimatedWashAll() },
                        enabled = dirtyItems.isNotEmpty(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltLime,
                            contentColor = ObsidianDark,
                            disabledContainerColor = fitColors.softIceTint,
                            disabledContentColor = fitColors.mutedText
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .defaultMinSize(minHeight = 46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalLaundryService,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cuci Semua",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Status row: • PERLU TINDAKAN SEGERA | 2 Item Menunggu
            item(key = "laundry_status_bar", contentType = "statusBar") {
                val hasDirty = dirtyItems.isNotEmpty()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (hasDirty) LaundryRed else SuccessGreen)
                        )
                        Text(
                            text = if (hasDirty) "PERLU TINDAKAN SEGERA" else "SEMUA PAKAIAN BERSIH",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (hasDirty) fitColors.softDangerTint else Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "${dirtyItems.size} Item Menunggu",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (hasDirty) LaundryRed else SuccessGreen,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Cycle Recommendation Card with auto-calculated Water & Spin time
            item(key = "cycle_recommendation", contentType = "cycleCard") {
                CycleRecommendationCard(dirtyItems = dirtyItems)
            }

            // Filter Chips Row
            item(key = "laundry_filter_chips", contentType = "filterChips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        items = LaundryFilterTab.entries,
                        key = { it.name },
                        contentType = { "laundryChip" }
                    ) { tab ->
                        val isSelected = selectedFilter == tab
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) ObsidianDark else fitColors.softIceTint,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Tab) { selectedFilter = tab }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Text(
                                    text = tab.label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) PureWhite else ObsidianDark
                                )
                                if (tab == LaundryFilterTab.ALL) {
                                    Box(
                                        modifier = Modifier
                                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) VoltLime else ObsidianDark.copy(alpha = 0.12f))
                                            .padding(horizontal = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dirtyItems.size.toString(),
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            color = ObsidianDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section header: Daftar Antrean Cuci
            item(key = "queue_section_header", contentType = "sectionHeader") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Antrean Cuci",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredDirty.size} Pakaian",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = fitColors.mutedText
                    )
                }
            }

            // Laundry Queue Items or Empty State
            if (filteredDirty.isEmpty()) {
                item(key = "empty_laundry_queue", contentType = "emptyQueue") {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, fitColors.subtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = if (dirtyItems.isEmpty()) {
                                    "Keranjang cucian kosong!"
                                } else {
                                    "Tidak ada pakaian di filter ini"
                                },
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (dirtyItems.isEmpty()) {
                                    "Seluruh koleksi lemarimu bersih dan siap dipadukan hari ini."
                                } else {
                                    "Pilih tab Semua Perlu Dicuci untuk melihat seluruh antrean."
                                },
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = fitColors.mutedText
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredDirty,
                    key = { "dirty_${it.id}" },
                    contentType = { "laundryQueueCard" }
                ) { item ->
                    val isConfirming = confirmingIds.contains(item.id)
                    val isVisible = !collapsingIds.contains(item.id)

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(220)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(220))
                    ) {
                        LaundryQueueCard(
                            item = item,
                            isConfirmingDone = isConfirming,
                            onCompleteWash = { triggerAnimatedWash(item) }
                        )
                    }
                }
            }

            // Section header: Baru Saja Dicuci + 10 Bersih + Riwayat >
            item(key = "clean_section_header", contentType = "sectionHeader") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Baru Saja Dicuci",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = CircleShape,
                            color = fitColors.softIceTint
                        ) {
                            Text(
                                text = "${cleanItems.size} Bersih",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = ObsidianDark,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { showFullHistory = !showFullHistory }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (showFullHistory) "Ringkas" else "Riwayat",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ElectricCobalt
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = ElectricCobalt,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Compact clean item cards
            items(
                items = visibleCleanItems,
                key = { "clean_${it.id}" },
                contentType = { "cleanItemCard" }
            ) { item ->
                RecentlyWashedCompactCard(item = item)
            }
        }
    }
}

@Composable
private fun CycleRecommendationCard(dirtyItems: List<WardrobeItemEntity>) {
    val fitColors = FitCastTheme.colors
    val count = dirtyItems.size
    val waterLiters = if (count == 0) 0 else 10 + (count * 4)
    val spinMinutes = if (count == 0) 0 else 25 + (count * 5)

    val summaryText = remember(dirtyItems) {
        when (dirtyItems.size) {
            0 -> "Semua pakaian bersih. Tidak perlu menjalankan siklus mesin cuci hari ini."
            1 -> "${dirtyItems[0].name} telah mencapai batas frekuensi pakai dan siap dicuci."
            2 -> "${dirtyItems[0].name} & ${dirtyItems[1].name} telah mencapai batas frekuensi pakai."
            else -> "${dirtyItems[0].name}, ${dirtyItems[1].name} & ${dirtyItems.size - 2} lainnya siap dicuci."
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, fitColors.subtleBorder),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(fitColors.softIceTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = ElectricCobalt,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rekomendasi Siklus Hari Ini",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = ElectricCobalt,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = summaryText,
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = fitColors.mutedText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Pale blue auto-calculated metrics sub-box (#EEF2FF)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = fitColors.softIceTint,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Eco,
                            contentDescription = null,
                            tint = fitColors.mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Estimasi Air",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = fitColors.mutedText
                            )
                            Text(
                                text = if (count == 0) "0 Liter (Siaga)" else "$waterLiters Liter (Eco)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalLaundryService,
                            contentDescription = null,
                            tint = fitColors.mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Waktu Putar",
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = fitColors.mutedText
                            )
                            Text(
                                text = "$spinMinutes Menit",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaundryQueueCard(
    item: WardrobeItemEntity,
    isConfirmingDone: Boolean,
    onCompleteWash: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    val categoryDisplay = when (item.category) {
        WardrobeCategory.TOP -> "Atasan"
        WardrobeCategory.OUTER -> "Outerwear"
        WardrobeCategory.BOTTOM -> "Bawahan"
        WardrobeCategory.SHOES -> "Sepatu"
        else -> "Aksesoris"
    }
    val reasonText = when (item.category) {
        WardrobeCategory.OUTER -> "Batas pakai outerwear selesai"
        WardrobeCategory.TOP -> "Batas pakai tercapai (Berkeringat)"
        else -> "Batas pakai tercapai"
    }
    val careTip = item.washCareTip.ifBlank { "Air Dingin (30°C)" }
    val useTempIcon = careTip.contains("Air", ignoreCase = true) || careTip.contains("°C")
    val progressFraction = (item.wearCount.toFloat() / item.maxWearBeforeWash.coerceAtLeast(1)).coerceIn(0f, 1f)

    val buttonBg by animateColorAsState(
        targetValue = if (isConfirmingDone) SuccessGreen else VoltLime,
        animationSpec = tween(200),
        label = "laundryDoneBtnBg"
    )
    val buttonContentColor by animateColorAsState(
        targetValue = if (isConfirmingDone) PureWhite else ObsidianDark,
        animationSpec = tween(200),
        label = "laundryDoneBtnText"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isConfirmingDone) SuccessGreen.copy(alpha = 0.5f) else fitColors.subtleBorder
        ),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left photo box with red LAUNDRY badge
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(fitColors.garmentBackdrop)
                ) {
                    GarmentVisualImage(
                        item = item,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                    Surface(
                        shape = CircleShape,
                        color = LaundryRed,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "LAUNDRY",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.4.sp,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Right details, progress bar & reason
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = item.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$categoryDisplay • ${item.materialDetail.ifBlank { "Katun" }}",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = fitColors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Full red progress bar + 1x / 1x label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(fitColors.softDangerTint)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(if (isConfirmingDone) SuccessGreen else LaundryRed)
                            )
                        }
                        Text(
                            text = "${item.wearCount}x / ${item.maxWearBeforeWash}x",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (isConfirmingDone) SuccessGreen else LaundryRed
                        )
                    }

                    Text(
                        text = if (isConfirmingDone) "Menandai bersih..." else reasonText,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isConfirmingDone) SuccessGreen else LaundryRed
                    )
                }
            }

            // Bottom row: wash care tip + Selesai Cuci Volt Lime pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (useTempIcon) Icons.Outlined.Thermostat else Icons.Outlined.Checkroom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = careTip,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onCompleteWash,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBg,
                        contentColor = buttonContentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 9.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Icon(
                        imageVector = if (isConfirmingDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConfirmingDone) "Bersih!" else "Selesai Cuci",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentlyWashedCompactCard(item: WardrobeItemEntity) {
    val fitColors = FitCastTheme.colors
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, fitColors.subtleBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fitColors.garmentBackdrop)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                GarmentVisualImage(
                    item = item,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tersimpan di ${item.storageLocation.ifBlank { "Lemari Utama" }}",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = fitColors.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = fitColors.successGreen,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "Siap Pakai",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = fitColors.successGreen
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 960)
@Composable
private fun LaundryScreenPreview() {
    val dirty1 = WardrobeItemEntity(
        id = 1,
        name = "Kaos Hitam Basic",
        category = WardrobeCategory.TOP,
        warmthLevel = WarmthLevel.LIGHT,
        style = OutfitStyle.CASUAL,
        colorHex = "#1E293B",
        colorTone = ColorTone.NEUTRAL,
        wearCount = 1,
        maxWearBeforeWash = 1,
        isClean = false,
        materialDetail = "Katun Combed 30s",
        colorName = "Charcoal",
        washCareTip = "Air Dingin (30°C)",
        storageLocation = "Laci Atasan"
    )
    val dirty2 = WardrobeItemEntity(
        id = 2,
        name = "Hoodie Abu Tua",
        category = WardrobeCategory.OUTER,
        warmthLevel = WarmthLevel.HEAVY,
        style = OutfitStyle.CASUAL,
        colorHex = "#475569",
        colorTone = ColorTone.NEUTRAL,
        wearCount = 4,
        maxWearBeforeWash = 4,
        isClean = false,
        materialDetail = "Fleece Tebal 360gsm",
        colorName = "Slate",
        washCareTip = "Putaran Lembut",
        storageLocation = "Lemari Utama"
    )
    val clean1 = WardrobeItemEntity(
        id = 3,
        name = "Jaket Parasut Teal",
        category = WardrobeCategory.OUTER,
        warmthLevel = WarmthLevel.HEAVY,
        style = OutfitStyle.SPORT,
        colorHex = "#0F766E",
        colorTone = ColorTone.COOL,
        wearCount = 1,
        maxWearBeforeWash = 5,
        isClean = true,
        storageLocation = "Lemari Utama"
    )
    val clean2 = WardrobeItemEntity(
        id = 4,
        name = "Kaos Katun Putih",
        category = WardrobeCategory.TOP,
        warmthLevel = WarmthLevel.LIGHT,
        style = OutfitStyle.CASUAL,
        colorHex = "#F8FAFC",
        colorTone = ColorTone.NEUTRAL,
        wearCount = 0,
        maxWearBeforeWash = 1,
        isClean = true,
        storageLocation = "Laci Atasan"
    )

    FitCastTheme(darkTheme = false) {
        LaundryScreenContent(
            allItems = listOf(dirty1, dirty2, clean1, clean2),
            dirtyItems = listOf(dirty1, dirty2),
            recentlyWashedIds = emptyList(),
            weather = null,
            snackbarHostState = remember { SnackbarHostState() },
            onLocationClick = {},
            onNotificationClick = {},
            onProfileClick = {},
            onMarkItemWashed = {},
            onMarkAllWashed = {}
        )
    }
}
