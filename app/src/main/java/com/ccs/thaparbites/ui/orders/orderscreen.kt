package com.ccs.thaparbites.ui.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.data.dummy.Order
import com.ccs.thaparbites.data.dummy.OrderStatus
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.ccs.thaparbites.ui.home.HomeBottomBar
import com.ccs.thaparbites.ui.theme.Crimson500
import com.ccs.thaparbites.ui.theme.ThaparBitesTheme
import com.ccs.thaparbites.ui.theme.extendedColors
import java.text.SimpleDateFormat
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel = viewModel(),
    onNavigateHome: () -> Unit,
    onNavigateCart: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Crimson500,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            HomeBottomBar(
                currentRoute = "ORDERS",
                onHomeClick = onNavigateHome,
                onOrdersClick = {},
                onCartClick = onNavigateCart,
                onProfileClick = onNavigateProfile
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is OrdersUiState.Loading -> {
                    CircularProgressIndicator(
                        color = Crimson500,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is OrdersUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadOrders() }) {
                            Text("Retry", color = Crimson500)
                        }
                    }
                }

                is OrdersUiState.Success -> {
                    if (state.orders.isEmpty()) {
                        EmptyOrdersState()
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item { Spacer(Modifier.height(8.dp)) }
                            itemsIndexed(state.orders, key = { _, o -> o.id }) { index, order ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(300, delayMillis = index * 60)) +
                                            slideInVertically(tween(300, delayMillis = index * 60)) { it / 4 }
                                ) {
                                    OrderRow(
                                        order = order,
                                        onClick = { viewModel.selectOrder(order) }
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }

        // Bottom sheet detail
        if (selectedOrder != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelectedOrder() },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                selectedOrder?.let { order ->
                    OrderDetailSheet(order = order)
                }
            }
        }
    }
}

// ── Order Row ────────────────────────────────────────────────────────────────

@Composable
private fun OrderRow(order: Order, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", LocalLocale.current.platformLocale)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(order.storeEmoji, fontSize = 28.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            order.storeName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            order.id,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OrderStatusChip(status = order.status)
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // Item summary
            Text(
                order.items.joinToString(" · ") { "${it.menuItem.name} ×${it.quantity}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    dateFormat.format(order.placedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₹${"%.0f".format(order.total)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Crimson500
                )
            }
        }
    }
}

// ── Status chip ───────────────────────────────────────────────────────────────

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val ext = MaterialTheme.extendedColors
    val (bgColor, textColor, label) = when (status) {
        OrderStatus.PLACED    -> Triple(ext.etaContent.copy(alpha = 0.15f), ext.etaContent, "Placed")
        OrderStatus.PREPARING -> Triple(ext.storeBusy.copy(alpha = 0.15f), ext.storeBusy, "Preparing")
        OrderStatus.READY     -> Triple(ext.storeOpen.copy(alpha = 0.15f), ext.storeOpen, "Ready")
        OrderStatus.DELIVERED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Delivered"
        )
        OrderStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Cancelled"
        )

        OrderStatus.CONFIRMED -> TODO()
    }

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Detail sheet ──────────────────────────────────────────────────────────────

@Composable
private fun OrderDetailSheet(order: Order) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", LocalLocale.current.platformLocale)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(order.storeEmoji, fontSize = 36.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(order.storeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(order.id, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            OrderStatusChip(status = order.status)
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        // Items
        Text("Items", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        order.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${item.menuItem.emoji} ${item.menuItem.name} × ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "₹${"%.0f".format(item.menuItem.price * item.quantity)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        // Bill
        SheetBillRow("Subtotal", "₹${"%.0f".format(order.subtotal)}")
        SheetBillRow("Delivery fee", "₹${"%.0f".format(order.deliveryFee)}")
        Spacer(Modifier.height(4.dp))
        SheetBillRow("Total", "₹${"%.0f".format(order.total)}", bold = true, color = Crimson500)

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        // Meta
        SheetBillRow(
            "Payment",
            if (order.paymentMethod == PaymentMethod.CASH) "Cash" else "UPI"
        )
        SheetBillRow("Placed at", dateFormat.format(order.placedAt))

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SheetBillRow(
    label: String,
    value: String,
    bold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (bold) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyOrdersState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🍽️", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No orders yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Browse stores and place your first order!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Orders Light")
@Composable
private fun OrdersPreviewLight() {
    ThaparBitesTheme(darkTheme = false) {
        OrdersScreen(onNavigateHome = {}, onNavigateCart = {}, onNavigateProfile = {})
    }
}

@Preview(showBackground = true, name = "Orders Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OrdersPreviewDark() {
    ThaparBitesTheme(darkTheme = true) {
        OrdersScreen(onNavigateHome = {}, onNavigateCart = {}, onNavigateProfile = {})
    }
}

