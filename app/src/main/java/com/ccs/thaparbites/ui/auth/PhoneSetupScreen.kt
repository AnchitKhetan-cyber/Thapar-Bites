package com.ccs.thaparbites.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccs.thaparbites.navigation.NavRoutes
import com.ccs.thaparbites.ui.theme.CardShape
import com.ccs.thaparbites.ui.theme.CompactCardShape
import com.ccs.thaparbites.ui.theme.Crimson500
import com.ccs.thaparbites.ui.theme.Crimson600
import com.ccs.thaparbites.ui.theme.PillShape
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneSetupScreen(
    navController: NavController
) {

    var phone by remember { mutableStateOf("") }
    var hostel by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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
    ).sorted()


    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            drawArc(
                brush = Brush.verticalGradient(
                    colors = listOf(Crimson500, Crimson600)
                ),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(
                    -size.width * 0.1f,
                    -size.height * 0.8f
                ),
                size = Size(
                    size.width * 1.2f,
                    size.height * 1.8f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(56.dp))

            BrandHeader()

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "Complete Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add your phone number for order updates and delivery support.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "+91",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        TextField(
                            value = phone,
                            onValueChange = { input ->

                                val digits = input.filter(Char::isDigit)

                                phone = when {
                                    digits.length == 12 && digits.startsWith("91") ->
                                        digits.substring(2)

                                    digits.length > 10 ->
                                        digits.takeLast(10)

                                    else ->
                                        digits
                                }

                                error = null
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("9876543210")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        }
                    ) {

                        OutlinedTextField(
                            value = hostel,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = {
                                Text("Hostel")
                            },
                            placeholder = {
                                Text("Select Hostel")
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {

                            hostels.forEach { hostelName ->

                                DropdownMenuItem(
                                    text = {
                                        Text(hostelName)
                                    },
                                    onClick = {
                                        hostel = hostelName
                                        expanded = false
                                        error = null
                                    }
                                )
                            }
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ErrorBanner(error!!)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {

                            if (phone.length != 10) {
                                error = "Enter a valid 10-digit phone number"
                                return@Button
                            }
                            if (hostel.isBlank()) {
                                error = "Please enter your hostel name"
                                return@Button
                            }

                            loading = true

                            val user = FirebaseAuth.getInstance().currentUser

                            if (user == null) {
                                loading = false
                                error = "User not logged in"
                                return@Button
                            }

                            val db = FirebaseFirestore.getInstance()

                            val userData = hashMapOf(
                                "phone" to phone,
                                "hostelName" to hostel
                            )

                            db.collection("users")
                                .document(user.uid)
                                .set(userData, SetOptions.merge())
                                .addOnSuccessListener {

                                    loading = false

                                    navController.navigate(NavRoutes.HOME) {
                                        popUpTo(NavRoutes.PHONE_SETUP) {
                                            inclusive = true
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    loading = false
                                    error = it.message ?: "Failed to save phone number"
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = PillShape
                    ) {

                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
}