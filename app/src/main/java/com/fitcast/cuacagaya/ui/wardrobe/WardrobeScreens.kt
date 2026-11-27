package com.fitcast.cuacagaya.ui.wardrobe

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.OutfitStyle
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WarmthLevel
import com.fitcast.cuacagaya.data.local.entity.WeatherCacheEntity
import com.fitcast.cuacagaya.domain.processor.DetectedColorSwatch
import com.fitcast.cuacagaya.ui.components.FitCastTopBar
import com.fitcast.cuacagaya.ui.components.GarmentVisualImage
import com.fitcast.cuacagaya.ui.components.VectorGarmentSilhouette
import com.fitcast.cuacagaya.ui.components.parseGarmentColor
import com.fitcast.cuacagaya.ui.home.CityPickerDialog
import com.fitcast.cuacagaya.ui.home.NotificationBriefingDialog
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.LaundryRed
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.SoftIceTint
import com.fitcast.cuacagaya.ui.theme.SuccessGreen
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
    onAboutClick: () -> Unit = {},
    onRefreshGps: () -> Unit = {},
    onSelectCity: (String) -> Unit = {}
) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCityDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<WardrobeItemEntity?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) onRefreshGps() else showCityDialog = true
    }

    WardrobeScreenContent(
        items = items,
        weather = weather,
        snackbarHostState = snackbarHostState,
        onAddClick = onAddClick,
        onLocationClick = { showCityDialog = true },
        onNotificationClick = { showNotificationDialog = true },
        onProfileClick = onAboutClick,
        onToggleLaundry = { item ->
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
            viewModel.deleteItem(item)
            scope.launch { snackbarHostState.showSnackbar("${item.name} dihapus dari lemari") }
        },
        onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
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
            laundryCount = items.count { !it.isClean },
            onDismiss = { showNotificationDialog = false }
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
    weather: WeatherCacheEntity?,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onLocationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onToggleLaundry: (WardrobeItemEntity) -> Unit,
    onEditItem: (WardrobeItemEntity) -> Unit,
    onDeleteItem: (WardrobeItemEntity) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val fitColors = FitCastTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var sortOption by remember { mutableStateOf(WardrobeSortOption.RECENT) }
    var cleanFilter by remember { mutableStateOf(CleanFilterOption.ALL) }

    val filtered = remember(items, searchQuery, selectedCategory, sortOption, cleanFilter) {
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
                when (sortOption) {
                    WardrobeSortOption.RECENT -> seq.toList()
                    WardrobeSortOption.LAUNDRY_FIRST -> seq.sortedBy { it.isClean }.toList()
                    WardrobeSortOption.MOST_WORN -> seq.sortedByDescending { it.wearCount }.toList()
                    WardrobeSortOption.NAME_ASC -> seq.sortedBy { it.name.lowercase() }.toList()
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
                // Header: LEMARI + • 12 ITEM + Sort button
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
                                tint = ObsidianDark,
                                modifier = Modifier.size(20.dp)
                            )
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
                                    tint = if (cleanFilter == CleanFilterOption.ALL) ObsidianDark else VoltLime,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Category filter chips
                item(span = { GridItemSpan(maxLineSpan) }, key = "category_chips", contentType = "chips") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            items = categoryOptions,
                            key = { it.first },
                            contentType = { "categoryChip" }
                        ) { (key, label) ->
                            val isSelected = selectedCategory == key
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) ObsidianDark else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else BorderStroke(1.dp, fitColors.subtleBorder),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .clip(CircleShape)
                                    .clickable(role = Role.Tab) { selectedCategory = key }
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
    onToggleLaundry: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    val parsedColor = remember(item.colorHex) { parseGarmentColor(item.colorHex) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Micro-animation when clean/laundry state transitions
    val statusScale = remember { Animatable(1f) }
    LaunchedEffect(item.isClean) {
        statusScale.animateTo(0.96f, tween(90))
        statusScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }

    val badgeContainerColor by animateColorAsState(
        targetValue = if (item.isClean) ObsidianDark.copy(alpha = 0.86f) else LaundryRed,
        animationSpec = tween(240),
        label = "wardrobeBadgeBg"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (item.isClean) fitColors.subtleBorder else LaundryRed.copy(alpha = 0.35f),
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
        border = BorderStroke(1.dp, cardBorderColor),
        shadowElevation = 2.dp,
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

                // Top-left status pill (BERSIH / LAUNDRY)
                Surface(
                    shape = CircleShape,
                    color = badgeContainerColor,
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
                            text = if (item.isClean) "BERSIH" else "LAUNDRY",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
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

            // Category & wear ratio row
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

            // Bottom color swatch + style pill
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemScreen(
    viewModel: WardrobeViewModel,
    onBack: () -> Unit
) {
    val swatches by viewModel.detectedSwatches.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessingImage.collectAsStateWithLifecycle()

    AddItemScreenContent(
        swatches = swatches,
        isProcessing = isProcessing,
        onPhotoSelected = { uri -> viewModel.extractPaletteFromUri(uri) },
        onResetSwatches = { viewModel.resetDetectedSwatches() },
        onSaveItem = { request ->
            viewModel.addClothingWithPhoto(
                name = request.name,
                category = request.category,
                warmthLevel = request.warmthLevel,
                style = request.primaryStyle,
                maxWear = request.maxWear,
                photoUri = request.selectedUri,
                removeBg = request.removeBg,
                colorHex = request.colorHex,
                colorName = request.colorName,
                colorTone = request.colorTone,
                materialDetail = request.materialDetail,
                suitableWeathers = request.suitableWeathers,
                styleTags = request.styleTags,
                isDraft = request.isDraft,
                onFinished = onBack
            )
        },
        onBack = onBack
    )
}

private data class SaveClothingRequest(
    val name: String,
    val category: String,
    val warmthLevel: String,
    val primaryStyle: String,
    val maxWear: Int,
    val selectedUri: Uri?,
    val removeBg: Boolean,
    val colorHex: String,
    val colorName: String,
    val colorTone: String,
    val materialDetail: String,
    val suitableWeathers: String,
    val styleTags: String,
    val isDraft: Boolean
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddItemScreenContent(
    swatches: List<DetectedColorSwatch>,
    isProcessing: Boolean,
    onPhotoSelected: (Uri?) -> Unit,
    onResetSwatches: () -> Unit,
    onSaveItem: (SaveClothingRequest) -> Unit,
    onBack: () -> Unit
) {
    val fitColors = FitCastTheme.colors

    var name by remember { mutableStateOf("Utility Overshirt Olive") }
    var category by remember { mutableStateOf(WardrobeCategory.OUTER) }
    var warmth by remember { mutableStateOf(WarmthLevel.MEDIUM) }
    var maxWear by remember { mutableIntStateOf(3) }
    var removeBg by remember { mutableStateOf(true) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSwatchIndex by remember { mutableIntStateOf(0) }
    var selectedWeathers by remember { mutableStateOf(setOf("Hujan Ringan", "Berangin")) }
    val availableStyleTags = remember {
        mutableStateListOf("Casual", "Streetwear", "Formal", "Workwear", "Outdoor")
    }
    var selectedStyleTags by remember { mutableStateOf(setOf("Casual", "Streetwear")) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }

    val activeSwatch = swatches.getOrElse(selectedSwatchIndex) {
        swatches.firstOrNull() ?: DetectedColorSwatch("Olive", "#3D5A40", ColorTone.COOL)
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedSwatchIndex = 0
            onPhotoSelected(uri)
        }
    }

    fun resetForm() {
        name = ""
        category = WardrobeCategory.OUTER
        warmth = WarmthLevel.MEDIUM
        maxWear = 3
        removeBg = true
        selectedUri = null
        selectedSwatchIndex = 0
        selectedWeathers = setOf("Hujan Ringan", "Berangin")
        selectedStyleTags = setOf("Casual")
        onResetSwatches()
    }

    val categoryDisplayName = when (category) {
        WardrobeCategory.TOP -> "Atasan"
        WardrobeCategory.OUTER -> "Outerwear"
        WardrobeCategory.BOTTOM -> "Bawahan"
        WardrobeCategory.SHOES -> "Sepatu"
        else -> "Aksesoris"
    }

    val materialSuggestion = when (category) {
        WardrobeCategory.OUTER -> "Katun Twill"
        WardrobeCategory.TOP -> "Katun Combed 24s"
        WardrobeCategory.BOTTOM -> "Twill Stretch"
        WardrobeCategory.SHOES -> "Kanvas & Sol Karet"
        else -> "Nilon Ringan"
    }

    val warmthTempRange = when (warmth) {
        WarmthLevel.LIGHT -> "27°C - 35°C"
        WarmthLevel.MEDIUM -> "20°C - 27°C"
        else -> "14°C - 20°C"
    }

    fun buildSaveRequest(isDraft: Boolean): SaveClothingRequest {
        val primaryStyle = when {
            selectedStyleTags.contains("Formal") -> OutfitStyle.FORMAL
            selectedStyleTags.contains("Outdoor") -> OutfitStyle.SPORT
            else -> OutfitStyle.CASUAL
        }
        return SaveClothingRequest(
            name = name.ifBlank { "$categoryDisplayName ${activeSwatch.name}" },
            category = category,
            warmthLevel = warmth,
            primaryStyle = primaryStyle,
            maxWear = maxWear,
            selectedUri = selectedUri,
            removeBg = removeBg,
            colorHex = activeSwatch.hex,
            colorName = activeSwatch.name,
            colorTone = activeSwatch.tone,
            materialDetail = materialSuggestion,
            suitableWeathers = selectedWeathers.joinToString(","),
            styleTags = selectedStyleTags.joinToString(","),
            isDraft = isDraft
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Tambah Pakaian",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { resetForm() }) {
                        Text(
                            text = "Reset",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = fitColors.mutedText
                        )
                    }
                }

                // Vision Engine pill badge
                Surface(
                    shape = CircleShape,
                    color = fitColors.softIceTint,
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(VoltLime)
                        )
                        Text(
                            text = "VISION ENGINE V2.4",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp,
                            color = ObsidianDark
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, fitColors.subtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBack,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = fitColors.softIceTint,
                            contentColor = ObsidianDark
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .width(104.dp)
                    ) {
                        Text(
                            text = "Batal",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { onSaveItem(buildSaveRequest(isDraft = false)) },
                        enabled = name.isNotBlank() && !isProcessing,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltLime,
                            contentColor = ObsidianDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ObsidianDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Memproses ML Kit...",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Simpan ke Lemari",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Foto & ML Kit Auto-Cut
            AddItemCardContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(232.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(fitColors.softIceTint.copy(alpha = 0.72f))
                    ) {
                        if (selectedUri != null) {
                            AsyncImage(
                                model = selectedUri,
                                contentDescription = "Preview pakaian",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        } else {
                            VectorGarmentSilhouette(
                                category = category,
                                baseColor = parseGarmentColor(activeSwatch.hex, Color(0xFF3D5A40)),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 42.dp, vertical = 22.dp)
                            )
                        }

                        // Auto-Cut status pill
                        Surface(
                            shape = CircleShape,
                            color = ObsidianDark.copy(alpha = 0.90f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoFixHigh,
                                    contentDescription = null,
                                    tint = VoltLime,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (removeBg) "Auto-Cut Aktif" else "Auto-Cut Nonaktif",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PureWhite
                                )
                                if (removeBg) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = VoltLime,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Detected metadata chips at bottom-left
                        FlowRow(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PureWhite.copy(alpha = 0.94f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(parseGarmentColor(activeSwatch.hex))
                                    )
                                    Text(
                                        text = activeSwatch.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = ObsidianDark
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = SoftIceTint
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Checkroom,
                                        contentDescription = null,
                                        tint = ElectricCobalt,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = categoryDisplayName,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = ElectricCobalt
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = PureWhite.copy(alpha = 0.88f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Air,
                                        contentDescription = null,
                                        tint = ObsidianDark,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = materialSuggestion,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = ObsidianDark
                                    )
                                }
                            }
                        }
                    }

                    // 3 Action buttons: Ulang, Galeri, Auto-Cut AI toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = fitColors.softIceTint,
                            modifier = Modifier
                                .height(42.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button) {
                                    selectedUri = null
                                    onResetSwatches()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = null,
                                    tint = ObsidianDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Ulang",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ObsidianDark
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = fitColors.softIceTint,
                            modifier = Modifier
                                .height(42.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button) {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    tint = ObsidianDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Galeri",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ObsidianDark
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (removeBg) ObsidianDark else fitColors.softIceTint,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Switch) { removeBg = !removeBg }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (removeBg) VoltLime else fitColors.mutedText)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Cut AI",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (removeBg) PureWhite else ObsidianDark,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Warna Terdeteksi (Android Palette API)
            AddItemCardContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECFDF3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Warna Terdeteksi",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pilih warna primer pakaian",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = fitColors.mutedText
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = fitColors.softIceTint
                        ) {
                            Text(
                                text = "${swatches.size} Terdeteksi",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = ElectricCobalt,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(
                            items = swatches.indices.toList(),
                            key = { swatches[it].hex },
                            contentType = { "swatch" }
                        ) { index ->
                            val swatch = swatches[index]
                            val isSelected = index == selectedSwatchIndex
                            val swatchColor = parseGarmentColor(swatch.hex)

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else fitColors.softIceTint.copy(alpha = 0.6f),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) swatchColor else fitColors.subtleBorder
                                ),
                                modifier = Modifier
                                    .width(82.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable(role = Role.RadioButton) { selectedSwatchIndex = index }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(1.dp, fitColors.subtleBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Terpilih",
                                                tint = PureWhite,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = swatch.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = swatch.hex,
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        color = fitColors.mutedText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Nama Pakaian & Kategori
            AddItemCardContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nama Pakaian",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = CircleShape,
                            color = VoltLime,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(role = Role.Button) {
                                    val suggestion = when (category) {
                                        WardrobeCategory.OUTER -> "Utility Overshirt ${activeSwatch.name}"
                                        WardrobeCategory.TOP -> "Kaos Katun ${activeSwatch.name}"
                                        WardrobeCategory.BOTTOM -> "Chino Relaxed ${activeSwatch.name}"
                                        WardrobeCategory.SHOES -> "Sneakers Low ${activeSwatch.name}"
                                        else -> "Topi Baseball ${activeSwatch.name}"
                                    }
                                    name = suggestion
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = ObsidianDark,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Sugesti AI",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    color = ObsidianDark
                                )
                            }
                        }
                    }

                    // Rounded soft-tint input field
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = fitColors.softIceTint.copy(alpha = 0.72f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (name.isEmpty()) {
                                    Text(
                                        text = "Contoh: Utility Overshirt Olive",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 14.sp,
                                        color = fitColors.mutedText
                                    )
                                }
                                BasicTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(ElectricCobalt),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = fitColors.mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Kategori Pakaian",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val categoryChips = remember {
                        listOf(
                            WardrobeCategory.TOP to "Atasan",
                            WardrobeCategory.OUTER to "Outer",
                            WardrobeCategory.BOTTOM to "Bawahan",
                            WardrobeCategory.SHOES to "Sepatu",
                            WardrobeCategory.ACCESSORY to "Aksesoris"
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryChips.forEach { (catKey, label) ->
                            val isSelected = category == catKey
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) ObsidianDark else fitColors.softIceTint,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .clip(CircleShape)
                                    .clickable(role = Role.RadioButton) { category = catKey }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
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
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) PureWhite else ObsidianDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 4: Kesesuaian Cuaca + Level Kehangatan Material
            AddItemCardContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(fitColors.softIceTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Cloud,
                                contentDescription = null,
                                tint = ElectricCobalt,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Kesesuaian Cuaca",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Parameter rekomendasi harian otomatis",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = fitColors.mutedText
                            )
                        }
                    }

                    Text(
                        text = "Kondisi Cuaca Cocok",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = fitColors.mutedText
                    )

                    // 2x2 Weather Grid (single clean vector icon per option)
                    val weatherRows = remember {
                        listOf(
                            listOf(
                                Triple("Panas / Terik", Icons.Outlined.WbSunny, Color(0xFFD97706)),
                                Triple("Hujan Ringan", Icons.Outlined.Grain, ElectricCobalt)
                            ),
                            listOf(
                                Triple("Hujan Deras", Icons.Outlined.Thunderstorm, ElectricCobalt),
                                Triple("Berangin", Icons.Outlined.Air, Color(0xFF0284C7))
                            )
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        weatherRows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { (label, icon, tint) ->
                                    val checked = selectedWeathers.contains(label)
                                    WeatherConditionSelectableTile(
                                        label = label,
                                        icon = icon,
                                        iconTint = tint,
                                        selected = checked,
                                        onClick = {
                                            selectedWeathers = if (checked) {
                                                selectedWeathers - label
                                            } else {
                                                selectedWeathers + label
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Warmth level segmented control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level Kehangatan Material",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = fitColors.mutedText
                        )
                        Text(
                            text = warmthTempRange,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = ElectricCobalt
                        )
                    }

                    val warmthOptions = remember {
                        listOf(
                            WarmthLevel.LIGHT to "Ringan",
                            WarmthLevel.MEDIUM to "Sedang",
                            WarmthLevel.HEAVY to "Tebal / Polar"
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = fitColors.softIceTint,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            warmthOptions.forEach { (key, label) ->
                                val isSelected = warmth == key
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(CircleShape)
                                        .clickable(role = Role.RadioButton) { warmth = key }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(ElectricCobalt)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                        }
                                        Text(
                                            text = label,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else fitColors.mutedText,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 5: Batas Pakai Stepper (- 3x +)
            AddItemCardContainer {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalLaundryService,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Batas Pakai",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Sebelum status otomatis masuk keranjang cucian",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = fitColors.mutedText
                        )
                    }

                    WearLimitStepper(
                        value = maxWear,
                        onDecrement = { if (maxWear > 1) maxWear-- },
                        onIncrement = { if (maxWear < 10) maxWear++ }
                    )
                }
            }

            // Card 6: Gaya & Acara (Multi-select Tags)
            AddItemCardContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Gaya & Acara (Tags)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableStyleTags.forEach { tag ->
                            val isSelected = selectedStyleTags.contains(tag)
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) ObsidianDark else fitColors.softIceTint,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .clip(CircleShape)
                                    .clickable(role = Role.Checkbox) {
                                        selectedStyleTags = if (isSelected) {
                                            selectedStyleTags - tag
                                        } else {
                                            selectedStyleTags + tag
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = VoltLime,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = tag,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) VoltLime else ObsidianDark
                                    )
                                }
                            }
                        }

                        // Add custom tag (+) button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(fitColors.softIceTint)
                                .clickable(role = Role.Button, onClickLabel = "Tambah tag gaya baru") {
                                    customTagInput = ""
                                    showAddTagDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Tambah tag",
                                tint = ObsidianDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Save as Draft text link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isProcessing, role = Role.Button) {
                            onSaveItem(buildSaveRequest(isDraft = true))
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = fitColors.mutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Simpan Sementara sebagai Draft",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = fitColors.mutedText
                    )
                }
            }
        }
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("Tambah Tag Gaya", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it },
                    placeholder = { Text("Contoh: Ngampus, Olahraga") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = customTagInput.trim()
                        if (cleaned.isNotBlank()) {
                            if (!availableStyleTags.contains(cleaned)) {
                                availableStyleTags.add(cleaned)
                            }
                            selectedStyleTags = selectedStyleTags + cleaned
                        }
                        showAddTagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoltLime, contentColor = ObsidianDark)
                ) {
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun AddItemCardContainer(
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, FitCastTheme.colors.subtleBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun WeatherConditionSelectableTile(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fitColors = FitCastTheme.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) SoftIceTint else fitColors.softIceTint.copy(alpha = 0.45f),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) ElectricCobalt.copy(alpha = 0.55f) else fitColors.subtleBorder
        ),
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Checkbox, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = if (selected) ElectricCobalt else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(ElectricCobalt),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Dipilih",
                        tint = PureWhite,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, fitColors.mutedText.copy(alpha = 0.45f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun WearLimitStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = FitCastTheme.colors.softIceTint
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = value > 1, role = Role.Button, onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "Kurangi batas pakai",
                    tint = ObsidianDark,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "${value}x",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = ObsidianDark
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = value < 10, role = Role.Button, onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Tambah batas pakai",
                    tint = ObsidianDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun WardrobeScreenPreview() {
    val sampleList = listOf(
        WardrobeItemEntity(
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
        ),
        WardrobeItemEntity(
            id = 2,
            name = "Kaos Katun Putih",
            category = WardrobeCategory.TOP,
            warmthLevel = WarmthLevel.LIGHT,
            style = OutfitStyle.CASUAL,
            colorHex = "#F8FAFC",
            colorTone = ColorTone.NEUTRAL,
            wearCount = 0,
            maxWearBeforeWash = 1,
            isClean = true,
            colorName = "White"
        ),
        WardrobeItemEntity(
            id = 3,
            name = "Kaos Hitam Basic",
            category = WardrobeCategory.TOP,
            warmthLevel = WarmthLevel.LIGHT,
            style = OutfitStyle.CASUAL,
            colorHex = "#1E293B",
            colorTone = ColorTone.NEUTRAL,
            wearCount = 1,
            maxWearBeforeWash = 1,
            isClean = false,
            colorName = "Charcoal"
        ),
        WardrobeItemEntity(
            id = 4,
            name = "Celana Chino Cream",
            category = WardrobeCategory.BOTTOM,
            warmthLevel = WarmthLevel.MEDIUM,
            style = OutfitStyle.FORMAL,
            colorHex = "#C2B280",
            colorTone = ColorTone.WARM,
            wearCount = 1,
            maxWearBeforeWash = 3,
            isClean = true,
            colorName = "Cream"
        )
    )

    FitCastTheme(darkTheme = false) {
        WardrobeScreenContent(
            items = sampleList,
            weather = null,
            snackbarHostState = remember { SnackbarHostState() },
            onAddClick = {},
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

@Preview(showBackground = true, widthDp = 390, heightDp = 1240)
@Composable
private fun AddItemScreenPreview() {
    FitCastTheme(darkTheme = false) {
        AddItemScreenContent(
            swatches = WardrobeViewModel.DEFAULT_PALETTE_SWATCHES,
            isProcessing = false,
            onPhotoSelected = {},
            onResetSwatches = {},
            onSaveItem = {},
            onBack = {}
        )
    }
}
