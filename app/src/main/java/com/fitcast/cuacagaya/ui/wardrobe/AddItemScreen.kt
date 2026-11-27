package com.fitcast.cuacagaya.ui.wardrobe

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.fitcast.cuacagaya.data.local.entity.OutfitStyle
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import com.fitcast.cuacagaya.data.local.entity.WarmthLevel
import com.fitcast.cuacagaya.domain.processor.DetectedColorSwatch
import com.fitcast.cuacagaya.ui.components.GarmentVisualImage
import com.fitcast.cuacagaya.ui.components.VectorGarmentSilhouette
import com.fitcast.cuacagaya.ui.components.parseGarmentColor
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.LaundryRed
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.SoftIceTint
import com.fitcast.cuacagaya.ui.theme.SuccessGreen
import com.fitcast.cuacagaya.ui.theme.VoltLime
import kotlinx.coroutines.delay
import java.io.File

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

@Composable
fun AddItemScreen(
    viewModel: WardrobeViewModel,
    onBack: () -> Unit,
    onNavigateToWardrobe: () -> Unit = onBack
) {
    val swatches by viewModel.detectedSwatches.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessingImage.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()

    var savedItemModal by remember { mutableStateOf<WardrobeItemEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.resetScanState()
    }

    LaunchedEffect(scanState.showExtractedToast) {
        if (scanState.showExtractedToast) {
            delay(3200L)
            viewModel.dismissScanToast()
        }
    }

    AddItemScreenContent(
        swatches = swatches,
        scanState = scanState,
        isProcessing = isProcessing,
        savedItemModal = savedItemModal,
        onPhotoChosen = { uri, removeBg -> viewModel.startPhotoScan(uri, removeBg) },
        onReplayScan = { removeBg -> viewModel.replayCurrentScan(removeBg) },
        onRemovePhoto = { viewModel.resetScanState() },
        onAddCustomSwatch = { viewModel.addCustomSwatch(it) },
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
                onSaved = { saved ->
                    if (request.isDraft) {
                        onBack()
                    } else {
                        savedItemModal = saved
                    }
                }
            )
        },
        onDismissSuccessModal = {
            savedItemModal = null
            onBack()
        },
        onViewInWardrobe = {
            savedItemModal = null
            onNavigateToWardrobe()
        },
        onAddAnotherItem = {
            savedItemModal = null
            viewModel.resetScanState()
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddItemScreenContent(
    swatches: List<DetectedColorSwatch>,
    scanState: AddItemScanUiState,
    isProcessing: Boolean,
    savedItemModal: WardrobeItemEntity?,
    onPhotoChosen: (Uri?, Boolean) -> Unit,
    onReplayScan: (Boolean) -> Unit,
    onRemovePhoto: () -> Unit,
    onAddCustomSwatch: (DetectedColorSwatch) -> Unit,
    onSaveItem: (SaveClothingRequest) -> Unit,
    onDismissSuccessModal: () -> Unit,
    onViewInWardrobe: () -> Unit,
    onAddAnotherItem: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val fitColors = FitCastTheme.colors

    var name by remember { mutableStateOf("Utility Overshirt Olive") }
    var category by remember { mutableStateOf(WardrobeCategory.OUTER) }
    var warmth by remember { mutableStateOf(WarmthLevel.MEDIUM) }
    var maxWear by remember { mutableIntStateOf(3) }
    var removeBg by remember { mutableStateOf(true) }
    var selectedSwatchIndex by remember { mutableIntStateOf(0) }
    var selectedWeathers by remember { mutableStateOf(setOf("Hujan Ringan", "Berangin")) }
    val availableStyleTags = remember {
        mutableStateListOf("Casual", "Streetwear", "Formal", "Workwear", "Outdoor")
    }
    var selectedStyleTags by remember { mutableStateOf(setOf("Casual", "Streetwear")) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var showFullscreenPreview by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Cascading form reveal state synchronized with ML Kit scan completion
    var revealStep by remember { mutableIntStateOf(4) }
    LaunchedEffect(scanState.phase) {
        when (scanState.phase) {
            MlScanPhase.SCANNING -> revealStep = 0
            MlScanPhase.SCANNED -> {
                revealStep = 0
                for (step in 1..4) {
                    delay(120L)
                    revealStep = step
                }
            }

            MlScanPhase.EMPTY -> revealStep = 4
        }
    }

    LaunchedEffect(swatches.size) {
        if (selectedSwatchIndex >= swatches.size) {
            selectedSwatchIndex = 0
        }
    }

    val activeSwatch = swatches.getOrElse(selectedSwatchIndex) {
        swatches.firstOrNull() ?: DetectedColorSwatch("Olive", "#3D5A40", ColorTone.COOL)
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedSwatchIndex = 0
            onPhotoChosen(uri, removeBg)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingCameraUri
        if (success && captured != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedSwatchIndex = 0
            onPhotoChosen(captured, removeBg)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempCameraUri(context)
            if (uri != null) {
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    fun launchCameraOrFallback() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val hasCameraPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPerm) {
            val uri = createTempCameraUri(context)
            if (uri != null) {
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun resetForm() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        name = ""
        category = WardrobeCategory.OUTER
        warmth = WarmthLevel.MEDIUM
        maxWear = 3
        removeBg = true
        selectedSwatchIndex = 0
        selectedWeathers = setOf("Hujan Ringan", "Berangin")
        selectedStyleTags = setOf("Casual")
        onRemovePhoto()
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
            selectedUri = scanState.sourceUri,
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Batal atau Tutup",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Tambah Pakaian",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = { resetForm() }) {
                            Text(
                                text = "Reset",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = fitColors.mutedText
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp,
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
                                contentColor = fitColors.onSoftIceTint
                            ),
                            modifier = Modifier
                                .heightIn(min = 52.dp)
                                .defaultMinSize(minWidth = 96.dp)
                        ) {
                            Text(
                                text = "Batal",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSaveItem(buildSaveRequest(isDraft = false))
                            },
                            enabled = name.isNotBlank() && !isProcessing && scanState.phase != MlScanPhase.SCANNING,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VoltLime,
                                contentColor = ObsidianDark
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = ObsidianDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Menyimpan ke Lemari...",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "Simpan ke Lemari",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Checkroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vision Engine pill + Replay Simulation button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = fitColors.softIceTint
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
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
                                letterSpacing = 0.7.sp,
                                color = fitColors.onSoftIceTint
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = fitColors.softIceTint,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReplayScan(removeBg)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Replay,
                                contentDescription = null,
                                tint = fitColors.onSoftIceTint,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Simulasi Scan",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = fitColors.onSoftIceTint
                            )
                        }
                    }
                }

                // Card 1: Foto Pakaian & ML Kit Auto-Cut Interactive Canvas
                PhotoAndMlKitCard(
                    scanState = scanState,
                    category = category,
                    activeColorHex = activeSwatch.hex,
                    removeBg = removeBg,
                    onToggleAutoCut = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        removeBg = !removeBg
                    },
                    onOpenCamera = { launchCameraOrFallback() },
                    onOpenGallery = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRescan = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onReplayScan(removeBg)
                    },
                    onDeletePhoto = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRemovePhoto()
                    },
                    onOpenFullscreen = { showFullscreenPreview = true }
                )

                // Card 2: Warna Terdeteksi (Android Palette API + Custom Swatch)
                CascadingRevealSection(visible = revealStep >= 1) {
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
                                            .background(fitColors.softIceTint),
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
                                            fontSize = 16.sp,
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
                                    val checkTint = if (swatchColor.luminance() > 0.65f) ObsidianDark else PureWhite

                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.surface else fitColors.softIceTint.copy(alpha = 0.55f),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SuccessGreen else fitColors.subtleBorder
                                        ),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .clickable(role = Role.RadioButton) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedSwatchIndex = index
                                            }
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
                                                        tint = checkTint,
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

                                // Custom Color (+) tile matching HTML prototype
                                item(key = "custom_color_btn", contentType = "customSwatch") {
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = fitColors.softIceTint.copy(alpha = 0.55f),
                                        border = BorderStroke(1.dp, fitColors.subtleBorder),
                                        modifier = Modifier
                                            .width(74.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .clickable(role = Role.Button) {
                                                showCustomColorDialog = true
                                            }
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
                                                    .background(fitColors.softIceTint),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Add,
                                                    contentDescription = "Tambah warna kustom",
                                                    tint = fitColors.onSoftIceTint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Text(
                                                text = "Kustom",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = fitColors.mutedText,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "-",
                                                fontFamily = PlusJakartaSans,
                                                fontSize = 10.sp,
                                                color = Color.Transparent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 3: Nama Pakaian & Kategori
                CascadingRevealSection(visible = revealStep >= 2) {
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
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            name = when (category) {
                                                WardrobeCategory.OUTER -> "Utility Overshirt ${activeSwatch.name}"
                                                WardrobeCategory.TOP -> "Kaos Katun ${activeSwatch.name}"
                                                WardrobeCategory.BOTTOM -> "Chino Relaxed ${activeSwatch.name}"
                                                WardrobeCategory.SHOES -> "Sneakers Low ${activeSwatch.name}"
                                                else -> "Topi Baseball ${activeSwatch.name}"
                                            }
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

                            Surface(
                                shape = CircleShape,
                                color = fitColors.softIceTint.copy(alpha = 0.72f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 50.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                            .clickable(role = Role.RadioButton) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                category = catKey
                                            }
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
                                                color = if (isSelected) PureWhite else fitColors.onSoftIceTint
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 4: Kesesuaian Cuaca + Level Kehangatan Material
                CascadingRevealSection(visible = revealStep >= 3) {
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
                                        fontSize = 16.sp,
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
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                                .heightIn(min = 40.dp)
                                                .clip(CircleShape)
                                                .clickable(role = Role.RadioButton) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    warmth = key
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
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
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 5 & 6: Batas Pakai Stepper + Gaya & Acara Tags
                CascadingRevealSection(visible = revealStep >= 4) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                            fontSize = 16.sp,
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
                                    onDecrement = {
                                        if (maxWear > 1) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            maxWear--
                                        }
                                    },
                                    onIncrement = {
                                        if (maxWear < 10) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            maxWear++
                                        }
                                    }
                                )
                            }
                        }

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
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                                    color = if (isSelected) VoltLime else fitColors.onSoftIceTint
                                                )
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(fitColors.softIceTint)
                                            .clickable(role = Role.Button, onClickLabel = "Tambah tag gaya") {
                                                customTagInput = ""
                                                showAddTagDialog = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Tambah tag",
                                            tint = fitColors.onSoftIceTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Draft quick helper link
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 44.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = !isProcessing, role = Role.Button) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
            }
        }

        // Floating celebratory ML Kit micro-toast near top
        AnimatedVisibility(
            visible = scanState.showExtractedToast,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(260)),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 62.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ObsidianDark.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, VoltLime.copy(alpha = 0.35f)),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(VoltLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = ObsidianDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "ML Kit berhasil mengekstrak fitur & warna pakaian!",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PureWhite,
                        maxLines = 2
                    )
                }
            }
        }
    }

    // Modal Konfirmasi Sukses "Pakaian Berhasil Disimpan!"
    if (savedItemModal != null) {
        SaveSuccessModal(
            item = savedItemModal,
            onViewInWardrobe = onViewInWardrobe,
            onAddAnother = {
                name = ""
                onAddAnotherItem()
            },
            onDismiss = onDismissSuccessModal
        )
    }

    // Fullscreen photo preview dialog
    if (showFullscreenPreview) {
        Dialog(
            onDismissRequest = { showFullscreenPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianDark.copy(alpha = 0.95f))
                    .clickable { showFullscreenPreview = false }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val previewFile = scanState.cutoutPreviewPath?.let(::File)?.takeIf { it.exists() }
                when {
                    previewFile != null -> AsyncImage(
                        model = previewFile,
                        contentDescription = "Fullscreen preview pakaian",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    scanState.sourceUri != null -> AsyncImage(
                        model = scanState.sourceUri,
                        contentDescription = "Fullscreen preview pakaian",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    else -> VectorGarmentSilhouette(
                        category = category,
                        baseColor = parseGarmentColor(activeSwatch.hex),
                        modifier = Modifier.size(260.dp)
                    )
                }

                IconButton(
                    onClick = { showFullscreenPreview = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .clip(CircleShape)
                        .background(PureWhite.copy(alpha = 0.14f))
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = PureWhite)
                }
            }
        }
    }

    // Custom Color Swatch Dialog
    if (showCustomColorDialog) {
        CustomColorPickerDialog(
            onDismiss = { showCustomColorDialog = false },
            onColorAdded = { customSwatch ->
                onAddCustomSwatch(customSwatch)
                selectedSwatchIndex = swatches.size
                showCustomColorDialog = false
            }
        )
    }

    // Custom Style Tag Dialog
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
private fun PhotoAndMlKitCard(
    scanState: AddItemScanUiState,
    category: String,
    activeColorHex: String,
    removeBg: Boolean,
    onToggleAutoCut: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onRescan: () -> Unit,
    onDeletePhoto: () -> Unit,
    onOpenFullscreen: () -> Unit
) {
    val fitColors = FitCastTheme.colors

    AddItemCardContainer {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
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
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Foto Pakaian",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ambil foto langsung atau unggah dari galeri",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = fitColors.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = if (removeBg) VoltLime else fitColors.softIceTint,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Switch, onClick = onToggleAutoCut)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = if (removeBg) ObsidianDark else fitColors.mutedText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when {
                                scanState.phase == MlScanPhase.SCANNING -> "Memindai..."
                                scanState.phase == MlScanPhase.SCANNED && removeBg -> "Auto-Cut AI ✓"
                                removeBg -> "Auto-Cut AI"
                                else -> "Auto-Cut Off"
                            },
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = if (removeBg) ObsidianDark else fitColors.mutedText
                        )
                    }
                }
            }

            // Interactive Canvas: Empty Upload Zone vs Scanning/Scanned Stage
            AnimatedContent(
                targetState = scanState.phase == MlScanPhase.EMPTY,
                transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(180)) },
                label = "photoCanvasStage"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyUploadDropZone(
                        removeBg = removeBg,
                        onTapUpload = onOpenGallery
                    )
                } else {
                    UploadedScanningCanvas(
                        scanState = scanState,
                        category = category,
                        activeColorHex = activeColorHex,
                        onOpenFullscreen = onOpenFullscreen
                    )
                }
            }

            // Context-aware bottom action row (2 buttons when empty, 3 buttons when uploaded/scanning)
            if (scanState.phase == MlScanPhase.EMPTY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = fitColors.softIceTint,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onOpenCamera)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kamera Foto",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = fitColors.onSoftIceTint,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = fitColors.softIceTint,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onOpenGallery)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = null,
                                tint = ElectricCobalt,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buka Galeri",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = fitColors.onSoftIceTint,
                                maxLines = 1
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = VoltLime,
                        modifier = Modifier
                            .weight(1.15f)
                            .heightIn(min = 44.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = scanState.phase != MlScanPhase.SCANNING,
                                role = Role.Button,
                                onClick = onRescan
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DocumentScanner,
                                contentDescription = null,
                                tint = ObsidianDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (scanState.phase == MlScanPhase.SCANNING) "Memindai..." else "Pindai Ulang",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = ObsidianDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = fitColors.softIceTint,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onOpenGallery)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Ganti Foto",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = fitColors.onSoftIceTint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = fitColors.softDangerTint.copy(alpha = 0.65f),
                        modifier = Modifier
                            .weight(0.85f)
                            .heightIn(min = 44.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onDeletePhoto)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = LaundryRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hapus",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = LaundryRed,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyUploadDropZone(
    removeBg: Boolean,
    onTapUpload: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    val dashColor = fitColors.mutedText.copy(alpha = 0.42f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 256.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fitColors.softIceTint.copy(alpha = 0.55f))
            .drawBehind {
                // Subtle technical dot-grid pattern
                val dotGap = 14.dp.toPx()
                val dotColor = dashColor.copy(alpha = 0.35f)
                var y = dotGap / 2f
                while (y < size.height) {
                    var x = dotGap / 2f
                    while (x < size.width) {
                        drawCircle(dotColor, radius = 1.dp.toPx(), center = Offset(x, y))
                        x += dotGap
                    }
                    y += dotGap
                }
                // Dashed outline border
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )
            }
            .clickable(role = Role.Button, onClick = onTapUpload)
            .padding(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = ObsidianDark.copy(alpha = 0.90f),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoFixHigh,
                    contentDescription = null,
                    tint = VoltLime,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (removeBg) "ML Kit Auto-Cut Aktif" else "Auto-Cut Nonaktif",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PureWhite
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.size(62.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Text(
                text = "Ketuk untuk Ambil Foto atau Unggah",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Foto bajumu sekali, ML Kit hapus latar otomatis",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = fitColors.mutedText,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FormatInfoChip(text = "JPG, PNG atau WebP")
                FormatInfoChip(text = "Maks. 10MB")
            }
        }
    }
}

