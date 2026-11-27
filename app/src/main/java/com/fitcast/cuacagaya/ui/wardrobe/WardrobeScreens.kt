package com.fitcast.cuacagaya.ui.wardrobe

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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
import com.fitcast.cuacagaya.ui.components.FitCastTopBar
import com.fitcast.cuacagaya.ui.components.GarmentVisualImage
import com.fitcast.cuacagaya.ui.components.parseGarmentColor
import com.fitcast.cuacagaya.ui.home.CityPickerDialog
import com.fitcast.cuacagaya.ui.home.NotificationBriefingDialog
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.LaundryRed
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.VoltLime
import kotlinx.coroutines.launch

private val AiBannerDark = Color(0xFF1E262B)

private enum class WardrobeSortOption(val label: String) {
    RECENT("Terbaru"),
    LAUNDRY_FIRST("Status Laundry"),
    MOST_WORN("Sering Dipakai"),
    NAME_ASC("Nama A-Z")
}

private enum class CleanFilterOption(val label: String) {
    ALL("Semua Status"),
    CLEAN_ONLY("Hanya Bersih"),
    LAUNDRY_ONLY("Perlu Laundry")
}

@Composable
fun WardrobeScreen(
    viewModel: WardrobeViewModel,
    onAddClick: () -> Unit,
    weather: WeatherCacheEntity? = null,
    isLoadingWeather: Boolean = false,
    onAboutClick: () -> Unit = {},
    onRefreshGps: () -> Unit = {},
    onSelectCity: (String) -> Unit = {}
) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val draftItems by viewModel.draftItems.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var showCityDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showDraftsDialog by remember { mutableStateOf(false) }
    var locationDenied by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<WardrobeItemEntity?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            locationDenied = false
            onRefreshGps()
        } else {
            locationDenied = true
            showCityDialog = true
        }
    }

    WardrobeScreenContent(
        items = items,
        draftCount = draftItems.size,
        recentlyAdded = recentlyAdded,
        weather = weather,
        isLoadingWeather = isLoadingWeather,
        snackbarHostState = snackbarHostState,
        onAddClick = onAddClick,
        onOpenDrafts = { showDraftsDialog = true },
        onDismissRecentlyAdded = viewModel::clearRecentlyAdded,
        onLocationClick = { showCityDialog = true },
        onNotificationClick = { showNotificationDialog = true },
        onProfileClick = onAboutClick,
        onToggleLaundry = { item ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.toggleLaundryStatus(item)
            scope.launch {
                val msg = if (item.isClean) {
                    "${item.name} dipindahkan ke Laundry"
                } else {
                    "${item.name} ditandai Bersih"
                }
                snackbarHostState.showSnackbar(msg)
            }
        },
        onEditItem = { editingItem = it },
        onDeleteItem = { item ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.deleteItem(item)
            scope.launch { snackbarHostState.showSnackbar("${item.name} dihapus dari lemari") }
        },
        onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
    )

    if (showCityDialog) {
        CityPickerDialog(
            currentCity = weather?.cityName ?: "Bandung",
            showSettingsShortcut = locationDenied,
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
            laundryCount = items.count { !it.isClean },
            onDismiss = { showNotificationDialog = false }
        )
    }

    if (showDraftsDialog) {
        DraftsManagerDialog(
            drafts = draftItems,
            onPublish = { draft ->
                viewModel.publishDraft(draft)
                scope.launch { snackbarHostState.showSnackbar("${draft.name} dipublikasikan ke Lemari") }
                if (draftItems.size <= 1) showDraftsDialog = false
            },
            onDelete = { draft ->
                viewModel.deleteItem(draft)
                if (draftItems.size <= 1) showDraftsDialog = false
            },
            onDismiss = { showDraftsDialog = false }
        )
    }

    editingItem?.let { item ->
        EditWardrobeItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                viewModel.updateItem(updated)
                editingItem = null
                scope.launch { snackbarHostState.showSnackbar("${updated.name} diperbarui") }
            }
        )
    }
}

