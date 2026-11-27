package com.fitcast.cuacagaya.ui.onboarding

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DryCleaning
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.fitcast.cuacagaya.ui.theme.ElectricCobalt
import com.fitcast.cuacagaya.ui.theme.FitCastTheme
import com.fitcast.cuacagaya.ui.theme.ObsidianDark
import com.fitcast.cuacagaya.ui.theme.PlusJakartaSans
import com.fitcast.cuacagaya.ui.theme.PureWhite
import com.fitcast.cuacagaya.ui.theme.SurfaceCardDark
import com.fitcast.cuacagaya.ui.theme.VoltLime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashMidnightTop = Color(0xFF0B1622)
private val SplashMidnightMid = Color(0xFF0F1F30)
private val SplashMidnightBottom = Color(0xFF0D1114)
private val SplashSilhouetteRim = Color(0xFF1D4ED8)
private val OnboardingDarkCanvas = Color(0xFF121415)
private val OnboardingDarkMuted = Color(0xFFA0AAA6)
private val OnboardingCardFill = Color(0xFF1B2023)
private val TealAccent = Color(0xFF2DD4BF)
private val SandBeige = Color(0xFFD6C5A9)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Crossfade(
        targetState = showSplash,
        animationSpec = tween(durationMillis = 360),
        label = "splashToOnboarding"
    ) { isSplash ->
        if (isSplash) {
            SplashLaunchScreen(onComplete = { showSplash = false })
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(OnboardingDarkCanvas)
            ) { page ->
                when (page) {
                    0 -> OnboardingWeatherAlertPage(
                        onSkip = onFinish,
                        onNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )

                    else -> OnboardingSmartWardrobePage(onStart = onFinish)
                }
            }
        }
    }
}

@Composable
fun SplashLaunchScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    autoAdvance: Boolean = true
) {
    val progress = remember { Animatable(0f) }
    var completed by remember { mutableStateOf(false) }

    fun triggerComplete() {
        if (!completed) {
            completed = true
            onComplete()
        }
    }

    LaunchedEffect(autoAdvance) {
        if (autoAdvance) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
            )
            triggerComplete()
        } else {
            progress.snapTo(0.45f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SplashMidnightTop, SplashMidnightMid, SplashMidnightBottom)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = "Lanjut ke pengenalan FitCast",
                onClick = { triggerComplete() }
            )
            .semantics { contentDescription = "FitCast CuacaGaya. Ketuk untuk melanjutkan." }
    ) {
        SplashBackdropAndSilhouette(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.35f))
            Text(
                text = "FitCast",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                lineHeight = 46.sp,
                letterSpacing = (-0.8).sp,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CUACAGAYA",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 4.6.sp,
                color = VoltLime
            )
            Spacer(modifier = Modifier.weight(0.65f))
        }

        // Progress fill reads Animatable in Draw phase to avoid per-frame recompositions.
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
                .width(136.dp)
                .height(3.5.dp)
        ) {
            val radius = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(
                color = PureWhite.copy(alpha = 0.16f),
                cornerRadius = radius
            )
            val fillWidth = (size.width * progress.value).coerceIn(0f, size.width)
            if (fillWidth > 0f) {
                drawRoundRect(
                    color = PureWhite.copy(alpha = 0.88f),
                    size = Size(fillWidth, size.height),
                    cornerRadius = radius
                )
            }
        }
    }
}