@Composable
private fun FormatInfoChip(text: String) {
    Surface(
        shape = CircleShape,
        color = FitCastTheme.colors.softIceTint
    ) {
        Text(
            text = text,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = FitCastTheme.colors.mutedText,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun UploadedScanningCanvas(
    scanState: AddItemScanUiState,
    category: String,
    activeColorHex: String,
    onOpenFullscreen: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    val isScanning = scanState.phase == MlScanPhase.SCANNING

    val infiniteTransition = rememberInfiniteTransition(label = "mlKitScanHud")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLaserY"
    )
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )
    val landmarkPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "landmarkPulse"
    )

    val cutoutFile = remember(scanState.cutoutPreviewPath) {
        scanState.cutoutPreviewPath?.let(::File)?.takeIf { it.exists() }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(286.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fitColors.softIceTint.copy(alpha = 0.65f))
            .border(1.dp, fitColors.subtleBorder, RoundedCornerShape(18.dp))
            .drawBehind {
                val dotGap = 13.dp.toPx()
                val dotColor = Color(0xFF5D5E60).copy(alpha = 0.24f)
                var y = dotGap / 2f
                while (y < size.height) {
                    var x = dotGap / 2f
                    while (x < size.width) {
                        drawCircle(dotColor, radius = 1.dp.toPx(), center = Offset(x, y))
                        x += dotGap
                    }
                    y += dotGap
                }
            }
    ) {
        val boxHeight = maxHeight

        // Garment photo or segmented cutout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                cutoutFile != null -> AsyncImage(
                    model = cutoutFile,
                    contentDescription = "Hasil Auto-Cut ML Kit",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                scanState.sourceUri != null -> AsyncImage(
                    model = scanState.sourceUri,
                    contentDescription = "Foto pakaian yang dipilih",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                else -> VectorGarmentSilhouette(
                    category = category,
                    baseColor = parseGarmentColor(activeColorHex, Color(0xFF3D5A40)),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
        }

        // Scanning HUD: 4 Volt Lime corner brackets, sweeping laser line & 4 landmark points
        AnimatedVisibility(
            visible = isScanning,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(320)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pad = 16.dp.toPx()
                    val arm = 20.dp.toPx()
                    val stroke = 2.4.dp.toPx()

                    // 4 HUD corner brackets
                    drawLine(VoltLime, Offset(pad, pad), Offset(pad + arm, pad), stroke, StrokeCap.Round)
                    drawLine(VoltLime, Offset(pad, pad), Offset(pad, pad + arm), stroke, StrokeCap.Round)

                    drawLine(VoltLime, Offset(size.width - pad, pad), Offset(size.width - pad - arm, pad), stroke, StrokeCap.Round)
                    drawLine(VoltLime, Offset(size.width - pad, pad), Offset(size.width - pad, pad + arm), stroke, StrokeCap.Round)

                    drawLine(VoltLime, Offset(pad, size.height - pad), Offset(pad + arm, size.height - pad), stroke, StrokeCap.Round)
                    drawLine(VoltLime, Offset(pad, size.height - pad), Offset(pad, size.height - pad - arm), stroke, StrokeCap.Round)

                    drawLine(VoltLime, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - arm, size.height - pad), stroke, StrokeCap.Round)
                    drawLine(VoltLime, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - arm), stroke, StrokeCap.Round)

                    // Feature landmarks (Collar, Center Button, Left Pocket, Right Pocket)
                    val landmarks = listOf(
                        Offset(size.width * 0.49f, size.height * 0.25f),
                        Offset(size.width * 0.50f, size.height * 0.50f),
                        Offset(size.width * 0.34f, size.height * 0.56f),
                        Offset(size.width * 0.66f, size.height * 0.56f)
                    )
                    landmarks.forEach { pt ->
                        drawCircle(
                            color = VoltLime.copy(alpha = 0.28f),
                            radius = 9.dp.toPx() * landmarkPulse,
                            center = pt
                        )
                        drawCircle(color = ObsidianDark, radius = 5.5.dp.toPx(), center = pt)
                        drawCircle(color = VoltLime, radius = 3.8.dp.toPx(), center = pt)
                    }
                }

                // Sweeping Volt Lime Laser Line (phase-deferred via graphicsLayer)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(3.dp)
                        .graphicsLayer {
                            translationY = boxHeight.toPx() * laserProgress
                        }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    VoltLime,
                                    PureWhite,
                                    VoltLime,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // Top-left Scanning / Completed Overlay Pill
        Surface(
            shape = CircleShape,
            color = ObsidianDark.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, VoltLime.copy(alpha = 0.32f)),
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
                    imageVector = if (isScanning) Icons.Outlined.Radar else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = VoltLime,
                    modifier = Modifier
                        .size(15.dp)
                        .graphicsLayer {
                            rotationZ = if (isScanning) radarRotation else 0f
                        }
                )
                Text(
                    text = if (isScanning) "Memindai Kontur ML Kit" else "Auto-Cut Berhasil Dipindai",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PureWhite,
                    maxLines = 1
                )
                Surface(
                    shape = CircleShape,
                    color = VoltLime
                ) {
                    Text(
                        text = if (isScanning) "${scanState.scanProgressPercent}%" else "Selesai",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        color = ObsidianDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Top-right Fullscreen preview button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .clickable(role = Role.Button, onClickLabel = "Lihat Fullscreen", onClick = onOpenFullscreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Fullscreen,
                contentDescription = "Lihat Fullscreen",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        // Bottom status chips: Latar Dihapus (ML Kit) & Resolution
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 2.dp
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
                            .background(if (isScanning) VoltLime else SuccessGreen)
                    )
                    Text(
                        text = if (isScanning) "ML Kit Vision Active" else "Latar Dihapus (ML Kit)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = scanState.resolutionLabel,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = fitColors.mutedText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun CascadingRevealSection(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320)) + slideInVertically(
            animationSpec = tween(360, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 8 }
        ),
        exit = fadeOut(tween(150))
    ) {
        content()
    }
}

