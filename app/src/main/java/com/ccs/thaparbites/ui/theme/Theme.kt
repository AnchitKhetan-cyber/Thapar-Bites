package com.ccs.thaparbites.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────
// MATERIAL 3 COLOR SCHEMES
// ─────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary                = md_theme_light_primary,
    onPrimary              = md_theme_light_onPrimary,
    primaryContainer       = md_theme_light_primaryContainer,
    onPrimaryContainer     = md_theme_light_onPrimaryContainer,

    secondary              = md_theme_light_secondary,
    onSecondary            = md_theme_light_onSecondary,
    secondaryContainer     = md_theme_light_secondaryContainer,
    onSecondaryContainer   = md_theme_light_onSecondaryContainer,

    tertiary               = md_theme_light_tertiary,
    onTertiary             = md_theme_light_onTertiary,
    tertiaryContainer      = md_theme_light_tertiaryContainer,
    onTertiaryContainer    = md_theme_light_onTertiaryContainer,

    error                  = md_theme_light_error,
    onError                = md_theme_light_onError,
    errorContainer         = md_theme_light_errorContainer,
    onErrorContainer       = md_theme_light_onErrorContainer,

    background             = md_theme_light_background,
    onBackground           = md_theme_light_onBackground,

    surface                = md_theme_light_surface,
    onSurface              = md_theme_light_onSurface,
    surfaceVariant         = md_theme_light_surfaceVariant,
    onSurfaceVariant       = md_theme_light_onSurfaceVariant,

    outline                = md_theme_light_outline,
    outlineVariant         = md_theme_light_outlineVariant,

    inverseSurface         = md_theme_light_inverseSurface,
    inverseOnSurface       = md_theme_light_inverseOnSurface,
    inversePrimary         = md_theme_light_inversePrimary,

    scrim                  = md_theme_light_scrim,
)

private val DarkColorScheme = darkColorScheme(
    primary                = md_theme_dark_primary,
    onPrimary              = md_theme_dark_onPrimary,
    primaryContainer       = md_theme_dark_primaryContainer,
    onPrimaryContainer     = md_theme_dark_onPrimaryContainer,

    secondary              = md_theme_dark_secondary,
    onSecondary            = md_theme_dark_onSecondary,
    secondaryContainer     = md_theme_dark_secondaryContainer,
    onSecondaryContainer   = md_theme_dark_onSecondaryContainer,

    tertiary               = md_theme_dark_tertiary,
    onTertiary             = md_theme_dark_onTertiary,
    tertiaryContainer      = md_theme_dark_tertiaryContainer,
    onTertiaryContainer    = md_theme_dark_onTertiaryContainer,

    error                  = md_theme_dark_error,
    onError                = md_theme_dark_onError,
    errorContainer         = md_theme_dark_errorContainer,
    onErrorContainer       = md_theme_dark_onErrorContainer,

    background             = md_theme_dark_background,
    onBackground           = md_theme_dark_onBackground,

    surface                = md_theme_dark_surface,
    onSurface              = md_theme_dark_onSurface,
    surfaceVariant         = md_theme_dark_surfaceVariant,
    onSurfaceVariant       = md_theme_dark_onSurfaceVariant,

    outline                = md_theme_dark_outline,
    outlineVariant         = md_theme_dark_outlineVariant,

    inverseSurface         = md_theme_dark_inverseSurface,
    inverseOnSurface       = md_theme_dark_inverseOnSurface,
    inversePrimary         = md_theme_dark_inversePrimary,

    scrim                  = md_theme_dark_scrim,
)

// ─────────────────────────────────────────────────────────────
// ROOT THEME COMPOSABLE
//
// Usage — wrap your NavHost / root screen:
//
//   ThaparBitesTheme {
//       MainNavGraph()
//   }
//
// Parameters:
//   darkTheme       — defaults to system setting; override for
//                     previews or in-app toggle
//   dynamicColor    — Material You dynamic color (Android 12+).
//                     Disabled by default to preserve brand colors.
//                     Enable only if you want users' wallpaper
//                     colors to override the crimson brand.
// ─────────────────────────────────────────────────────────────

@Composable
fun ThaparBitesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // keep false — preserve brand crimson
    content: @Composable () -> Unit,
) {
    // Resolve color scheme
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Resolve extended colors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // Tint the status bar and navigation bar to match the top bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Status bar — crimson on both themes to match TopAppBar
            window.statusBarColor = extendedColors.topBarBackground.toArgb()

            // Navigation bar — match screen background
            window.navigationBarColor = colorScheme.background.toArgb()

            // Icon brightness: light icons on dark status bar
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false  // white icons on crimson bar
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = ThaparBitesTypography,
            shapes      = ThaparBitesShapes,
            content     = content,
        )
    }
}

// ─────────────────────────────────────────────────────────────
// CONVENIENCE EXTENSION ON MaterialTheme
//
// Lets you access extended colors the same way as M3 tokens:
//
//   MaterialTheme.colorScheme.primary       ← standard M3
//   MaterialTheme.extendedColors.storeOpen  ← Thapar Bites
//   MaterialTheme.extendedColors.fabBackground
// ─────────────────────────────────────────────────────────────

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current