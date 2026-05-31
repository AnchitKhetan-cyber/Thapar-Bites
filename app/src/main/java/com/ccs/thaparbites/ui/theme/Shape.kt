package com.ccs.thaparbites.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────
// SHAPE SCALE
//
// Corner radii are intentionally generous — the logo's rounded
// letterforms and delivery dome motif call for soft, friendly
// UI shapes rather than sharp corners.
//
// Material 3 shape roles → Thapar Bites usage:
//
//   extraSmall  →  4dp   Input field corners, tooltip arrows
//   small       →  8dp   Chips, badges, small buttons, snackbar
//   medium      → 16dp   Menu item cards, category chips, dialogs
//   large       → 24dp   Store cards, bottom sheet top corners
//   extraLarge  → 32dp   Floating panels, full-width promo banners
//   full (pill) → 50%   FAB, search bar, quantity stepper, tags
// ─────────────────────────────────────────────────────────────

val ThaparBitesShapes = Shapes(

    // Text fields, tooltip backgrounds, small containers
    extraSmall = RoundedCornerShape(4.dp),

    // Status chips ("Open", "Closed"), icon buttons, badges
    small = RoundedCornerShape(8.dp),

    // Food item cards, category tabs, alert dialogs
    medium = RoundedCornerShape(16.dp),

    // Store cards on home screen, modal bottom sheet
    // (top corners only for sheets — see BottomSheet usage below)
    large = RoundedCornerShape(24.dp),

    // Promo banners, full-width image cards, large overlays
    extraLarge = RoundedCornerShape(32.dp),
)

// ─────────────────────────────────────────────────────────────
// NAMED SHAPE ALIASES
// Import these by name for clarity at call sites.
// ─────────────────────────────────────────────────────────────

/** 50% radius — FAB, search pill, quantity stepper, price tag */
val PillShape = RoundedCornerShape(50)

/** Top-only rounding for modal bottom sheets */
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

/** Left-only rounding for side-panel navigation drawer */
val DrawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)

/** Standard card — store card, item card */
val CardShape = RoundedCornerShape(16.dp)

/** Compact card — order history rows, cart items */
val CompactCardShape = RoundedCornerShape(12.dp)

/** Badge / chip */
val BadgeShape = RoundedCornerShape(8.dp)