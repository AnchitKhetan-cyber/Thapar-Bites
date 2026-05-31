package com.ccs.thaparbites.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// EXTENDED COLORS
//
// Material 3's ColorScheme covers generic UI. These tokens cover
// Thapar Bites' domain-specific color needs — store status,
// ETA badges, cart, payment method indicators, etc.
//
// Access in any composable:
//   val ext = LocalExtendedColors.current
//   Text(color = ext.storeOpen)
// ─────────────────────────────────────────────────────────────

@Immutable
data class ExtendedColors(

    // ── Store status ─────────────────────────────────────────
    /** Text / icon color for open stores */
    val storeOpen: Color,
    /** Background chip color for open stores */
    val storeOpenContainer: Color,
    /** Text / icon color for closed stores */
    val storeClosed: Color,
    /** Background chip color for closed stores */
    val storeClosedContainer: Color,
    /** Text / icon color for busy / high-wait stores */
    val storeBusy: Color,
    /** Background chip color for busy stores */
    val storeBusyContainer: Color,

    // ── ETA / estimated time badge ───────────────────────────
    /** Background of the "~10 min" delivery time badge */
    val etaBackground: Color,
    /** Text inside the ETA badge */
    val etaContent: Color,

    // ── Pricing & cart ───────────────────────────────────────
    /** Price text color (₹ amounts) */
    val priceText: Color,
    /** Add-to-cart FAB background */
    val fabBackground: Color,
    /** Add-to-cart FAB icon / label color */
    val fabContent: Color,
    /** Quantity stepper (+/−) button background */
    val stepperBackground: Color,
    /** Quantity stepper count text */
    val stepperContent: Color,

    // ── Payment method chips ─────────────────────────────────
    /** UPI chip / badge background */
    val upiBackground: Color,
    /** UPI chip text */
    val upiContent: Color,
    /** Cash chip / badge background */
    val cashBackground: Color,
    /** Cash chip text */
    val cashContent: Color,

    // ── Bottom navigation bar ─────────────────────────────────
    /** Nav bar surface color */
    val navBarBackground: Color,
    /** Selected nav item indicator pill */
    val navBarIndicator: Color,
    /** Selected nav icon / label */
    val navBarSelected: Color,
    /** Unselected nav icon / label */
    val navBarUnselected: Color,

    // ── Top app bar ───────────────────────────────────────────
    /** TopAppBar background (crimson on both themes) */
    val topBarBackground: Color,
    /** TopAppBar title and icon tint */
    val topBarContent: Color,

    // ── Dividers & subtle borders ─────────────────────────────
    val divider: Color,
    val cardBorder: Color,
)

// ─────────────────────────────────────────────────────────────
// LIGHT VALUES
// ─────────────────────────────────────────────────────────────

val LightExtendedColors = ExtendedColors(
    // Store status
    storeOpen              = StoreOpenGreen,
    storeOpenContainer     = StoreOpenGreenContainer,
    storeClosed            = StoreClosedRed,
    storeClosedContainer   = StoreClosedRedContainer,
    storeBusy              = StoreBusyAmber,
    storeBusyContainer     = StoreBusyAmberContainer,

    // ETA badge — gold on light yellow
    etaBackground          = Gold50,
    etaContent             = Gold600,

    // Pricing & cart
    priceText              = Crimson500,
    fabBackground          = Crimson500,
    fabContent             = White,
    stepperBackground      = Crimson100,
    stepperContent         = Crimson700,

    // Payment chips
    upiBackground          = Color(0xFFE8F0FE),   // light blue — UPI brand association
    upiContent             = Color(0xFF1A56A4),
    cashBackground         = StoreOpenGreenContainer,
    cashContent            = Color(0xFF0F6840),

    // Bottom nav
    navBarBackground       = White,
    navBarIndicator        = Crimson100,
    navBarSelected         = Crimson500,
    navBarUnselected       = Charcoal400,

    // Top bar — always crimson
    topBarBackground       = Crimson500,
    topBarContent          = White,

    // Structural
    divider                = Charcoal100,
    cardBorder             = Charcoal100,
)

// ─────────────────────────────────────────────────────────────
// DARK VALUES
// ─────────────────────────────────────────────────────────────

val DarkExtendedColors = ExtendedColors(
    // Store status
    storeOpen              = StoreOpenGreenDark,
    storeOpenContainer     = StoreOpenGreenContainerDark,
    storeClosed            = StoreClosedRedDark,
    storeClosedContainer   = StoreClosedRedContainerDark,
    storeBusy              = StoreBusyAmberDark,
    storeBusyContainer     = StoreBusyAmberContainerDark,

    // ETA badge
    etaBackground          = Gold800,
    etaContent             = Gold100,

    // Pricing & cart
    priceText              = Crimson300,
    fabBackground          = Crimson400,
    fabContent             = White,
    stepperBackground      = Crimson800,
    stepperContent         = Crimson200,

    // Payment chips
    upiBackground          = Color(0xFF1A2A4A),
    upiContent             = Color(0xFF90B4F5),
    cashBackground         = StoreOpenGreenContainerDark,
    cashContent            = StoreOpenGreenDark,

    // Bottom nav
    navBarBackground       = Color(0xFF1A1A1A),
    navBarIndicator        = Crimson700,
    navBarSelected         = Crimson300,
    navBarUnselected       = Charcoal400,

    // Top bar — slightly deeper in dark mode
    topBarBackground       = Crimson600,
    topBarContent          = White,

    // Structural
    divider                = Charcoal700,
    cardBorder             = Charcoal700,
)

// ─────────────────────────────────────────────────────────────
// COMPOSITION LOCAL
// ─────────────────────────────────────────────────────────────

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }