package dev.anilbeesetti.nextplayer.core.ui.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.anilbeesetti.nextplayer.core.model.BackgroundConfig
import dev.anilbeesetti.nextplayer.core.model.BackgroundType
import dev.anilbeesetti.nextplayer.core.model.ThemePalette

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun NextPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrastDarkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    palette: ThemePalette = ThemePalette.CINEMA,
    background: BackgroundConfig = BackgroundConfig(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && supportsDynamicTheming() -> {
            val context = LocalContext.current
            when {
                darkTheme && highContrastDarkTheme -> dynamicDarkColorScheme(
                    context = context,
                ).copy(
                    background = backgroundPureBlack,
                    surface = surfacePureBlack,
                    surfaceDim = surfaceDimPureBlack,
                    surfaceBright = surfaceBrightPureBlack,
                    surfaceContainerLowest = surfaceContainerLowestPureBlack,
                    surfaceContainerLow = surfaceContainerLowPureBlack,
                    surfaceContainer = surfaceContainerPureBlack,
                    surfaceContainerHigh = surfaceContainerHighPureBlack,
                    surfaceContainerHighest = surfaceContainerHighestPureBlack,
                )

                darkTheme -> dynamicDarkColorScheme(context = context)

                else -> dynamicLightColorScheme(context = context)
            }
        }

        darkTheme && highContrastDarkTheme -> darkScheme.copy(
            background = backgroundPureBlack,
            surface = surfacePureBlack,
            surfaceDim = surfaceDimPureBlack,
            surfaceBright = surfaceBrightPureBlack,
            surfaceContainerLowest = surfaceContainerLowestPureBlack,
            surfaceContainerLow = surfaceContainerLowPureBlack,
            surfaceContainer = surfaceContainerPureBlack,
            surfaceContainerHigh = surfaceContainerHighPureBlack,
            surfaceContainerHighest = surfaceContainerHighestPureBlack,
        )

        else -> palette.scheme(isDark = darkTheme)
    }.withImageBackground(background = background)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicTheming() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * With a picture behind the app, surfaces become a light translucent veil: the picture stays
 * fully visible (never blurred, never dimmed) while text still reads on top of it.
 */
private fun ColorScheme.withImageBackground(background: BackgroundConfig): ColorScheme =
    if (background.type != BackgroundType.IMAGE) {
        this
    } else {
        copy(
            // Page-level surfaces are fully see-through so the picture is untouched; only cards
            // keep a thin translucent base for text legibility.
            background = this.background.copy(alpha = 0f),
            surface = this.surface.copy(alpha = 0f),
            surfaceDim = this.surfaceDim.copy(alpha = 0f),
            surfaceBright = this.surfaceBright.copy(alpha = 0f),
            surfaceContainerLowest = this.surfaceContainerLowest.copy(alpha = 0.45f),
            surfaceContainerLow = this.surfaceContainerLow.copy(alpha = 0.38f),
            surfaceContainer = this.surfaceContainer.copy(alpha = 0.18f),
            surfaceContainerHigh = this.surfaceContainerHigh.copy(alpha = 0.40f),
            surfaceContainerHighest = this.surfaceContainerHighest.copy(alpha = 0.55f),
        )
    }
