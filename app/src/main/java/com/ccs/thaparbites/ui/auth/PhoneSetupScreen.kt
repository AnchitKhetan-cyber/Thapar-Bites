package com.ccs.thaparbites.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ccs.thaparbites.R
import com.ccs.thaparbites.navigation.NavRoutes
import com.ccs.thaparbites.ui.components.GlassCard
import com.ccs.thaparbites.ui.components.glassTextFieldColors
import com.ccs.thaparbites.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private val HOSTELS_SETUP = listOf(
    "Agira Hall", "Ambaram Hall", "Amritam Hall", "Ananta Hall", "Anantam Hall",
    "Dhriti Hall", "FRF", "FRG", "Ira Hall", "Neeram Hall", "Prithvi Hall",
    "Tejas Hall", "Vahni Hall", "Vasudha Hall - Block E", "Vasudha Hall - Block G",
    "Viyat Hall", "Vyan Hall", "Vyom Hall"
).sorted()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneSetupScreen(navController: NavController) {
    var phone by remember { mutableStateOf("") }
    var hostel by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {

        // ── 1. Campus background ────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.thapar_campus),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ── 2. Gradient scrim ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.60f),
                            0.35f to Color.Black.copy(alpha = 0.35f),
                            0.60f to Color.Black.copy(alpha = 0.45f),
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // ── 3. Scrollable content with IME padding ──────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                // imePadding pushes the column up when the soft keyboard appears
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            BrandHeaderOnImage()

            Spacer(modifier = Modifier.height(36.dp))

            // ── Glassmorphism card (reusable component) ─────
            GlassCard(modifier = Modifier.fillMaxWidth()) {

                // ── Header ──────────────────────────────────
                Text(
                    text = "Almost there! 🎉",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add your phone & hostel for order updates and delivery support.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Phone section pill ───────────────────────
                SectionPill(icon = Icons.Default.Phone, label = "PHONE NUMBER")

                Spacer(modifier = Modifier.height(8.dp))

                // ── Phone row ────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.30f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "+91",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TextField(
                        value = phone,
                        onValueChange = { input ->
                            val digits = input.filter(Char::isDigit)
                            phone = when {
                                digits.length == 12 && digits.startsWith("91") ->
                                    digits.substring(2)
                                digits.length > 10 -> digits.takeLast(10)
                                else -> digits
                            }
                            error = null
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "9876543210",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 14.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    AnimatedVisibility(visible = phone.length == 10) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid number",
                            tint = Color(0xFF1DA462),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Hostel section pill ──────────────────────
                SectionPill(icon = Icons.Default.Home, label = "HOSTEL / RESIDENCE")

                Spacer(modifier = Modifier.height(8.dp))

                // ── Hostel dropdown ──────────────────────────
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = hostel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hostel / Residence") },
                        placeholder = { Text("Select your hostel") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = glassTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        HOSTELS_SETUP.forEach { hostelName ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        hostelName,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                onClick = {
                                    hostel = hostelName
                                    expanded = false
                                    error = null
                                },
                                leadingIcon = if (hostelName == hostel) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Crimson500,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

                // ── Error banner ─────────────────────────────
                AnimatedVisibility(visible = error != null) {
                    error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        ErrorBanner(message = it)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Continue button ──────────────────────────
                Button(
                    onClick = {
                        when {
                            phone.length != 10 -> {
                                error = "Enter a valid 10-digit phone number"
                                return@Button
                            }
                            hostel.isBlank() -> {
                                error = "Please select your hostel"
                                return@Button
                            }
                        }

                        loading = true
                        val user = FirebaseAuth.getInstance().currentUser

                        if (user == null) {
                            loading = false
                            error = "User not logged in"
                            return@Button
                        }

                        val userData = hashMapOf(
                            "phone" to phone,
                            "hostelName" to hostel
                        )

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.uid)
                            .set(userData, SetOptions.merge())
                            .addOnSuccessListener {
                                loading = false
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.PHONE_SETUP) { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                loading = false
                                error = it.message ?: "Failed to save details"
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !loading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Continue",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            // Extra bottom breathing room so content clears the nav bar
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}