@Composable
private fun WardrobeScreenContent(
    items: List<WardrobeItemEntity>,
    draftCount: Int,
    recentlyAdded: RecentlyAddedHighlight?,
    weather: WeatherCacheEntity?,
    isLoadingWeather: Boolean,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onOpenDrafts: () -> Unit,
    onDismissRecentlyAdded: () -> Unit,
    onLocationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onToggleLaundry: (WardrobeItemEntity) -> Unit,
    onEditItem: (WardrobeItemEntity) -> Unit,
    onDeleteItem: (WardrobeItemEntity) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val fitColors = FitCastTheme.colors
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var sortOption by remember { mutableStateOf(WardrobeSortOption.RECENT) }
    var cleanFilter by remember { mutableStateOf(CleanFilterOption.ALL) }

    // Auto-select newly saved item's category when returning from AddItemScreen
    LaunchedEffect(recentlyAdded?.itemId) {
        if (recentlyAdded != null) {
            selectedCategory = recentlyAdded.category
            searchQuery = ""
            cleanFilter = CleanFilterOption.ALL
        }
    }

    val filtered = remember(items, searchQuery, selectedCategory, sortOption, cleanFilter, recentlyAdded) {
        items
            .asSequence()
            .filter { item ->
                selectedCategory == "ALL" || item.category == selectedCategory
            }
            .filter { item ->
                when (cleanFilter) {
                    CleanFilterOption.ALL -> true
                    CleanFilterOption.CLEAN_ONLY -> item.isClean
                    CleanFilterOption.LAUNDRY_ONLY -> !item.isClean
                }
            }
            .filter { item ->
                if (searchQuery.isBlank()) true
                else item.name.contains(searchQuery, ignoreCase = true) ||
                    item.colorName.contains(searchQuery, ignoreCase = true) ||
                    item.materialDetail.contains(searchQuery, ignoreCase = true) ||
                    item.style.contains(searchQuery, ignoreCase = true)
            }
            .let { seq ->
                val sorted = when (sortOption) {
                    WardrobeSortOption.RECENT -> seq.toList()
                    WardrobeSortOption.LAUNDRY_FIRST -> seq.sortedBy { it.isClean }.toList()
                    WardrobeSortOption.MOST_WORN -> seq.sortedByDescending { it.wearCount }.toList()
                    WardrobeSortOption.NAME_ASC -> seq.sortedBy { it.name.lowercase() }.toList()
                }
                if (recentlyAdded != null) {
                    sorted.sortedByDescending { it.id == recentlyAdded.itemId }
                } else {
                    sorted
                }
            }
    }

    val categoryOptions = remember {
        listOf(
            "ALL" to "Semua",
            WardrobeCategory.TOP to "Atasan",
            WardrobeCategory.OUTER to "Outer",
            WardrobeCategory.BOTTOM to "Bawahan",
            WardrobeCategory.SHOES to "Sepatu",
            WardrobeCategory.ACCESSORY to "Aksesoris"
        )
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
                isLoadingWeather = isLoadingWeather,
                hasUnreadNotification = items.any { !it.isClean },
                notificationBadgeColor = LaundryRed,
                weatherIconTint = PureWhite.copy(alpha = 0.85f)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 172.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: LEMARI + • N ITEM + Sort button
                item(span = { GridItemSpan(maxLineSpan) }, key = "header_title", contentType = "header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "LEMARI",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Surface(
                                shape = CircleShape,
                                color = fitColors.softIceTint
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ElectricCobalt)
                                    )
                                    Text(
                                        text = "${items.size} ITEM",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.4.sp,
                                        color = ElectricCobalt
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(fitColors.softIceTint)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Urutkan pakaian"
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val entries = WardrobeSortOption.entries
                                    val next = entries[(sortOption.ordinal + 1) % entries.size]
                                    sortOption = next
                                    onShowMessage("Urutan: ${next.label}")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = "Urutkan",
                                tint = fitColors.onSoftIceTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Auto-filter banner when a new garment was just added
                if (recentlyAdded != null) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "auto_added_banner", contentType = "autoBanner") {
                        val catLabel = when (recentlyAdded.category) {
                            WardrobeCategory.TOP -> "Atasan"
                            WardrobeCategory.OUTER -> "Outer"
                            WardrobeCategory.BOTTOM -> "Bawahan"
                            WardrobeCategory.SHOES -> "Sepatu"
                            else -> "Aksesoris"
                        }
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically() + fadeIn(tween(240)),
                            exit = fadeOut()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ObsidianDark,
                                border = BorderStroke(1.dp, VoltLime.copy(alpha = 0.35f)),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .clickable { onDismissRecentlyAdded() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(VoltLime)
                                        )
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(color = VoltLime, fontWeight = FontWeight.Bold)) {
                                                    append("Pakaian baru ditambahkan")
                                                }
                                                withStyle(SpanStyle(color = PureWhite.copy(alpha = 0.88f))) {
                                                    append(" ke koleksi '$catLabel'")
                                                }
                                            },
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(shape = CircleShape, color = VoltLime) {
                                        Text(
                                            text = "AUTO",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = ObsidianDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search pill bar
                item(span = { GridItemSpan(maxLineSpan) }, key = "search_bar", contentType = "search") {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, fitColors.subtleBorder),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = fitColors.mutedText,
                                modifier = Modifier.size(20.dp)
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari baju, jaket, atau sepatu...",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 14.sp,
                                        color = fitColors.mutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(ElectricCobalt),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Hapus pencarian",
                                        tint = fitColors.mutedText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (cleanFilter == CleanFilterOption.ALL) fitColors.softIceTint
                                        else ObsidianDark
                                    )
                                    .clickable(role = Role.Button) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val entries = CleanFilterOption.entries
                                        val next = entries[(cleanFilter.ordinal + 1) % entries.size]
                                        cleanFilter = next
                                        onShowMessage("Filter: ${next.label}")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = "Filter status",
                                    tint = if (cleanFilter == CleanFilterOption.ALL) fitColors.onSoftIceTint else VoltLime,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Category filter chips (with BARU badge on newly added category)
                item(span = { GridItemSpan(maxLineSpan) }, key = "category_chips", contentType = "chips") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            items = categoryOptions,
                            key = { it.first },
                            contentType = { "categoryChip" }
                        ) { (key, label) ->
                            val isSelected = selectedCategory == key
                            val isNewlyAddedCategory = recentlyAdded?.category == key
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) ObsidianDark else MaterialTheme.colorScheme.surface,
                                border = when {
                                    isSelected && isNewlyAddedCategory -> BorderStroke(1.5.dp, VoltLime.copy(alpha = 0.65f))
                                    isSelected -> null
                                    else -> BorderStroke(1.dp, fitColors.subtleBorder)
                                },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .clip(CircleShape)
                                    .clickable(role = Role.Tab) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedCategory = key
                                        if (key != recentlyAdded?.category) {
                                            onDismissRecentlyAdded()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(VoltLime)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isNewlyAddedCategory) {
                                        Surface(shape = CircleShape, color = VoltLime) {
                                            Text(
                                                text = "BARU",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp,
                                                color = ObsidianDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dark ML Kit Auto-Cut Promo Banner
                item(span = { GridItemSpan(maxLineSpan) }, key = "ai_banner", contentType = "banner") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AiBannerDark,
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoFixHigh,
                                    contentDescription = null,
                                    tint = VoltLime,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "ML Kit Auto-Cut",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = PureWhite,
                                        maxLines = 1
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = VoltLime
                                    ) {
                                        Text(
                                            text = "AI",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            color = ObsidianDark,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Hapus latar & deteksi warna otomatis",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFFB4C0C5)
                                )
                            }

                            Button(
                                onClick = onAddClick,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VoltLime,
                                    contentColor = ObsidianDark
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                            ) {
                                Text(
                                    text = "+ Tambah",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Saved Drafts quick-access bar when drafts exist
                if (draftCount > 0) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "drafts_bar", contentType = "draftsBar") {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = fitColors.softIceTint,
                            border = BorderStroke(1.dp, fitColors.subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(role = Role.Button, onClick = onOpenDrafts)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        tint = ElectricCobalt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "$draftCount Draft Pakaian Tersimpan",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = fitColors.onSoftIceTint
                                    )
                                }
                                Text(
                                    text = "Kelola",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ElectricCobalt
                                )
                            }
                        }
                    }
                }

                // Empty state or 2-column garment grid
                if (filtered.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "empty_wardrobe", contentType = "empty") {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, fitColors.subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Checkroom,
                                    contentDescription = null,
                                    tint = fitColors.mutedText,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Tidak ada pakaian yang cocok",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Coba ubah filter pencarian atau tambahkan koleksi baru ke lemari.",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = fitColors.mutedText
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = filtered,
                        key = { it.id },
                        contentType = { it.category }
                    ) { item ->
                        WardrobeItemCard(
                            item = item,
                            isNewlyAdded = recentlyAdded?.itemId == item.id,
                            onToggleLaundry = { onToggleLaundry(item) },
                            onEdit = { onEditItem(item) },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }

            // Circular Volt Lime FAB positioned cleanly above Floating Bottom Nav
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = VoltLime,
                contentColor = ObsidianDark,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 18.dp, bottom = 94.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Pakaian",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun WardrobeItemCard(
    item: WardrobeItemEntity,
    isNewlyAdded: Boolean = false,
    onToggleLaundry: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    val parsedColor = remember(item.colorHex) { parseGarmentColor(item.colorHex) }
    var menuExpanded by remember { mutableStateOf(false) }

    val statusScale = remember { Animatable(1f) }
    LaunchedEffect(item.isClean) {
        statusScale.animateTo(0.96f, tween(90))
        statusScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }

    val badgeContainerColor by animateColorAsState(
        targetValue = if (item.isClean) ObsidianDark.copy(alpha = 0.88f) else LaundryRed,
        animationSpec = tween(240),
        label = "wardrobeBadgeBg"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isNewlyAdded && item.isClean -> VoltLime
            !item.isClean -> LaundryRed.copy(alpha = 0.45f)
            else -> fitColors.subtleBorder
        },
        animationSpec = tween(240),
        label = "wardrobeCardBorder"
    )

    val reachedLimit = !item.isClean || item.wearCount >= item.maxWearBeforeWash
    val categoryDisplay = when (item.category) {
        WardrobeCategory.TOP -> "ATASAN"
        WardrobeCategory.BOTTOM -> "BAWAHAN"
        WardrobeCategory.OUTER -> "OUTER"
        WardrobeCategory.SHOES -> "SEPATU"
        else -> "AKSESORIS"
    }
    val styleDisplay = item.style.lowercase().replaceFirstChar { it.uppercase() }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isNewlyAdded) 2.dp else 1.dp, cardBorderColor),
        shadowElevation = if (isNewlyAdded) 5.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = statusScale.value
                scaleY = statusScale.value
            }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(fitColors.garmentBackdrop)
            ) {
                GarmentVisualImage(
                    item = item,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )

                // Top-left status pill (BARU • BERSIH / BERSIH / LAUNDRY)
                Surface(
                    shape = CircleShape,
                    color = badgeContainerColor,
                    border = if (isNewlyAdded && item.isClean) BorderStroke(1.dp, VoltLime.copy(alpha = 0.65f)) else null,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (item.isClean) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(VoltLime)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.LocalLaundryService,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Text(
                            text = when {
                                !item.isClean -> "LAUNDRY"
                                isNewlyAdded -> "BARU • BERSIH"
                                else -> "BERSIH"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.4.sp,
                            color = if (item.isClean) VoltLime else PureWhite
                        )
                    }
                }

                // Top-right circular overflow menu
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PureWhite.copy(alpha = 0.92f))
                            .clickable(role = Role.Button, onClickLabel = "Menu opsi ${item.name}") {
                                menuExpanded = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "Menu lainnya",
                            tint = ObsidianDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (item.isClean) "Tandai Kotor / Laundry" else "Tandai Bersih")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (item.isClean) Icons.Outlined.LocalLaundryService else Icons.Outlined.CheckCircle,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleLaundry()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = LaundryRed) },
                            leadingIcon = {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = LaundryRed)
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryDisplay,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = fitColors.mutedText
                )
                Text(
                    text = "Pakai ${item.wearCount}/${item.maxWearBeforeWash}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = if (reachedLimit) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (reachedLimit) LaundryRed else fitColors.mutedText
                )
            }

            Text(
                text = item.name,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                            .border(1.dp, fitColors.subtleBorder, CircleShape)
                    )
                    Text(
                        text = item.colorName.ifBlank { "Netral" },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = fitColors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = fitColors.softIceTint
                ) {
                    Text(
                        text = styleDisplay,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = ElectricCobalt,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditWardrobeItemDialog(
    item: WardrobeItemEntity,
    onDismiss: () -> Unit,
    onSave: (WardrobeItemEntity) -> Unit
) {
    var name by remember(item) { mutableStateOf(item.name) }
    var storage by remember(item) { mutableStateOf(item.storageLocation) }
    var maxWear by remember(item) { mutableIntStateOf(item.maxWearBeforeWash) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Pakaian",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pakaian") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = storage,
                    onValueChange = { storage = it },
                    label = { Text("Lokasi Simpan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Batas Pakai", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold)
                    WearLimitStepper(
                        value = maxWear,
                        onDecrement = { if (maxWear > 1) maxWear-- },
                        onIncrement = { if (maxWear < 10) maxWear++ }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            item.copy(
                                name = name.trim(),
                                storageLocation = storage.trim().ifBlank { item.storageLocation },
                                maxWearBeforeWash = maxWear
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VoltLime, contentColor = ObsidianDark)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun DraftsManagerDialog(
    drafts: List<WardrobeItemEntity>,
    onPublish: (WardrobeItemEntity) -> Unit,
    onDelete: (WardrobeItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Draft Pakaian (${drafts.size})",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (drafts.isEmpty()) {
                Text("Tidak ada draft tersimpan.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    drafts.forEach { draft ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FitCastTheme.colors.softIceTint,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = draft.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = FitCastTheme.colors.onSoftIceTint,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${draft.category} • ${draft.colorName.ifBlank { "Netral" }}",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = FitCastTheme.colors.mutedText
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = { onPublish(draft) }) {
                                        Text("Simpan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { onDelete(draft) }, modifier = Modifier.size(32.dp)) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Hapus draft",
                                            tint = LaundryRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun WardrobeScreenAutoFilterPreview() {
    val newlyAdded = WardrobeItemEntity(
        id = 99,
        name = "Utility Overshirt Olive",
        category = WardrobeCategory.OUTER,
        warmthLevel = WarmthLevel.MEDIUM,
        style = OutfitStyle.CASUAL,
        colorHex = "#3D5A40",
        colorTone = ColorTone.COOL,
        wearCount = 0,
        maxWearBeforeWash = 3,
        isClean = true,
        colorName = "Olive"
    )
    val existingOuter = WardrobeItemEntity(
        id = 1,
        name = "Jaket Parasut Teal",
        category = WardrobeCategory.OUTER,
        warmthLevel = WarmthLevel.HEAVY,
        style = OutfitStyle.SPORT,
        colorHex = "#0F766E",
        colorTone = ColorTone.COOL,
        wearCount = 1,
        maxWearBeforeWash = 5,
        isClean = true,
        colorName = "Teal"
    )

    FitCastTheme(darkTheme = false) {
        WardrobeScreenContent(
            items = listOf(newlyAdded, existingOuter),
            draftCount = 1,
            recentlyAdded = RecentlyAddedHighlight(99, newlyAdded.name, WardrobeCategory.OUTER),
            weather = null,
            isLoadingWeather = false,
            snackbarHostState = remember { SnackbarHostState() },
            onAddClick = {},
            onOpenDrafts = {},
            onDismissRecentlyAdded = {},
            onLocationClick = {},
            onNotificationClick = {},
            onProfileClick = {},
            onToggleLaundry = {},
            onEditItem = {},
            onDeleteItem = {},
            onShowMessage = {}
        )
    }
}
