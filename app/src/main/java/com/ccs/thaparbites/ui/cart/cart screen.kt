package com.ccs.thaparbites.ui.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccs.thaparbites.data.dummy.*
import com.ccs.thaparbites.ui.theme.*

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────

@Composable
fun CartScreen(
    initialCart: List<CartItem> = listOf(
        CartItem(dummyMenuItems[10], 2),
        CartItem(dummyMenuItems[11], 1),
        CartItem(dummyMenuItems[13], 1)
    ),
    storeName: String = "Momos Corner",
    onBack: () -> Unit = {},
    onCheckout: (List<CartItem>) -> Unit = {}
) {
    // Mutable cart state
    val cart = remember {
        mutableStateListOf<CartItem>().also { it.addAll(initialCart) }
    }

    fun updateQty(item: MenuItem, delta: Int) {
        val idx = cart.indexOfFirst { it.menuItem.id == item.id }
        if (idx == -1) return
        val newQty = cart[idx].quantity + delta
        if (newQty <= 0) cart.removeAt(idx)
        else cart[idx] = cart[idx].copy(quantity = newQty)
    }

    val subtotal = cart.sumOf { it.menuItem.price * it.quantity }
    val deliveryFee = if (subtotal > 0) 10 else 0
    val total = subtotal + deliveryFee

    Scaffold(
        topBar = { CartTopBar(itemCount = cart.sumOf { it.quantity }, onBack = onBack) },
        bottomBar = {
            if (cart.isNotEmpty()) {
                CartCheckoutBar(total = total, onCheckout = { onCheckout(cart.toList()) })
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AnimatedVisibility(
            visible = cart.isEmpty(),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            EmptyCartState(modifier = Modifier.padding(padding))
        }

        AnimatedVisibility(
            visible = cart.isNotEmpty(),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Store banner
                item {
                    StoreBanner(storeName = storeName)
                }

                // Cart items header
                item {
                    Text(
                        text = "Your Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(cart, key = { it.menuItem.id }) { cartItem ->
                    CartItemRow(
                        cartItem = cartItem,
                        onAdd = { updateQty(cartItem.menuItem, +1) },
                        onRemove = { updateQty(cartItem.menuItem, -1) },
                        modifier = Modifier.animateItem()
                    )
                }

                // Bill summary
                item {
                    Spacer(Modifier.height(8.dp))
                    BillSummary(subtotal = subtotal, deliveryFee = deliveryFee, total = total)
                }

                // Delivery note
                item {
                    DeliveryNoteCard()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Top Bar
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartTopBar(itemCount: Int, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text("My Cart", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Color.White)
                if (itemCount > 0)
                    Text("$itemCount item${if (itemCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f))
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

// ─────────────────────────────────────────────
//  Store Banner
// ─────────────────────────────────────────────

@Composable
private fun StoreBanner(storeName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Store, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Ordering from ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = storeName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider()
}

// ─────────────────────────────────────────────
//  Cart Item Row
// ─────────────────────────────────────────────

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = cartItem.menuItem
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Veg/non-veg dot
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

        // Emoji
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CompactCardShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, fontSize = 22.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "₹${item.price} each",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stepper
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CompactCardShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (cartItem.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                cartItem.quantity.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add",
                    tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        // Line total
        Text(
            "₹${item.price * cartItem.quantity}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// ─────────────────────────────────────────────
//  Bill Summary
// ─────────────────────────────────────────────

@Composable
private fun BillSummary(subtotal: Int, deliveryFee: Int, total: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Bill Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider()
            BillRow("Item Total", "₹$subtotal")
            BillRow("Delivery Fee", if (deliveryFee == 0) "Free" else "₹$deliveryFee")
            HorizontalDivider()
            BillRow(
                label = "To Pay",
                value = "₹$total",
                bold = true,
                valueColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    bold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

// ─────────────────────────────────────────────
//  Delivery note
// ─────────────────────────────────────────────

@Composable
private fun DeliveryNoteCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(CompactCardShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🛵", fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            "Your order will be delivered to your hostel. Keep your phone handy!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─────────────────────────────────────────────
//  Bottom checkout bar
// ─────────────────────────────────────────────

@Composable
private fun CartCheckoutBar(total: Int, onCheckout: () -> Unit) {
    Surface(
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₹$total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onCheckout,
                shape = PillShape,
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Proceed to Checkout",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Empty state
// ─────────────────────────────────────────────

@Composable
private fun EmptyCartState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛒", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Your cart is empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Browse stores and add items to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────

@Preview(showBackground = true, name = "Cart – Light")
@Composable
private fun CartPreviewLight() {
    ThaparBitesTheme(darkTheme = false) { CartScreen() }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Cart – Dark")
@Composable
private fun CartPreviewDark() {
    ThaparBitesTheme(darkTheme = true) { CartScreen() }
}

@Preview(showBackground = true, name = "Cart – Empty")
@Composable
private fun CartPreviewEmpty() {
    ThaparBitesTheme(darkTheme = false) { CartScreen(initialCart = emptyList()) }
}

