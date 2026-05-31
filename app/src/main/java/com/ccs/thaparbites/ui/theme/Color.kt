package com.ccs.thaparbites.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// PRIMITIVE RAMPS
// Derived from the Thapar Bites logo:
//   • Crimson  → the bold red "ti" lettermark
//   • Charcoal → the dark wordmark + campus silhouette
//   • Gold     → warm accent, delivery dome highlight
// ─────────────────────────────────────────────────────────────

// Crimson
val Crimson50  = Color(0xFFFFF0F0)
val Crimson100 = Color(0xFFFFD6D6)
val Crimson200 = Color(0xFFFFAAAA)
val Crimson300 = Color(0xFFFF7070)
val Crimson400 = Color(0xFFE03030)
val Crimson500 = Color(0xFFC0000C)   // ★ Primary brand
val Crimson600 = Color(0xFF9B0009)
val Crimson700 = Color(0xFF7A0007)
val Crimson800 = Color(0xFF5A0005)
val Crimson900 = Color(0xFF3A0003)

// Charcoal
val Charcoal50  = Color(0xFFF5F5F5)
val Charcoal100 = Color(0xFFE8E8E8)
val Charcoal200 = Color(0xFFCCCCCC)
val Charcoal300 = Color(0xFFAAAAAA)
val Charcoal400 = Color(0xFF888888)
val Charcoal500 = Color(0xFF6A6A6A)
val Charcoal600 = Color(0xFF555555)
val Charcoal700 = Color(0xFF3A3A3A)
val Charcoal800 = Color(0xFF282828)
val Charcoal900 = Color(0xFF181818)

// Gold
val Gold50  = Color(0xFFFFF4D6)
val Gold100 = Color(0xFFFFE59A)
val Gold200 = Color(0xFFF5C842)
val Gold300 = Color(0xFFE8AD1A)
val Gold400 = Color(0xFFD4960A)   // ★ Secondary accent
val Gold500 = Color(0xFFB87C08)
val Gold600 = Color(0xFFA37008)
val Gold700 = Color(0xFF7A5205)
val Gold800 = Color(0xFF543803)
val Gold900 = Color(0xFF342201)

// Utility
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Transparent = Color(0x00000000)

// ─────────────────────────────────────────────────────────────
// SEMANTIC TOKENS  (used directly in Theme.kt color schemes)
// ─────────────────────────────────────────────────────────────

// — Light scheme tokens —
val md_theme_light_primary                = Crimson500
val md_theme_light_onPrimary              = White
val md_theme_light_primaryContainer       = Crimson100
val md_theme_light_onPrimaryContainer     = Crimson800

val md_theme_light_secondary              = Gold400
val md_theme_light_onSecondary            = White
val md_theme_light_secondaryContainer     = Gold50
val md_theme_light_onSecondaryContainer   = Gold800

val md_theme_light_tertiary               = Charcoal700
val md_theme_light_onTertiary             = White
val md_theme_light_tertiaryContainer      = Charcoal100
val md_theme_light_onTertiaryContainer    = Charcoal900

val md_theme_light_error                  = Crimson400
val md_theme_light_onError                = White
val md_theme_light_errorContainer         = Crimson50
val md_theme_light_onErrorContainer       = Crimson800

val md_theme_light_background             = White
val md_theme_light_onBackground           = Charcoal900

val md_theme_light_surface                = Charcoal50
val md_theme_light_onSurface              = Charcoal900
val md_theme_light_surfaceVariant         = Charcoal100
val md_theme_light_onSurfaceVariant       = Charcoal600

val md_theme_light_outline                = Charcoal200
val md_theme_light_outlineVariant         = Charcoal100

val md_theme_light_inverseSurface         = Charcoal900
val md_theme_light_inverseOnSurface       = Charcoal100
val md_theme_light_inversePrimary         = Crimson200

val md_theme_light_scrim                  = Color(0x80000000)

// — Dark scheme tokens —
val md_theme_dark_primary                 = Crimson300
val md_theme_dark_onPrimary               = Crimson900
val md_theme_dark_primaryContainer        = Crimson700
val md_theme_dark_onPrimaryContainer      = Crimson100

val md_theme_dark_secondary               = Gold200
val md_theme_dark_onSecondary             = Gold900
val md_theme_dark_secondaryContainer      = Gold700
val md_theme_dark_onSecondaryContainer    = Gold50

val md_theme_dark_tertiary                = Charcoal300
val md_theme_dark_onTertiary              = Charcoal900
val md_theme_dark_tertiaryContainer       = Charcoal700
val md_theme_dark_onTertiaryContainer     = Charcoal100

val md_theme_dark_error                   = Crimson300
val md_theme_dark_onError                 = Crimson900
val md_theme_dark_errorContainer          = Crimson800
val md_theme_dark_onErrorContainer        = Crimson100

val md_theme_dark_background              = Color(0xFF111111)
val md_theme_dark_onBackground            = Charcoal50

val md_theme_dark_surface                 = Color(0xFF1A1A1A)
val md_theme_dark_onSurface               = Charcoal100
val md_theme_dark_surfaceVariant          = Color(0xFF252525)
val md_theme_dark_onSurfaceVariant        = Charcoal400

val md_theme_dark_outline                 = Charcoal700
val md_theme_dark_outlineVariant          = Charcoal800

val md_theme_dark_inverseSurface          = Charcoal100
val md_theme_dark_inverseOnSurface        = Charcoal900
val md_theme_dark_inversePrimary          = Crimson600

val md_theme_dark_scrim                   = Color(0x80000000)

// ─────────────────────────────────────────────────────────────
// APP-SPECIFIC SEMANTIC COLORS
// (status, badges — referenced in ExtendedColors.kt)
// ─────────────────────────────────────────────────────────────

// Store open/closed/busy
val StoreOpenGreen          = Color(0xFF1DA462)
val StoreOpenGreenContainer = Color(0xFFD4F5E4)
val StoreClosedRed          = Crimson400
val StoreClosedRedContainer = Crimson50
val StoreBusyAmber          = Gold400
val StoreBusyAmberContainer = Gold50

// Dark-mode variants
val StoreOpenGreenDark          = Color(0xFF34C67A)
val StoreOpenGreenContainerDark = Color(0xFF0A3D22)
val StoreClosedRedDark          = Crimson300
val StoreClosedRedContainerDark = Crimson800
val StoreBusyAmberDark          = Gold200
val StoreBusyAmberContainerDark = Gold800