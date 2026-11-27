package com.fitcast.cuacagaya.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fitcast.cuacagaya.data.local.entity.OutfitStyle
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAboutClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCityDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("FitCast", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showCityDialog = true }) {
                        Icon(Icons.Default.LocationCity, "Pilih Kota Manual")
                    }
                    IconButton(onClick = {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }) {
                        Icon(Icons.Default.MyLocation, "Perbarui GPS")
                    }
                    IconButton(onClick = onAboutClick) {
                        Icon(Icons.Default.Info, "Tentang")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            state.weather?.let { w ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(w.cityName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(w.weatherDescription, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (state.isLoadingWeather) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            } else {
                                Text("${w.temperature.toInt()}°C", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Terasa ${w.feelsLike.toInt()}°C • Lembap ${w.humidity}% • Peluang Hujan ${w.rainProbability}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                        state.recommendation?.let { rec ->
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                color = if (rec.bringUmbrella) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = rec.weatherAdvice,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.selectedStyle == null,
                        onClick = { viewModel.setStyleFilter(null) },
                        label = { Text("Semua Gaya") }
                    )
                }
                items(OutfitStyle.ALL) { style ->
                    FilterChip(
                        selected = state.selectedStyle == style,
                        onClick = { viewModel.setStyleFilter(style) },
                        label = { Text(style) }
                    )
                }
            }

            state.recommendation?.let { rec ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rekomendasi Hari Ini", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(rec.harmonyLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedButton(onClick = { viewModel.shuffleOutfit() }) {
                        Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Acak")
                    }
                }

                OutfitSlotRow("Atasan", WardrobeCategory.TOP, rec.top, state.lockedCategories.contains(WardrobeCategory.TOP)) {
                    viewModel.toggleLock(WardrobeCategory.TOP, rec.top)
                }
                if (rec.outer != null) {
                    OutfitSlotRow("Luaran / Jaket", WardrobeCategory.OUTER, rec.outer, state.lockedCategories.contains(WardrobeCategory.OUTER)) {
                        viewModel.toggleLock(WardrobeCategory.OUTER, rec.outer)
                    }
                }
                OutfitSlotRow("Bawahan", WardrobeCategory.BOTTOM, rec.bottom, state.lockedCategories.contains(WardrobeCategory.BOTTOM)) {
                    viewModel.toggleLock(WardrobeCategory.BOTTOM, rec.bottom)
                }
                OutfitSlotRow("Alas Kaki", WardrobeCategory.SHOES, rec.shoes, state.lockedCategories.contains(WardrobeCategory.SHOES)) {
                    viewModel.toggleLock(WardrobeCategory.SHOES, rec.shoes)
                }

                Button(
                    onClick = { viewModel.wearCurrentOutfit() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pakai Outfit Ini Hari Ini")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCityDialog) {
        val cities = listOf("Bandung", "Bogor", "Jakarta", "Depok", "Surabaya", "Yogyakarta")
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("Pilih Kota Manual") },
            text = {
                Column {
                    cities.forEach { city ->
                        TextButton(
                            onClick = {
                                viewModel.refreshWeatherByCity(city)
                                showCityDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(city) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCityDialog = false }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun OutfitSlotRow(
    title: String,
    category: String,
    item: WardrobeItemEntity?,
    isLocked: Boolean,
    onToggleLock: () -> Unit
) {
    val itemColor = remember(item?.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(item?.colorHex ?: "#64748B")) }
            .getOrDefault(Color.Gray)
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(itemColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                if (item != null && item.imagePath.isNotBlank() && File(item.imagePath).exists()) {
                    AsyncImage(model = File(item.imagePath), contentDescription = item.name, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Checkroom, null, tint = itemColor)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(item?.name ?: "Belum ada $category bersih", fontWeight = FontWeight.SemiBold)
                if (item != null) {
                    Text("${item.style} • ${item.warmthLevel}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onToggleLock) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Kunci Item",
                    tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
