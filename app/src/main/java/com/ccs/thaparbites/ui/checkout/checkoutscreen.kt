package com.ccs.thaparbites.ui.checkout

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
    cartViewModel: SharedCartViewModel,
    checkoutViewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModel.Factory()
    )
) {
    val cartItems   by cartViewModel.cartItems.collectAsState()
    val subtotal    = cartViewModel.subtotal
    val deliveryFee = cartViewModel.deliveryFee
    val total       = cartViewModel.total
    val uiState     by checkoutViewModel.uiState.collectAsState()

    var selectedPayment by remember { mutableStateOf(PaymentMethod.UPI) }
    var hostel          by remember { mutableStateOf("") }
    var roomNumber      by remember { mutableStateOf("") }
    var showDialog      by remember { mutableStateOf(false) }
    var fieldError      by remember { mutableStateOf<String?>(null) }

    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current

    // ── UPI launcher ────────────────────────────────────────────────────────
    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val response = result.data?.getStringExtra("response") ?: ""
            if (response.contains("SUCCESS", ignoreCase = true)) {
                checkoutViewModel.placeOrder(
                    cartItems     = cartItems,
                    subtotal      = subtotal,
                    deliveryFee   = deliveryFee,
                    total         = total,
                    paymentMethod = selectedPayment,
                    hostel        = hostel,
                    roomNumber    = roomNumber
                )
            }
        }
    }

    val hostels = listOf(
        "Agira Hall",
        "Ambaram Hall",
        "Amritam Hall",
        "Ananta Hall",
        "Anantam Hall",
        "Dhriti Hall",
        "FRF",
        "FRG",
        "Ira Hall",
        "Neeram Hall",
        "Prithvi Hall",
        "Tejas Hall",
        "Vahni Hall",
        "Vasudha Hall - Block E",
        "Vasudha Hall - Block G",
        "Viyat Hall",
        "Vyan Hall",
        "Vyom Hall"
    )

    var expanded by remember { mutableStateOf(false) }

    fun openUpiPayment(amount: Double, upiId: String, storeName: String) {
        val uri = Uri.parse(
            "upi://pay?pa=$upiId&pn=$storeName&am=$amount&cu=INR"
        )
        val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), "Pay using")
        upiLauncher.launch(chooser)
    }

    // ── React to successful order ────────────────────────────────────────────
    LaunchedEffect(uiState) {
        if (uiState is CheckoutUiState.Success) {
            cartViewModel.clearCart()
            onOrderPlaced((uiState as CheckoutUiState.Success).orderId)
        }
    }

    // ── Confirm dialog ───────────────────────────────────────────────────────
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Order", fontWeight = FontWeight.Bold) },
            text = {
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
                Button(
                    onClick = {
                        showDialog = false
                        if (selectedPayment == PaymentMethod.UPI) {
                            openUpiPayment(
                                amount    = total,
                                upiId     = "9306731334@fam",
                                storeName = "Thapar Bites"
                            )
                        } else {
                            checkoutViewModel.placeOrder(
                                cartItems     = cartItems,
                                subtotal      = subtotal,
                                deliveryFee   = deliveryFee,
                                total         = total,
                                paymentMethod = selectedPayment,
                                hostel        = hostel,
                                roomNumber    = roomNumber
                            )
                        }
                    }
                ) { Text("Place Order") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Scaffold ─────────────────────────────────────────────────────────────
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
                    onClick = {
                        focusManager.clearFocus()
                        // Validate fields before showing dialog
                        when {
                            hostel.isBlank() -> fieldError = "Please enter your hostel / block name."
                            roomNumber.isBlank() -> fieldError = "Please enter your room number."
                            else -> {
                                fieldError = null
                                showDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(52.dp),
                    enabled = uiState !is CheckoutUiState.Loading
                ) {
                    if (uiState is CheckoutUiState.Loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
        ) {
            // ── All content in ONE scrollable Column ─────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Error banner (API error)
                if (uiState is CheckoutUiState.Error) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text     = (uiState as CheckoutUiState.Error).message,
                            modifier = Modifier.padding(12.dp),
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }

                // Validation error banner
                if (fieldError != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text     = fieldError!!,
                            modifier = Modifier.padding(12.dp),
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }

                // ── Order Summary ─────────────────────────────────────────────
                CheckoutCard(title = "Order Summary") {
                    cartItems.forEach { cartItem ->
                        Row(
                            modifier             = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text     = "${cartItem.menuItem.emoji}  ${cartItem.menuItem.name} × ${cartItem.quantity}",
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text       = "₹${cartItem.menuItem.price * cartItem.quantity}",
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

                // ── Delivery Details ──────────────────────────────────────────
                CheckoutCard(title = "Delivery Details") {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {

                        OutlinedTextField(
                            value = hostel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Hostel") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            isError = fieldError != null && hostel.isBlank()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            hostels.forEach { hostelName ->
                                DropdownMenuItem(
                                    text = { Text(hostelName) },
                                    onClick = {
                                        hostel = hostelName
                                        fieldError = null
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value         = roomNumber,
                        onValueChange = {
                            roomNumber = it
                            fieldError = null
                        },
                        label         = { Text("Room Number") },
                        placeholder   = { Text("e.g. 204") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        singleLine    = true,
                        isError       = fieldError != null && roomNumber.isBlank()
                    )
                }

                // ── Payment Method ────────────────────────────────────────────
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
}

// ── Reusable card wrapper ─────────────────────────────────────────────────────
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