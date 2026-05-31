package com.ccs.thaparbites.ui.theme

import com.ccs.thaparbites.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────
// FONT FAMILY
//
// Using Poppins — its rounded, confident geometry matches the
// logo's letterforms. Add these files to res/font/:
//
//   res/font/poppins_regular.ttf     (weight 400)
//   res/font/poppins_medium.ttf      (weight 500)
//   res/font/poppins_semibold.ttf    (weight 600)
//   res/font/poppins_bold.ttf        (weight 700)
//
// Download from: https://fonts.google.com/specimen/Poppins
//
// To use Google Fonts at runtime instead of bundled TTFs,
// replace the FontFamily block with:
//
//   val provider = GoogleFont.Provider(
//       providerAuthority = "com.google.android.gms.fonts",
//       providerPackage   = "com.google.android.gms",
//       certificates      = R.array.com_google_android_gms_fonts_certs,
//   )
//   val poppinsFont = GoogleFont("Poppins")
//   val Poppins = FontFamily(
//       Font(googleFont = poppinsFont, fontProvider = provider, weight = FontWeight.Normal),
//       Font(googleFont = poppinsFont, fontProvider = provider, weight = FontWeight.Medium),
//       Font(googleFont = poppinsFont, fontProvider = provider, weight = FontWeight.SemiBold),
//       Font(googleFont = poppinsFont, fontProvider = provider, weight = FontWeight.Bold),
//   )
// ─────────────────────────────────────────────────────────────

val Poppins = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

// ─────────────────────────────────────────────────────────────
// TYPE SCALE
//
// Mapped to Material 3 roles with Thapar Bites usage notes.
// Reference: https://m3.material.io/styles/typography/type-scale-tokens
// ─────────────────────────────────────────────────────────────

val ThaparBitesTypography = Typography(

    // ── Display ──────────────────────────────────────────────
    // Hero / splash screens, large promotional banners
    displayLarge = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Bold,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Bold,
        fontSize      = 45.sp,
        lineHeight    = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = 0.sp,
    ),

    // ── Headline ─────────────────────────────────────────────
    // Screen titles, section headers (e.g. "Choose your canteen")
    headlineLarge = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),

    // ── Title ────────────────────────────────────────────────
    // TopAppBar title, card shop names, dialog headings
    titleLarge = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    ),
    // Food item names in menu list
    titleMedium = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    // Sub-items, modifier headings
    titleSmall = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body ─────────────────────────────────────────────────
    // Descriptions, order details, terms
    bodyLarge = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    // Item descriptions, hostel name, secondary info
    bodyMedium = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    // Timestamps, metadata, fine print
    bodySmall = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // ── Label ────────────────────────────────────────────────
    // Buttons, chips, status badges, bottom nav labels
    labelLarge = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Chip text, filter tags, price tags
    labelMedium = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    // Overlines, tiny status indicators, ETA badge
    labelSmall = TextStyle(
        fontFamily    = Poppins,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)