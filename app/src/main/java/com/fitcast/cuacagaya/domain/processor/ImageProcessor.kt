package com.fitcast.cuacagaya.domain.processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
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

data class ProcessedClothingImage(
    val savedFilePath: String,
    val dominantColorHex: String,
    val colorTone: String
)

class ImageProcessor(private val context: Context) {

    suspend fun processAndSaveClothingPhoto(
        uri: Uri,
        removeBackground: Boolean = true
    ): ProcessedClothingImage? = withContext(Dispatchers.IO) {
        val originalBitmap = decodeSampledBitmap(uri) ?: return@withContext null

        val finalBitmap = if (removeBackground) {
            removeBackgroundWithMlKit(originalBitmap) ?: originalBitmap
        } else {
            originalBitmap
        }

        val (hex, tone) = extractDominantColor(finalBitmap)
        val file = File(context.filesDir, "cloth_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        ProcessedClothingImage(
            savedFilePath = file.absolutePath,
            dominantColorHex = hex,
            colorTone = tone
        )
    }

    private suspend fun removeBackgroundWithMlKit(bitmap: Bitmap): Bitmap? =
        suspendCancellableCoroutine { cont ->
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build()
            val segmenter = SubjectSegmentation.getClient(options)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            segmenter.process(inputImage)
                .addOnSuccessListener { result ->
                    if (cont.isActive) cont.resume(result.foregroundBitmap)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }

    private fun extractDominantColor(bitmap: Bitmap): Pair<String, String> {
        val palette = Palette.from(bitmap).generate()
        val dominantInt = palette.getDominantColor(Color.DKGRAY)
        val hex = String.format("#%06X", 0xFFFFFF and dominantInt)

        val hsv = FloatArray(3)
        Color.colorToHSV(dominantInt, hsv)
        val hue = hsv[0]
        val sat = hsv[1]
        val value = hsv[2]

        val tone = when {
            sat < 0.18f || value < 0.22f || (value > 0.88f && sat < 0.25f) -> ColorTone.NEUTRAL
            hue in 0f..65f || hue >= 300f -> ColorTone.WARM
            else -> ColorTone.COOL
        }
        return hex to tone
    }

    private fun decodeSampledBitmap(uri: Uri, maxSize: Int = 800): Bitmap? {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return null
            val raw = BitmapFactory.decodeStream(stream)
            stream.close()
            if (raw == null) return null
            val ratio = minOf(maxSize.toFloat() / raw.width, maxSize.toFloat() / raw.height, 1f)
            Bitmap.createScaledBitmap(
                raw,
                (raw.width * ratio).toInt().coerceAtLeast(1),
                (raw.height * ratio).toInt().coerceAtLeast(1),
                true
            )
        } catch (_: Exception) {
            null
        }
    }
}