@Composable
private fun SplashBackdropAndSilhouette(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height

            val outlinePath = Path().apply {
                moveTo(-w * 0.04f, h * 0.72f)
                cubicTo(
                    w * 0.10f, h * 0.66f,
                    w * 0.22f, h * 0.635f,
                    w * 0.31f, h * 0.62f
                )
                cubicTo(
                    w * 0.36f, h * 0.56f,
                    w * 0.425f, h * 0.515f,
                    w * 0.50f, h * 0.515f
                )
                cubicTo(
                    w * 0.575f, h * 0.515f,
                    w * 0.64f, h * 0.56f,
                    w * 0.69f, h * 0.62f
                )
                cubicTo(
                    w * 0.78f, h * 0.635f,
                    w * 0.90f, h * 0.66f,
                    w * 1.04f, h * 0.72f
                )
            }

            val bodyFillPath = Path().apply {
                addPath(outlinePath)
                lineTo(w * 1.04f, h * 1.02f)
                lineTo(-w * 0.04f, h * 1.02f)
                close()
            }

            val leftHoodFacet = Path().apply {
                moveTo(w * 0.50f, h * 0.525f)
                lineTo(w * 0.39f, h * 0.65f)
                lineTo(w * 0.50f, h * 0.76f)
                close()
            }

            val rightHoodFacet = Path().apply {
                moveTo(w * 0.50f, h * 0.525f)
                lineTo(w * 0.61f, h * 0.65f)
                lineTo(w * 0.50f, h * 0.76f)
                close()
            }

            val ambientGlow = Brush.radialGradient(
                colors = listOf(
                    SplashSilhouetteRim.copy(alpha = 0.20f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.50f),
                radius = w * 0.78f
            )

            val silhouetteFill = Brush.verticalGradient(
                colors = listOf(Color(0xFF101720), Color(0xFF0A0D10), Color(0xFF080A0C)),
                startY = h * 0.51f,
                endY = h
            )

            onDrawBehind {
                // Subtle diagonal fabric weave texture
                val step = 18.dp.toPx()
                var x = -h * 0.4f
                while (x < w + h * 0.4f) {
                    drawLine(
                        color = PureWhite.copy(alpha = 0.024f),
                        start = Offset(x, 0f),
                        end = Offset(x - h * 0.22f, h),
                        strokeWidth = 1f
                    )
                    x += step
                }

                drawRect(brush = ambientGlow)
                drawPath(path = bodyFillPath, brush = silhouetteFill)
                drawPath(path = leftHoodFacet, color = Color(0xFF151E28).copy(alpha = 0.62f))
                drawPath(path = rightHoodFacet, color = Color(0xFF0C1117).copy(alpha = 0.75f))

                // Soft outer blue glow + sharp rim highlight along the shoulder/hood contour
                drawPath(
                    path = outlinePath,
                    color = SplashSilhouetteRim.copy(alpha = 0.28f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = outlinePath,
                    color = SplashSilhouetteRim.copy(alpha = 0.85f),
                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Minimalist vertical center zipper seam
                drawLine(
                    color = PureWhite.copy(alpha = 0.08f),
                    start = Offset(w * 0.5f, h * 0.76f),
                    end = Offset(w * 0.5f, h),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
        }
    )
}

@Composable
fun OnboardingWeatherAlertPage(
    onSkip: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OnboardingDarkCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onSkip),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.12f))
                ) {
                    Text(
                        text = "Lewati",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFFB5BEC4),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            LockscreenWeatherNotificationMockup()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Peringatan Cuaca",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 27.sp,
                lineHeight = 33.sp,
                letterSpacing = (-0.5).sp,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Dapatkan briefing outfit otomatis setiap pagi sebelum cuaca berubah tiba-tiba.",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = OnboardingDarkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            OnboardingStepIndicator(activeIndex = 1)
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = onNext,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoltLime,
                    contentColor = ObsidianDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Lanjut",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LockscreenWeatherNotificationMockup() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Dark lockscreen phone silhouette
        Box(
            modifier = Modifier
                .width(248.dp)
                .height(232.dp)
                .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF394148),
                            Color(0xFF212D34),
                            Color(0xFF151C20)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PureWhite.copy(alpha = 0.24f),
                            PureWhite.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dynamic Island pill
                Row(
                    modifier = Modifier
                        .width(82.dp)
                        .height(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF090B0C))
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A2229))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16273B))
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Jumat, 27 Nov",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = PureWhite.copy(alpha = 0.80f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "06:30",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 54.sp,
                    lineHeight = 58.sp,
                    letterSpacing = (-1.5).sp,
                    color = PureWhite
                )
            }
        }

        // Frosted-glass notification card overlapping the lockscreen with Electric Cobalt glow
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF171C21).copy(alpha = 0.94f),
            border = BorderStroke(1.3.dp, ElectricCobalt),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .fillMaxWidth()
                .drawBehind {
                    val corner = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                    drawRoundRect(
                        color = ElectricCobalt.copy(alpha = 0.22f),
                        topLeft = Offset(-4.dp.toPx(), 2.dp.toPx()),
                        size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                        cornerRadius = corner
                    )
                    drawRoundRect(
                        color = ElectricCobalt.copy(alpha = 0.10f),
                        topLeft = Offset(-10.dp.toPx(), 6.dp.toPx()),
                        size = Size(size.width + 20.dp.toPx(), size.height + 16.dp.toPx()),
                        cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
                    )
                }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1114))
                            .border(1.dp, PureWhite.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Checkroom,
                            contentDescription = null,
                            tint = VoltLime,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(ElectricCobalt)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FitCast Pagi",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Baru saja",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF939EA5)
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = PureWhite, fontWeight = FontWeight.Bold)) {
                            append("Bandung 24°C")
                        }
                        withStyle(SpanStyle(color = Color(0xFFD0D8DE), fontWeight = FontWeight.Normal)) {
                            append(", peluang hujan 60%. Saran: Kaos Katun + Jaket Parasut. Jangan lupa bawa payung!")
                        }
                    },
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingSmartWardrobePage(
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageNote by remember { mutableStateOf(false) }

    LaunchedEffect(showLanguageNote) {
        if (showLanguageNote) {
            delay(2000L)
            showLanguageNote = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OnboardingDarkCanvas)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF172638).copy(alpha = 0.72f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.24f),
                        radius = size.width * 0.85f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(VoltLime.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.52f),
                        radius = size.width * 0.72f
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = showLanguageNote,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180))
                ) {
                    Text(
                        text = "Bahasa Indonesia",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = VoltLime,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Bahasa aplikasi Indonesia",
                            onClick = { showLanguageNote = !showLanguageNote }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PureWhite.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = PureWhite.copy(alpha = 0.82f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ID",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PureWhite
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = PureWhite.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                SmartWardrobeCollage()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lemari Digital Pintar",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 27.sp,
                    lineHeight = 33.sp,
                    letterSpacing = (-0.5).sp,
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Foto bajumu sekali, AI menghapus latar belakang dan meracik kombinasi terbaik untukmu.",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = OnboardingDarkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                OnboardingStepIndicator(activeIndex = 2)
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = onStart,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = ObsidianDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = VoltLime.copy(alpha = 0.22f),
                                topLeft = Offset(8.dp.toPx(), 10.dp.toPx()),
                                size = Size(size.width - 16.dp.toPx(), size.height),
                                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                            )
                        }
                ) {
                    Text(
                        text = "Mulai FitCast",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartWardrobeCollage() {
    val infiniteTransition = rememberInfiniteTransition(label = "wardrobeCollageFloat")
    val floatPrimary by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatPrimary"
    )
    val floatSecondary by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatSecondary"
    )
    val floatCenter by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatCenter"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(356.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. TOP - Crewneck Tee (top-left)
        FloatingGarmentCard(
            category = "TOP",
            categoryColor = Color(0xFF8C9793),
            title = "Crewneck Tee",
            icon = Icons.Outlined.DryCleaning,
            iconTint = Color(0xFFB3BCB8),
            trailingDotColor = Color(0xFF7B8581),
            width = 114.dp,
            height = 116.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 6.dp, y = 8.dp)
                .graphicsLayer {
                    rotationZ = -7f
                    translationY = floatPrimary
                }
        )

        // 2. OUTER - Windbreaker (top-right)
        FloatingGarmentCard(
            category = "OUTER",
            categoryColor = TealAccent,
            title = "Windbreaker",
            icon = Icons.Outlined.Checkroom,
            iconTint = TealAccent.copy(alpha = 0.85f),
            trailingIcon = Icons.Outlined.WaterDrop,
            trailingIconTint = TealAccent,
            width = 118.dp,
            height = 118.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 12.dp)
                .graphicsLayer {
                    rotationZ = 7f
                    translationY = floatSecondary
                }
        )

        // 3. Palette swatch card (mid-left)
        FloatingPaletteCard(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 0.dp, y = 34.dp)
                .graphicsLayer {
                    rotationZ = -6f
                    translationY = floatSecondary * 0.8f
                }
        )

        // 4. BOTTOM - Beige Chino (bottom-left)
        FloatingTrousersCard(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 24.dp, y = (-12).dp)
                .graphicsLayer {
                    rotationZ = -4f
                    translationY = floatPrimary * 0.85f
                }
        )

        // 5. SHOES - Court Low 01 (bottom-right)
        FloatingSneakerCard(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-14).dp)
                .graphicsLayer {
                    rotationZ = 5f
                    translationY = floatSecondary * 0.9f
                }
        )

        // Focal center recommendation card
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF1B2024),
            shadowElevation = 18.dp,
            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.12f)),
            modifier = Modifier
                .width(170.dp)
                .height(210.dp)
                .graphicsLayer {
                    translationY = floatCenter
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "24°",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            lineHeight = 26.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "C",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = VoltLime,
                            modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(VoltLime.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = null,
                            tint = VoltLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252C29)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Style,
                        contentDescription = null,
                        tint = VoltLime,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Fit Rekomendasi",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PureWhite,
                    maxLines = 1
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF131719),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f))
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
                                .background(VoltLime)
                        )
                        Text(
                            text = "Harmoni Netral",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = VoltLime,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingGarmentCard(
    category: String,
    categoryColor: Color,
    title: String,
    icon: ImageVector,
    iconTint: Color,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    trailingDotColor: Color? = null,
    trailingIcon: ImageVector? = null,
    trailingIconTint: Color = categoryColor
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = OnboardingCardFill.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.6.sp,
                    color = categoryColor
                )
                when {
                    trailingIcon != null -> Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = trailingIconTint,
                        modifier = Modifier.size(13.dp)
                    )

                    trailingDotColor != null -> Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(trailingDotColor)
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = PureWhite.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FloatingPaletteCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = OnboardingCardFill.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
        modifier = modifier
            .width(96.dp)
            .height(102.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Palette",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = Color(0xFF919B97)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7FA942))
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D4ED8))
                    )
                }
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawArc(
                        color = Color(0xFF9EA5A2),
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = size
                    )
                    drawArc(
                        color = Color(0xFF2D3331),
                        startAngle = -90f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = size
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingTrousersCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = OnboardingCardFill.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
        modifier = modifier
            .width(120.dp)
            .height(104.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BOTTOM",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.6.sp,
                color = Color(0xFF8C9793)
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(width = 34.dp, height = 22.dp)) {
                    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    drawRoundRect(
                        color = SandBeige,
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = stroke
                    )
                    val tickTop = 0f
                    val tickBottom = size.height * 0.42f
                    drawLine(SandBeige, Offset(size.width * 0.30f, tickTop), Offset(size.width * 0.30f, tickBottom), 1.8.dp.toPx())
                    drawLine(SandBeige, Offset(size.width * 0.50f, tickTop), Offset(size.width * 0.50f, tickBottom * 1.15f), 1.8.dp.toPx())
                    drawLine(SandBeige, Offset(size.width * 0.70f, tickTop), Offset(size.width * 0.70f, tickBottom), 1.8.dp.toPx())
                }
            }
            Text(
                text = "Beige Chino",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = PureWhite.copy(alpha = 0.78f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FloatingSneakerCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = OnboardingCardFill.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
        modifier = modifier
            .width(116.dp)
            .height(106.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SHOES",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.6.sp,
                    color = Color(0xFF8C9793)
                )
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF8C9793),
                    modifier = Modifier.size(13.dp)
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(width = 34.dp, height = 24.dp)) {
                    val w = size.width
                    val h = size.height
                    val shoePath = Path().apply {
                        moveTo(w * 0.12f, h * 0.25f)
                        lineTo(w * 0.34f, h * 0.18f)
                        lineTo(w * 0.56f, h * 0.50f)
                        lineTo(w * 0.86f, h * 0.58f)
                        arcTo(
                            rect = Rect(w * 0.72f, h * 0.58f, w * 0.94f, h * 0.88f),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                        lineTo(w * 0.14f, h * 0.88f)
                        lineTo(w * 0.12f, h * 0.25f)
                        close()
                    }
                    drawPath(
                        path = shoePath,
                        color = Color(0xFFC7CFD3),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
            Text(
                text = "Court Low 01",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = PureWhite.copy(alpha = 0.78f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OnboardingStepIndicator(
    activeIndex: Int,
    totalSteps: Int = 3
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == activeIndex
            val width by animateDpAsState(
                targetValue = if (isActive) 26.dp else 7.dp,
                animationSpec = tween(durationMillis = 220),
                label = "stepDotWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isActive) VoltLime else Color(0xFF39413E),
                animationSpec = tween(durationMillis = 220),
                label = "stepDotColor"
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val privacyPolicyUrl = "https://github.com/fitcast-privacy-policy"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tentang FitCast",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianDark,
                    titleContentColor = PureWhite,
                    navigationIconContentColor = PureWhite
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCardDark,
                contentColor = PureWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FITCAST (CUACAGAYA) v1.0",
                        style = MaterialTheme.typography.titleMedium,
                        color = VoltLime
                    )
                    Text(
                        text = "Asisten lemari digital pintar berbasis cuaca harian. Seluruh foto pakaianmu disimpan 100% secara lokal di perangkat dan tidak pernah diunggah ke server eksternal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite.copy(alpha = 0.86f)
                    )
                }
            }
            HorizontalDivider(color = FitCastTheme.colors.subtleBorder)
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, privacyPolicyUrl.toUri()))
                },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Outlined.PrivacyTip, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Kebijakan Privasi (Privacy Policy)")
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1622, widthDp = 390, heightDp = 844)
@Composable
private fun SplashLaunchScreenPreview() {
    FitCastTheme(darkTheme = true) {
        SplashLaunchScreen(onComplete = {}, autoAdvance = false)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121415, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingWeatherAlertPagePreview() {
    FitCastTheme(darkTheme = true) {
        OnboardingWeatherAlertPage(onSkip = {}, onNext = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121415, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingSmartWardrobePagePreview() {
    FitCastTheme(darkTheme = true) {
        OnboardingSmartWardrobePage(onStart = {})
    }
}
