package com.fitcast.cuacagaya.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.fitcast.cuacagaya.data.local.entity.WardrobeCategory
import com.fitcast.cuacagaya.data.local.entity.WardrobeItemEntity
import java.io.File

fun parseGarmentColor(hex: String?, fallback: Color = Color(0xFF475569)): Color {
    if (hex.isNullOrBlank()) return fallback
    return runCatching { Color(AndroidColor.parseColor(hex)) }.getOrDefault(fallback)
}

@Composable
fun GarmentVisualImage(
    item: WardrobeItemEntity?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val localFile = remember(item?.imagePath) {
        val path = item?.imagePath.orEmpty()
        if (path.isNotBlank()) File(path).takeIf { it.exists() } else null
    }
    val drawableResId = remember(item?.drawableResName) {
        val resName = item?.drawableResName.orEmpty()
        if (resName.isNotBlank()) {
            context.resources.getIdentifier(resName, "drawable", context.packageName)
        } else 0
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            localFile != null -> {
                AsyncImage(
                    model = localFile,
                    contentDescription = item?.name,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }

            drawableResId != 0 -> {
                Image(
                    painter = painterResource(id = drawableResId),
                    contentDescription = item?.name,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                VectorGarmentSilhouette(
                    category = item?.category ?: WardrobeCategory.TOP,
                    baseColor = parseGarmentColor(item?.colorHex),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun VectorGarmentSilhouette(
    category: String,
    baseColor: Color,
    modifier: Modifier = Modifier
) {
    val isLightFabric = baseColor.luminance() > 0.62f
    val shadowColor = remember(baseColor, isLightFabric) {
        if (isLightFabric) Color(0xFFD0D5DC) else Color(
            red = (baseColor.red * 0.68f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.68f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 0.68f).coerceIn(0f, 1f)
        )
    }
    val highlightColor = remember(baseColor, isLightFabric) {
        if (isLightFabric) Color.White else Color(
            red = (baseColor.red + (1f - baseColor.red) * 0.22f).coerceIn(0f, 1f),
            green = (baseColor.green + (1f - baseColor.green) * 0.22f).coerceIn(0f, 1f),
            blue = (baseColor.blue + (1f - baseColor.blue) * 0.22f).coerceIn(0f, 1f)
        )
    }
    val seamColor = if (isLightFabric) Color(0xFF94A3B8).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.22f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val fabricBrush = Brush.linearGradient(
            colors = listOf(highlightColor, baseColor, shadowColor),
            start = Offset(w * 0.15f, h * 0.1f),
            end = Offset(w * 0.85f, h * 0.92f)
        )
        val strokeStyle = Stroke(width = (w * 0.018f).coerceIn(1.5f, 3.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (category) {
            WardrobeCategory.OUTER -> {
                val jacketPath = Path().apply {
                    moveTo(w * 0.36f, h * 0.16f)
                    quadraticTo(w * 0.50f, h * 0.08f, w * 0.64f, h * 0.16f)
                    lineTo(w * 0.78f, h * 0.24f)
                    lineTo(w * 0.90f, h * 0.64f)
                    lineTo(w * 0.77f, h * 0.68f)
                    lineTo(w * 0.72f, h * 0.42f)
                    lineTo(w * 0.73f, h * 0.86f)
                    lineTo(w * 0.27f, h * 0.86f)
                    lineTo(w * 0.28f, h * 0.42f)
                    lineTo(w * 0.23f, h * 0.68f)
                    lineTo(w * 0.10f, h * 0.64f)
                    lineTo(w * 0.22f, h * 0.24f)
                    close()
                }
                drawPath(jacketPath, brush = fabricBrush)
                drawPath(jacketPath, color = seamColor, style = strokeStyle)
                // Collar & central zipper
                drawLine(seamColor, Offset(w * 0.50f, h * 0.19f), Offset(w * 0.50f, h * 0.85f), strokeStyle.width)
                drawLine(seamColor, Offset(w * 0.34f, h * 0.58f), Offset(w * 0.43f, h * 0.68f), strokeStyle.width)
                drawLine(seamColor, Offset(w * 0.66f, h * 0.58f), Offset(w * 0.57f, h * 0.68f), strokeStyle.width)
            }

            WardrobeCategory.BOTTOM -> {
                val pantsPath = Path().apply {
                    moveTo(w * 0.26f, h * 0.14f)
                    lineTo(w * 0.74f, h * 0.14f)
                    lineTo(w * 0.78f, h * 0.88f)
                    lineTo(w * 0.56f, h * 0.88f)
                    lineTo(w * 0.50f, h * 0.44f)
                    lineTo(w * 0.44f, h * 0.88f)
                    lineTo(w * 0.22f, h * 0.88f)
                    close()
                }
                drawPath(pantsPath, brush = fabricBrush)
                drawPath(pantsPath, color = seamColor, style = strokeStyle)
                // Waistband and fly seam
                drawLine(seamColor, Offset(w * 0.26f, h * 0.22f), Offset(w * 0.74f, h * 0.22f), strokeStyle.width)
                drawLine(seamColor, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.36f), strokeStyle.width)
            }

            WardrobeCategory.SHOES -> {
                val shoePath = Path().apply {
                    moveTo(w * 0.16f, h * 0.42f)
                    lineTo(w * 0.36f, h * 0.34f)
                    lineTo(w * 0.56f, h * 0.54f)
                    lineTo(w * 0.84f, h * 0.58f)
                    quadraticTo(w * 0.90f, h * 0.68f, w * 0.82f, h * 0.74f)
                    lineTo(w * 0.16f, h * 0.74f)
                    close()
                }
                drawPath(shoePath, brush = fabricBrush)
                drawPath(shoePath, color = seamColor, style = strokeStyle)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.15f, h * 0.70f),
                    size = Size(w * 0.70f, h * 0.08f),
                    cornerRadius = CornerRadius(h * 0.04f, h * 0.04f)
                )
            }

            else -> {
                val teePath = Path().apply {
                    moveTo(w * 0.36f, h * 0.16f)
                    quadraticTo(w * 0.50f, h * 0.24f, w * 0.64f, h * 0.16f)
                    lineTo(w * 0.86f, h * 0.32f)
                    lineTo(w * 0.76f, h * 0.48f)
                    lineTo(w * 0.68f, h * 0.42f)
                    lineTo(w * 0.68f, h * 0.86f)
                    lineTo(w * 0.32f, h * 0.86f)
                    lineTo(w * 0.32f, h * 0.42f)
                    lineTo(w * 0.24f, h * 0.48f)
                    lineTo(w * 0.14f, h * 0.32f)
                    close()
                }
                drawPath(teePath, brush = fabricBrush)
                drawPath(teePath, color = seamColor, style = strokeStyle)
                // Neckline rib
                val collar = Path().apply {
                    moveTo(w * 0.38f, h * 0.17f)
                    quadraticTo(w * 0.50f, h * 0.27f, w * 0.62f, h * 0.17f)
                }
                drawPath(collar, color = seamColor, style = strokeStyle)
            }
        }
    }
}
