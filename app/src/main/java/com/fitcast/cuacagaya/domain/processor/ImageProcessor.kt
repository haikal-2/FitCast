package com.fitcast.cuacagaya.domain.processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import androidx.core.graphics.scale
import androidx.palette.graphics.Palette
import com.fitcast.cuacagaya.data.local.entity.ColorTone
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

data class DetectedColorSwatch(
    val name: String,
    val hex: String,
    val tone: String
)

data class ProcessedClothingImage(
    val savedFilePath: String,
    val dominantColorHex: String,
    val colorTone: String,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val swatches: List<DetectedColorSwatch> = emptyList()
)

class ImageProcessor(private val context: Context) {

    suspend fun extractColorSwatches(uri: Uri): List<DetectedColorSwatch> = withContext(Dispatchers.IO) {
        val bitmap = decodeSampledBitmap(uri, maxSize = 480) ?: return@withContext emptyList()
        extractSwatchesFromBitmap(bitmap)
    }

    suspend fun analyzeAndCutoutPreview(
        uri: Uri,
        removeBackground: Boolean = true
    ): ProcessedClothingImage? = withContext(Dispatchers.IO) {
        val originalBitmap = decodeSampledBitmap(uri, maxSize = 960) ?: return@withContext null
        val finalBitmap = if (removeBackground) {
            removeBackgroundWithMlKit(originalBitmap) ?: originalBitmap
        } else {
            originalBitmap
        }

        val swatches = extractSwatchesFromBitmap(finalBitmap)
        val (hex, tone) = swatches.firstOrNull()?.let { it.hex to it.tone }
            ?: extractDominantColor(finalBitmap)

        val cacheDir = File(context.cacheDir, "mlkit_preview").apply { mkdirs() }
        val previewFile = File(cacheDir, "preview_${System.currentTimeMillis()}.png")
        FileOutputStream(previewFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        val width = finalBitmap.width
        val height = finalBitmap.height
        if (finalBitmap !== originalBitmap) originalBitmap.recycle()

        ProcessedClothingImage(
            savedFilePath = previewFile.absolutePath,
            dominantColorHex = hex,
            colorTone = tone,
            widthPx = width,
            heightPx = height,
            swatches = swatches
        )
    }

    suspend fun processAndSaveClothingPhoto(
        uri: Uri,
        removeBackground: Boolean = true,
        existingPreviewPath: String? = null
    ): ProcessedClothingImage? = withContext(Dispatchers.IO) {
        if (!existingPreviewPath.isNullOrBlank()) {
            val previewFile = File(existingPreviewPath)
            if (previewFile.exists()) {
                val targetFile = File(context.filesDir, "cloth_${System.currentTimeMillis()}.png")
                previewFile.copyTo(targetFile, overwrite = true)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(targetFile.absolutePath, bounds)
                return@withContext ProcessedClothingImage(
                    savedFilePath = targetFile.absolutePath,
                    dominantColorHex = "#3D5A40",
                    colorTone = ColorTone.NEUTRAL,
                    widthPx = bounds.outWidth.coerceAtLeast(0),
                    heightPx = bounds.outHeight.coerceAtLeast(0)
                )
            }
        }

        val originalBitmap = decodeSampledBitmap(uri, maxSize = 960) ?: return@withContext null
        val finalBitmap = if (removeBackground) {
            removeBackgroundWithMlKit(originalBitmap) ?: originalBitmap
        } else {
            originalBitmap
        }

        val swatches = extractSwatchesFromBitmap(finalBitmap)
        val (hex, tone) = swatches.firstOrNull()?.let { it.hex to it.tone }
            ?: extractDominantColor(finalBitmap)

        val file = File(context.filesDir, "cloth_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        val width = finalBitmap.width
        val height = finalBitmap.height
        if (finalBitmap !== originalBitmap) originalBitmap.recycle()

        ProcessedClothingImage(
            savedFilePath = file.absolutePath,
            dominantColorHex = hex,
            colorTone = tone,
            widthPx = width,
            heightPx = height,
            swatches = swatches
        )
    }

    private suspend fun removeBackgroundWithMlKit(bitmap: Bitmap): Bitmap? =
        suspendCancellableCoroutine { cont ->
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build()
            val segmenter = SubjectSegmentation.getClient(options)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            cont.invokeOnCancellation {
                runCatching { segmenter.close() }
            }

            segmenter.process(inputImage)
                .addOnSuccessListener { result ->
                    runCatching { segmenter.close() }
                    if (cont.isActive) cont.resume(result.foregroundBitmap)
                }
                .addOnFailureListener {
                    runCatching { segmenter.close() }
                    if (cont.isActive) cont.resume(null)
                }
                .addOnCanceledListener {
                    runCatching { segmenter.close() }
                    if (cont.isActive) cont.resume(null)
                }
        }

    private fun extractSwatchesFromBitmap(bitmap: Bitmap): List<DetectedColorSwatch> {
        val palette = Palette.from(bitmap).maximumColorCount(8).generate()
        val candidates = listOfNotNull(
            palette.dominantSwatch,
            palette.vibrantSwatch,
            palette.mutedSwatch,
            palette.darkMutedSwatch,
            palette.lightMutedSwatch,
            palette.darkVibrantSwatch
        ) + palette.swatches.sortedByDescending { it.population }

        val seenHex = mutableSetOf<String>()
        val result = mutableListOf<DetectedColorSwatch>()
        for (swatch in candidates) {
            val hex = String.format("#%06X", 0xFFFFFF and swatch.rgb)
            if (seenHex.add(hex)) {
                result.add(
                    DetectedColorSwatch(
                        name = describeColorName(swatch.rgb),
                        hex = hex,
                        tone = classifyTone(swatch.rgb)
                    )
                )
            }
            if (result.size >= 4) break
        }
        return result
    }

    private fun extractDominantColor(bitmap: Bitmap): Pair<String, String> {
        val palette = Palette.from(bitmap).generate()
        val dominantInt = palette.getDominantColor(Color.DKGRAY)
        val hex = String.format("#%06X", 0xFFFFFF and dominantInt)
        return hex to classifyTone(dominantInt)
    }

    private fun classifyTone(colorInt: Int): String {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorInt, hsv)
        val hue = hsv[0]
        val sat = hsv[1]
        val value = hsv[2]

        return when {
            sat < 0.18f || value < 0.22f || (value > 0.88f && sat < 0.25f) -> ColorTone.NEUTRAL
            hue in 0f..65f || hue >= 300f -> ColorTone.WARM
            else -> ColorTone.COOL
        }
    }

    private fun describeColorName(colorInt: Int): String {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorInt, hsv)
        val hue = hsv[0]
        val sat = hsv[1]
        val value = hsv[2]

        if (value < 0.20f) return "Charcoal"
        if (sat < 0.14f) {
            return when {
                value > 0.86f -> "Off-White"
                value > 0.65f -> "Silver"
                else -> "Slate"
            }
        }
        return when {
            hue < 18f || hue >= 342f -> if (value < 0.55f) "Maroon" else "Crimson"
            hue < 42f -> if (value < 0.6f) "Espresso" else "Sand"
            hue < 68f -> if (sat < 0.35f) "Cream" else "Mustard"
            hue < 130f -> if (value < 0.45f) "Dark Moss" else if (sat < 0.45f) "Sage" else "Olive"
            hue < 195f -> "Teal"
            hue < 250f -> if (value < 0.48f) "Navy" else "Cobalt"
            hue < 295f -> "Indigo"
            else -> "Berry"
        }
    }

    // Two-pass subsampled decode + EXIF rotation prevents OOM and sideways camera photos.
    private fun decodeSampledBitmap(uri: Uri, maxSize: Int = 960): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            } ?: return null

            val rawWidth = bounds.outWidth
            val rawHeight = bounds.outHeight
            if (rawWidth <= 0 || rawHeight <= 0) return null

            var sampleSize = 1
            while (rawWidth / (sampleSize * 2) >= maxSize || rawHeight / (sampleSize * 2) >= maxSize) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val sampled = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            val oriented = applyExifOrientation(uri, sampled)
            val ratio = minOf(maxSize.toFloat() / oriented.width, maxSize.toFloat() / oriented.height, 1f)
            if (ratio >= 0.99f) {
                oriented
            } else {
                val targetW = (oriented.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (oriented.height * ratio).toInt().coerceAtLeast(1)
                val scaled = oriented.scale(targetW, targetH)
                if (scaled !== oriented) oriented.recycle()
                scaled
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun applyExifOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }

        return runCatching {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) bitmap.recycle()
            rotated
        }.getOrDefault(bitmap)
    }
}
