package com.example.ecofruit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary          = EcoMoss,
    onPrimary        = NeutralWhite,
    primaryContainer = EcoMint,
    onPrimaryContainer = EcoForest,

    secondary        = EcoLeaf,
    onSecondary      = NeutralWhite,
    secondaryContainer = EcoFoam,
    onSecondaryContainer = EcoMoss,

    tertiary         = EarthClay,
    onTertiary       = NeutralWhite,
    tertiaryContainer = EarthCream,
    onTertiaryContainer = EarthSoil,

    background       = EcoFoam,
    onBackground     = NeutralDark,

    surface          = NeutralWhite,
    onSurface        = NeutralDark,
    surfaceVariant   = EcoFoam,
    onSurfaceVariant = NeutralMid,

    outline          = EcoMint,
    outlineVariant   = NeutralLight,

    error            = FruitTomato,
    onError          = NeutralWhite,
    errorContainer   = Color(0xFFFFF0EC),
    onErrorContainer = FruitTomato,

    inverseSurface   = EcoForest,
    inverseOnSurface = EcoFoam,
    inversePrimary   = EcoSpring,
)

// ── Dark scheme ──
private val DarkColorScheme = darkColorScheme(
    primary          = EcoSpring,
    onPrimary        = EcoForest,
    primaryContainer = EcoMoss,
    onPrimaryContainer = EcoMint,

    secondary        = EcoMint,
    onSecondary      = EcoForest,
    secondaryContainer = Color(0xFF1E4D34),
    onSecondaryContainer = EcoMint,

    tertiary         = EarthSand,
    onTertiary       = EarthSoil,
    tertiaryContainer = Color(0xFF4A3020),
    onTertiaryContainer = EarthSand,

    background       = NeutralDark,
    onBackground     = NeutralWhite,

    surface          = Color(0xFF1A2B1E),
    onSurface        = NeutralWhite,
    surfaceVariant   = Color(0xFF243528),
    onSurfaceVariant = NeutralLight,

    outline          = NeutralMid,
    outlineVariant   = Color(0xFF2E4434),

    error            = FruitTomato,
    onError          = NeutralWhite,
    errorContainer   = Color(0xFF5C1E0A),
    onErrorContainer = Color(0xFFFFB4A0),

    inverseSurface   = EcoFoam,
    inverseOnSurface = NeutralDark,
    inversePrimary   = EcoMoss,
)

@Composable
fun EcoFruitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}