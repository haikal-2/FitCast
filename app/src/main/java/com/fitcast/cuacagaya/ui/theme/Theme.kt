package com.fitcast.cuacagaya.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class FitCastExtendedColors(
    val obsidianChrome: Color,
    val darkCardSurface: Color,
    val voltLime: Color,
    val onVoltLime: Color,
    val electricCobalt: Color,
    val onElectricCobalt: Color,
    val softIceTint: Color,
    val laundryRed: Color,
    val softDangerTint: Color,
    val garmentBackdrop: Color,
    val mutedText: Color,
    val subtleBorder: Color,
    val successGreen: Color
)

private val LightExtendedColors = FitCastExtendedColors(
    obsidianChrome = ObsidianDark,
    darkCardSurface = SurfaceCardDark,
    voltLime = VoltLime,
    onVoltLime = ObsidianDark,
    electricCobalt = ElectricCobalt,
    onElectricCobalt = PureWhite,
    softIceTint = SoftIceTint,
    laundryRed = LaundryRed,
    softDangerTint = SoftDangerTint,
    garmentBackdrop = CardNeutralBackdrop,
    mutedText = TextMutedLight,
    subtleBorder = BorderLight,
    successGreen = SuccessGreen
)

private val DarkExtendedColors = FitCastExtendedColors(
    obsidianChrome = ObsidianDark,
    darkCardSurface = SurfaceCardDark,
    voltLime = VoltLime,
    onVoltLime = ObsidianDark,
    electricCobalt = ElectricCobalt,
    onElectricCobalt = PureWhite,
    softIceTint = SoftIceTintDark,
    laundryRed = LaundryRed,
    softDangerTint = SoftDangerTintDark,
    garmentBackdrop = CardNeutralBackdropDark,
    mutedText = TextMutedDark,
    subtleBorder = BorderDark,
    successGreen = SuccessGreenDark
)

val LocalFitCastColors = staticCompositionLocalOf { LightExtendedColors }

private val FitCastLightColorScheme = lightColorScheme(
    primary = ElectricCobalt,
    onPrimary = PureWhite,
    primaryContainer = SoftIceTint,
    onPrimaryContainer = ObsidianDark,
    secondary = VoltLime,
    onSecondary = ObsidianDark,
    secondaryContainer = ObsidianDark,
    onSecondaryContainer = VoltLime,
    tertiary = GraphiteSlate,
    onTertiary = PureWhite,
    background = CanvasLight,
    onBackground = TextPrimaryLight,
    surface = PureWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardNeutralBackdrop,
    onSurfaceVariant = TextMutedLight,
    outline = BorderLight,
    outlineVariant = BorderLight,
    error = LaundryRed,
    onError = PureWhite,
    errorContainer = SoftDangerTint,
    onErrorContainer = LaundryRed
)

private val FitCastDarkColorScheme = darkColorScheme(
    primary = VoltLime,
    onPrimary = ObsidianDark,
    primaryContainer = SoftIceTintDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = ElectricCobalt,
    onSecondary = PureWhite,
    secondaryContainer = SurfaceCardDark,
    onSecondaryContainer = VoltLime,
    tertiary = GraphiteSlate,
    onTertiary = TextPrimaryDark,
    background = ObsidianDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = GraphiteSlate,
    onSurfaceVariant = TextMutedDark,
    outline = BorderDark,
    outlineVariant = BorderDark,
    error = LaundryRed,
    onError = PureWhite,
    errorContainer = SoftDangerTintDark,
    onErrorContainer = Color(0xFFFCA5A5)
)

object FitCastTheme {
    val colors: FitCastExtendedColors
        @Composable get() = LocalFitCastColors.current
}

@Composable
fun FitCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FitCastDarkColorScheme
        else -> FitCastLightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalFitCastColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FitCastTypography,
            content = content
        )
    }
}
