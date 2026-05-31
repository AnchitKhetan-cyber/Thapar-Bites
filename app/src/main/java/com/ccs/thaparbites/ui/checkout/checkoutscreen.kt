package com.ccs.thaparbites.ui.checkout

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.ccs.thaparbites.data.dummy.dummyStores
import com.ccs.thaparbites.data.dummy.dummyMenuItems
import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.ui.theme.Crimson500
import com.ccs.thaparbites.ui.theme.ThaparBitesTheme
import com.ccs.thaparbites.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel = viewModel(),
    onBack: () -> Unit,
    onOrderPlaced: (orderId: String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Navigate on success
    LaunchedEffect(state.uiState) {
        if (state.uiState is CheckoutUiState.Success) {
            onOrderPlaced((state.uiState as CheckoutUiState.Success).orderId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Crimson500,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                total = state.total,
                isLoading = state.uiState is CheckoutUiState.Placing,
                paymentMethod = state.selectedPayment,
                onPlaceOrder = {
                    viewModel.placeOrder { upiId, storeName, amount ->
                        launchUpiIntent(context, upiId, storeName, amount)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // Error banner
            AnimatedVisibility(visible = state.uiState is CheckoutUiState.Error) {
                val msg = (state.uiState as? CheckoutUiState.Error)?.message ?: ""
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.dismissError() }
                ) {
                    Text(
                        text = "⚠️ $msg  (tap to dismiss)",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- Store info ---
            state.store?.let { store ->
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(store.emoji, fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                store.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                store.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- Delivery details ---
            SectionCard(title = "Delivery To") {
                val u = state.user
                DetailRow("Name", u.name)
                DetailRow("Phone", "+91 ${u.phone}")
                DetailRow("Hostel", u.hostelName)
            }

            // --- Order summary ---
            SectionCard(title = "Order Summary") {
                state.cart.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                BillRow("Subtotal", "₹${"%.0f".format(state.subtotal)}")
                BillRow("Delivery fee", "₹${"%.0f".format(state.deliveryFee)}")
                Spacer(Modifier.height(4.dp))
                BillRow(
                    "Total",
                    "₹${"%.0f".format(state.total)}",
                    bold = true,
                    color = Crimson500
                )
            }

            // --- Payment method ---
            SectionCard(title = "Payment Method") {
                val store = state.store
                val allowCash = store?.paymentMethod == PaymentMethod.UPI_AND_CASH

                PaymentOption(
                    label = "UPI / Online",
                    icon = "📲",
                    selected = state.selectedPayment != PaymentMethod.CASH,
                    onClick = { viewModel.selectPayment(PaymentMethod.UPI_ONLY) }
                )
                if (allowCash) {
                    Spacer(Modifier.height(8.dp))
                    PaymentOption(
                        label = "Cash on Delivery",
                        icon = "💵",
                        selected = state.selectedPayment == PaymentMethod.CASH,
                        onClick = { viewModel.selectPayment(PaymentMethod.CASH) }
                    )
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This store only accepts UPI payments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Small building blocks ────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    bold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
        Text(
            value,
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun PaymentOption(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ext = MaterialTheme.extendedColors
    val borderColor = if (selected) Crimson500 else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Crimson500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CheckoutBottomBar(
    total: Double,
    isLoading: Boolean,
    paymentMethod: PaymentMethod,
    onPlaceOrder: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₹${"%.0f".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Crimson500
                )
            }
            Button(
                onClick = onPlaceOrder,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Crimson500),
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.height(48.dp)
            ) {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        val label = if (paymentMethod == PaymentMethod.CASH) "Place Order" else "Pay ₹${"%.0f".format(total)}"
                        Text(label, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── UPI Intent ───────────────────────────────────────────────────────────────

fun launchUpiIntent(context: Context, upiId: String, storeName: String, amount: Double) {
    val upiUri = Uri.parse(
        "upi://pay?pa=$upiId&pn=${Uri.encode(storeName)}&am=${"%.2f".format(amount)}&cu=INR&tn=ThaparBites"
    )
    val intent = Intent(Intent.ACTION_VIEW, upiUri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    // If no UPI app is installed, catch the ActivityNotFoundException gracefully
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Show a snackbar / toast in real app
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Checkout Light")
@Composable
private fun CheckoutPreviewLight() {
    ThaparBitesTheme(darkTheme = false) {
        CheckoutScreen(onBack = {}, onOrderPlaced = {})
    }
}

@Preview(showBackground = true, name = "Checkout Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CheckoutPreviewDark() {
    ThaparBitesTheme(darkTheme = true) {
        CheckoutScreen(onBack = {}, onOrderPlaced = {})
    }
}