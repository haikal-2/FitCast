package com.fitcast.cuacagaya.ui.wardrobe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fitcast.cuacagaya.data.local.entity.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    viewModel: WardrobeViewModel,
    onAddClick: () -> Unit
) {
    val items by viewModel.allItems.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filtered = remember(items, selectedCategory) {
        if (selectedCategory == "ALL") items else items.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lemari Digital (${items.size})") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Baju")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == "ALL",
                        onClick = { selectedCategory = "ALL" },
                        label = { Text("Semua") }
                    )
                }
                items(WardrobeCategory.ALL) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pakaian di kategori ini.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        WardrobeItemCard(item = item, onDelete = { viewModel.deleteItem(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun WardrobeItemCard(item: WardrobeItemEntity, onDelete: () -> Unit) {
    val parsedColor = remember(item.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(item.colorHex)) }.getOrDefault(Color.Gray)
    }

    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(parsedColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imagePath.isNotBlank() && File(item.imagePath).exists()) {
                    AsyncImage(
                        model = File(item.imagePath),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = parsedColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(parsedColor)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(item.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                text = "${item.warmthLevel} • ${if (item.isClean) "Bersih" else "Kotor"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.isClean) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pakai: ${item.wearCount}/${item.maxWearBeforeWash}", style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    viewModel: WardrobeViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(WardrobeCategory.TOP) }
    var warmth by remember { mutableStateOf(WarmthLevel.MEDIUM) }
    var style by remember { mutableStateOf(OutfitStyle.CASUAL) }
    var maxWear by remember { mutableIntStateOf(2) }
    var removeBg by remember { mutableStateOf(true) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val isProcessing by viewModel.isProcessingImage.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Pakaian") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (selectedUri == null) "Pilih Foto Baju (Auto-Color + Hapus BG)" else "Ganti Foto Baju")
            }

            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = removeBg, onCheckedChange = { removeBg = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Hapus Latar Belakang Otomatis (ML Kit)")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Pakaian (mis. Kaos Hitam Oversize)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Kategori", fontWeight = FontWeight.Medium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WardrobeCategory.ALL) { cat ->
                    FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                }
            }

            Text("Ketebalan Bahan", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WarmthLevel.ALL.forEach { w ->
                    FilterChip(selected = warmth == w, onClick = { warmth = w }, label = { Text(w) })
                }
            }

            Text("Gaya", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutfitStyle.ALL.forEach { s ->
                    FilterChip(selected = style == s, onClick = { style = s }, label = { Text(s) })
                }
            }

            Text("Batas Pakai Sebelum Dicuci: $maxWear kali")
            Slider(value = maxWear.toFloat(), onValueChange = { maxWear = it.toInt() }, valueRange = 1f..5f, steps = 3)

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addClothingWithPhoto(name, category, warmth, style, maxWear, selectedUri, removeBg, onBack)
                    }
                },
                enabled = name.isNotBlank() && !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Memproses ML Kit & Warna...")
                } else {
                    Text("Simpan ke Lemari")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryScreen(viewModel: WardrobeViewModel) {
    val dirtyItems by viewModel.dirtyItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Laundry (${dirtyItems.size})") },
                actions = {
                    if (dirtyItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.washAll() }) {
                            Text("Cuci Semua")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (dirtyItems.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Semua pakaianmu dalam kondisi bersih!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dirtyItems, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text("Sudah dipakai ${item.wearCount}x (${item.category})", style = MaterialTheme.typography.bodySmall)
                            }
                            FilledTonalButton(onClick = { viewModel.washItem(item.id) }) {
                                Text("Selesai Cuci")
                            }
                        }
                    }
                }
            }
        }
    }
}
