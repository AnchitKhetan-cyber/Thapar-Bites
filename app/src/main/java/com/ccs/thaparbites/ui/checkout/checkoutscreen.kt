package com.ccs.thaparbites.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.ccs.thaparbites.ui.cart.BillRow
import com.ccs.thaparbites.ui.shared.SharedCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (orderId: String) -> Unit,
    cartViewModel: SharedCartViewModel = viewModel(),
    checkoutViewModel: CheckoutViewModel = viewModel(factory = CheckoutViewModel.Factory())
) {
    val cartItems       by cartViewModel.cartItems.collectAsState()
    val subtotal        = cartViewModel.subtotal
    val deliveryFee     = cartViewModel.deliveryFee
    val total           = cartViewModel.total
    val uiState         by checkoutViewModel.uiState.collectAsState()

    var selectedPayment by remember { mutableStateOf(PaymentMethod.UPI) }
    var hostel          by remember { mutableStateOf("") }
    var roomNumber      by remember { mutableStateOf("") }
    var showDialog      by remember { mutableStateOf(false) }

    // React to successful order placement
    LaunchedEffect(uiState) {
        if (uiState is CheckoutUiState.Success) {
            cartViewModel.clearCart()
            onOrderPlaced((uiState as CheckoutUiState.Success).orderId)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Order", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Place order for ₹${total.toInt()}?")
                    Text(
                        "Payment: ${if (selectedPayment == PaymentMethod.UPI) "UPI" else "Cash on Delivery"}",
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    checkoutViewModel.placeOrder(
                        cartItems     = cartItems,
                        subtotal      = subtotal,
                        deliveryFee   = deliveryFee,
                        total         = total,
                        paymentMethod = selectedPayment,
                        hostel        = hostel,
                        roomNumber    = roomNumber
                    )
                }) {
                    Text("Place Order", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 10.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick  = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    enabled  = cartItems.isNotEmpty() && uiState !is CheckoutUiState.Loading
                ) {
                    if (uiState is CheckoutUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color    = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Place Order  •  ₹${total.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Error banner
            if (uiState is CheckoutUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        (uiState as CheckoutUiState.Error).message,
                        modifier = Modifier.padding(12.dp),
                        color    = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }

            // Order summary
            CheckoutCard(title = "Order Summary") {
                cartItems.forEach { cartItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${cartItem.menuItem.emoji}  ${cartItem.menuItem.name} × ${cartItem.quantity}",
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "₹${cartItem.menuItem.price * cartItem.quantity}",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                BillRow("Subtotal",     "₹${subtotal.toInt()}")
                BillRow("Delivery fee", "₹${deliveryFee.toInt()}")
                BillRow("Total",        "₹${total.toInt()}", bold = true)
            }

            // Delivery details
            CheckoutCard(title = "Delivery Details") {
                OutlinedTextField(
                    value         = hostel,
                    onValueChange = { hostel = it },
                    label         = { Text("Hostel / Block Name") },
                    placeholder   = { Text("e.g. Kailash Boys Hostel") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    singleLine    = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value         = roomNumber,
                    onValueChange = { roomNumber = it },
                    label         = { Text("Room Number") },
                    placeholder   = { Text("e.g. 204") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    singleLine    = true
                )
            }

            // Payment
            CheckoutCard(title = "Payment Method") {
                PaymentMethod.entries.forEach { method ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPayment == method,
                            onClick  = { selectedPayment = method }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text     = if (method == PaymentMethod.UPI) "💳  UPI" else "💵  Cash on Delivery",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}