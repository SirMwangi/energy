package com.example.smartmoney.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * CompositionLocal indicating whether the app is currently displaying in dark theme.
 */
val LocalDarkTheme = compositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = SmartMoneyColors.DarkActiveCyan,
    onPrimary = SmartMoneyColors.DarkBackground,
    primaryContainer = SmartMoneyColors.DarkSurface,
    onPrimaryContainer = SmartMoneyColors.DarkActiveCyan,
    secondary = SmartMoneyColors.AzurePrimary,
    onSecondary = Color.White,
    background = SmartMoneyColors.DarkBackground,
    onBackground = SmartMoneyColors.DarkTextPrimary,
    surface = SmartMoneyColors.DarkSurface,
    onSurface = SmartMoneyColors.DarkTextPrimary,
    surfaceVariant = SmartMoneyColors.DeepNavy,
    onSurfaceVariant = SmartMoneyColors.DarkInactive,
    outline = SmartMoneyColors.DarkBorderLine,
    outlineVariant = SmartMoneyColors.DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = SmartMoneyColors.DarkSlateGreen,          // #657166: Dark Slate Green
    onPrimary = Color.White,
    primaryContainer = SmartMoneyColors.PaleMintGreen,  // #DAEBE3: Pale Mint Green
    onPrimaryContainer = SmartMoneyColors.DarkSlateGreen,
    secondary = SmartMoneyColors.PowderBlue,            // #99CDD8: Powder Blue
    onSecondary = SmartMoneyColors.DarkSlateGreen,
    secondaryContainer = SmartMoneyColors.LightPeach,   // #FDE8D3: Light Peach / Cream
    onSecondaryContainer = SmartMoneyColors.DarkSlateGreen,
    tertiary = SmartMoneyColors.LightSalmon,            // #F3C3B2: Light Salmon / Melon Pink
    onTertiary = Color.White,
    tertiaryContainer = SmartMoneyColors.LightSalmon.copy(alpha = 0.35f),
    onTertiaryContainer = Color(0xFF7A271A),
    background = SmartMoneyColors.SlateBackground,      // #FAF9F6: Soft warm canvas
    onBackground = SmartMoneyColors.TextPrimary,
    surface = Color.White,
    onSurface = SmartMoneyColors.TextPrimary,
    surfaceVariant = SmartMoneyColors.PaleSageGreen.copy(alpha = 0.35f), // #CFD6C4
    onSurfaceVariant = SmartMoneyColors.DarkSlateGreen,
    outline = SmartMoneyColors.PaleSageGreen,           // #CFD6C4: Pale Sage Green
    outlineVariant = SmartMoneyColors.PaleSageGreen.copy(alpha = 0.5f)
)

@Composable
fun EnergyTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}