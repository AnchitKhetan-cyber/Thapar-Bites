package com.ccs.thaparbites.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.data.dummy.*
import com.ccs.thaparbites.ui.theme.*

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────

@Composable
fun MenuScreen(
    storeId: String = "",
    onBack: () -> Unit = {},
    onViewCart: (List<CartItem>) -> Unit = {},
    menuViewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory(storeId))
) {
    val uiState by menuViewModel.uiState.collectAsStateWithLifecycle()

    // Cart state: itemId -> quantity (local, in-memory)
    val cart = remember { mutableStateMapOf<String, Int>() }

    Scaffold(
        topBar = {
            when (val state = uiState) {
                is MenuUiState.Success ->
                    MenuTopBar(store = state.store, onBack = onBack)
                else ->
                    MenuTopBarSimple(onBack = onBack)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        when (val state = uiState) {

            // ── Loading ───────────────────────────────────
            is MenuUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Loading menu...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Error ─────────────────────────────────────
            is MenuUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Couldn't load menu",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { menuViewModel.retry() }) {
                            Text("Try Again")
                        }
                    }
                }
            }

            // ── Success ───────────────────────────────────
            is MenuUiState.Success -> {
                val store     = state.store
                val menuItems = state.menuItems
                val categories = menuItems.map { it.category }.distinct()

                val cartItems = cart.entries
                    .filter { it.value > 0 }
                    .mapNotNull { (id, qty) ->
                        menuItems.find { it.id == id }?.let { CartItem(it, qty) }
                    }
                val cartTotal = cartItems.sumOf { it.menuItem.price * it.quantity }
                val cartCount = cartItems.sumOf { it.quantity }

                Scaffold(
                    floatingActionButton = {
                        if (cartCount > 0) {
                            ViewCartFab(
                                itemCount = cartCount,
                                total     = cartTotal,
                                onClick   = { onViewCart(cartItems) }
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(innerPadding),
                        contentPadding = PaddingValues(
                            bottom = if (cartCount > 0) 96.dp else 16.dp
                        )
                    ) {
                        // Store info header
                        item { StoreInfoHeader(store = store) }

                        // Menu by category
                        categories.forEach { category ->
                            val categoryItems = menuItems.filter { it.category == category }
                            item {
                                Text(
                                    text  = category,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        start = 16.dp, top = 20.dp,
                                        bottom = 8.dp, end = 16.dp
                                    )
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                            items(categoryItems, key = { it.id }) { item ->
                                MenuItemCard(
                                    item     = item,
                                    quantity = cart[item.id] ?: 0,
                                    onAdd    = { cart[item.id] = (cart[item.id] ?: 0) + 1 },
                                    onRemove = {
                                        val current = cart[item.id] ?: 0
                                        if (current > 0) cart[item.id] = current - 1
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Top Bars
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuTopBar(store: Store, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    store.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Text(
                    store.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Crimson500)
    )
}

// Simple top bar shown while loading / error (no store data yet)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuTopBarSimple(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Menu", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Crimson500)
    )
}

// ─────────────────────────────────────────────
//  Store Info Header  (unchanged from original)
// ─────────────────────────────────────────────

@Composable
private fun StoreInfoHeader(store: Store) {
    val ext = MaterialTheme.extendedColors
    val statusColor = when (store.status) {
        StoreStatus.OPEN   -> ext.storeOpen
        StoreStatus.BUSY   -> ext.storeBusy
        StoreStatus.CLOSED -> ext.storeClosed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(store.emoji, fontSize = 36.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    store.name,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    store.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoChip(
                icon  = Icons.Default.Circle,
                label = when (store.status) {
                    StoreStatus.OPEN   -> "Open Now"
                    StoreStatus.BUSY   -> "Busy"
                    StoreStatus.CLOSED -> "Closed"
                },
                tint = statusColor
            )
            if (store.status != StoreStatus.CLOSED) {
                InfoChip(
                    icon  = Icons.Default.AccessTime,
                    label = "${store.etaMinutes} min",
                    tint  = MaterialTheme.colorScheme.primary
                )
            }
            InfoChip(
                icon  = Icons.Default.Schedule,
                label = store.timings,
                tint  = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {},
                label   = { Text("UPI", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = { Text("💳", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            if (store.paymentMethod == PaymentMethod.CASH) {
                AssistChip(
                    onClick = {},
                    label   = { Text("Cash", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Text("💵", fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────
//  Menu Item Card  (unchanged from original)
// ─────────────────────────────────────────────

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val isUnavailable = !item.isAvailable

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .border(
                    1.5.dp,
                    if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFC62828),
                    RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFC62828))
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnavailable)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text     = item.description,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "₹${item.price}",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            if (isUnavailable) {
                Text(
                    "Currently unavailable",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CompactCardShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 26.sp)
            }

            Spacer(Modifier.height(6.dp))

            if (!isUnavailable) {
                if (quantity == 0) {
                    OutlinedButton(
                        onClick         = onAdd,
                        modifier        = Modifier.width(72.dp).height(32.dp),
                        contentPadding  = PaddingValues(0.dp),
                        shape           = CompactCardShape,
                        border          = androidx.compose.foundation.BorderStroke(
                            1.5.dp, MaterialTheme.colorScheme.primary
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("ADD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .width(88.dp)
                            .height(32.dp)
                            .clip(CompactCardShape)
                            .background(MaterialTheme.colorScheme.primary),
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove",
                                tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            quantity.toString(),
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add",
                                tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color    = MaterialTheme.colorScheme.outlineVariant
    )
}

// ─────────────────────────────────────────────
//  View Cart FAB  (unchanged from original)
// ─────────────────────────────────────────────

@Composable
private fun ViewCartFab(itemCount: Int, total: Int, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick          = onClick,
        shape            = PillShape,
        containerColor   = MaterialTheme.colorScheme.primary,
        contentColor     = Color.White,
        modifier         = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(BadgeShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "$itemCount item${if (itemCount > 1) "s" else ""}",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("View Cart",  style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text("₹$total",    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────

@Preview(showBackground = true, name = "Menu – Light")
@Composable
private fun MenuPreviewLight() {
    ThaparBitesTheme(darkTheme = false) { MenuScreen(storeId = "canteen_nescafe") }
}