@Composable
private fun SaveSuccessModal(
    item: WardrobeItemEntity,
    onViewInWardrobe: () -> Unit,
    onAddAnother: () -> Unit,
    onDismiss: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    val popScale = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        popScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }

    val categoryLabel = when (item.category) {
        WardrobeCategory.TOP -> "Atasan"
        WardrobeCategory.OUTER -> "Outer"
        WardrobeCategory.BOTTOM -> "Bawahan"
        WardrobeCategory.SHOES -> "Sepatu"
        else -> "Aksesoris"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianDark.copy(alpha = 0.62f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, fitColors.subtleBorder),
                shadowElevation = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = popScale.value
                        scaleY = popScale.value
                    }
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(VoltLime.copy(alpha = 0.24f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VoltLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ObsidianDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = VoltLime.copy(alpha = 0.26f)
                    ) {
                        Text(
                            text = "FITCAST WARDROBE SYNC",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Pakaian Berhasil Disimpan!",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Item telah masuk katalog lemari digitalmu dan siap digunakan untuk rekomendasi outfit cuaca.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = fitColors.mutedText,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Mini garment preview card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = fitColors.softIceTint.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, fitColors.subtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GarmentVisualImage(item = item, modifier = Modifier.fillMaxSize())
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = CircleShape, color = ObsidianDark) {
                                        Text(
                                            text = categoryLabel,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = VoltLime,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(parseGarmentColor(item.colorHex))
                                    )
                                    Text(
                                        text = "${item.colorName.ifBlank { "Netral" }} • Batas ${item.maxWearBeforeWash}x",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = fitColors.mutedText
                                    )
                                }
                            }
                        }
                    }

                    // Primary modal action: Lihat di Lemari Digital
                    Button(
                        onClick = onViewInWardrobe,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ObsidianDark,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                    ) {
                        Text(
                            text = "Lihat di Lemari Digital",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = VoltLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddAnother,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fitColors.softIceTint,
                                contentColor = fitColors.onSoftIceTint
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddCircleOutline,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Tambah Baru",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fitColors.softIceTint.copy(alpha = 0.6f),
                                contentColor = fitColors.mutedText
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                        ) {
                            Text(
                                text = "Tutup",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomColorPickerDialog(
    onDismiss: () -> Unit,
    onColorAdded: (DetectedColorSwatch) -> Unit
) {
    val presets = remember {
        listOf(
            DetectedColorSwatch("Navy", "#1E3A8A", ColorTone.COOL),
            DetectedColorSwatch("Terracotta", "#B45309", ColorTone.WARM),
            DetectedColorSwatch("Charcoal", "#1E293B", ColorTone.NEUTRAL),
            DetectedColorSwatch("Cream", "#E5D3B3", ColorTone.WARM),
            DetectedColorSwatch("Teal", "#0F766E", ColorTone.COOL),
            DetectedColorSwatch("Burgundy", "#881337", ColorTone.WARM)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Warna Kustom", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FitCastTheme.colors.softIceTint,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onColorAdded(preset) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(parseGarmentColor(preset.hex))
                                    )
                                    Text(
                                        text = preset.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = FitCastTheme.colors.onSoftIceTint,
                                        maxLines = 1
                                    )
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
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Checkbox, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
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
                color = if (selected) ElectricCobalt else fitColors.onSoftIceTint,
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
internal fun WearLimitStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val fitColors = FitCastTheme.colors
    Surface(
        shape = CircleShape,
        color = fitColors.softIceTint
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "${value}x",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = fitColors.onSoftIceTint
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun createTempCameraUri(context: Context): Uri? {
    return runCatching {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull()
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1280)
@Composable
private fun AddItemEmptyUploadPreview() {
    FitCastTheme(darkTheme = false) {
        AddItemScreenContent(
            swatches = WardrobeViewModel.DEFAULT_PALETTE_SWATCHES,
            scanState = AddItemScanUiState(phase = MlScanPhase.EMPTY),
            isProcessing = false,
            savedItemModal = null,
            onPhotoChosen = { _, _ -> },
            onReplayScan = {},
            onRemovePhoto = {},
            onAddCustomSwatch = {},
            onSaveItem = {},
            onDismissSuccessModal = {},
            onViewInWardrobe = {},
            onAddAnotherItem = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 920)
@Composable
private fun AddItemScanningHudPreview() {
    FitCastTheme(darkTheme = false) {
        AddItemScreenContent(
            swatches = WardrobeViewModel.DEFAULT_PALETTE_SWATCHES,
            scanState = AddItemScanUiState(
                phase = MlScanPhase.SCANNING,
                scanProgressPercent = 78,
                resolutionLabel = "1024 × 1024 px"
            ),
            isProcessing = false,
            savedItemModal = null,
            onPhotoChosen = { _, _ -> },
            onReplayScan = {},
            onRemovePhoto = {},
            onAddCustomSwatch = {},
            onSaveItem = {},
            onDismissSuccessModal = {},
            onViewInWardrobe = {},
            onAddAnotherItem = {},
            onBack = {}
        )
    }
}
