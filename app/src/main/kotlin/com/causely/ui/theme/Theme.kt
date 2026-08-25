package com.causely.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CauselyDarkColorScheme = darkColorScheme(
    primary = CauselyViolet,
    onPrimary = Color.White,
    primaryContainer = CauselyVioletDim,
    onPrimaryContainer = CauselyVioletBright,

    secondary = CaselyCyan,
    onSecondary = BackgroundDeep,
    secondaryContainer = CaselyCyanDim,
    onSecondaryContainer = CaselyCyan,

    tertiary = CauselyGreen,
    onTertiary = BackgroundDeep,
    tertiaryContainer = CauselyGreenDim,
    onTertiaryContainer = CauselyGreen,

    background = BackgroundDeep,
    onBackground = TextPrimary,

    surface = SurfaceBase,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,

    outline = BorderSubtle,
    outlineVariant = BorderBright,

    error = CauselyRed,
    onError = Color.White
)

/**
 * Causely Material3 dark theme.
 * Applied at the root of the Compose hierarchy.
 */
@Composable
fun CauselyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CauselyDarkColorScheme,
        typography = CauselyTypography,
        content = content
    )
}
