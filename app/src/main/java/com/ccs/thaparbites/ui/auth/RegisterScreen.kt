package com.ccs.thaparbites.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.R
import com.ccs.thaparbites.ui.components.GlassCard
import com.ccs.thaparbites.ui.components.glassTextFieldColors
import com.ccs.thaparbites.ui.theme.*

private val HOSTELS = listOf(
    "Agira Hall", "Ambaram Hall", "Amritam Hall", "Ananta Hall", "Anantam Hall",
    "Dhriti Hall", "FRF", "FRG", "Ira Hall", "Neeram Hall", "Prithvi Hall",
    "Tejas Hall", "Vahni Hall", "Vasudha Hall - Block E", "Vasudha Hall - Block G",
    "Viyat Hall", "Vyan Hall", "Vyom Hall"
).sorted()

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory())
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RegisterEvent.NavigateToHome -> onRegisterSuccess()
                else -> Unit
            }
        }
    }

    RegisterContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onHostelChange = viewModel::onHostelChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onToggleConfirmVisibility = viewModel::toggleConfirmPasswordVisibility,
        onRegisterClick = viewModel::register,
        onNavigateToLogin = onNavigateToLogin
    )
}

// ─────────────────────────────────────────────
//  Stateless content
// ─────────────────────────────────────────────

@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onHostelChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit = {},
    onToggleConfirmVisibility: () -> Unit = {},
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() }
            .imePadding()
    ) {
        // ── 1. Campus background image ──────────────────────
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
                            0.0f to Color.Black.copy(alpha = 0.65f),
                            0.30f to Color.Black.copy(alpha = 0.40f),
                            0.55f to Color.Black.copy(alpha = 0.50f),
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // ── 3. Scrollable content ───────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Brand header — white on top of the image
            BrandHeaderOnImage()

            Spacer(modifier = Modifier.height(32.dp))

            // ── Frosted glass card ──────────────────────────
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ── Section: Personal Details ───────────
                    SectionPill(icon = Icons.Default.Person, label = "Personal Details")

                    // Full Name
                    AuthTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = "Full Name",
                        placeholder = "e.g. Arjun Sharma",
                        leadingIcon = Icons.Default.Person,
                        error = uiState.nameError,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Email
                    AuthTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = "Email",
                        placeholder = "yourname@thapar.edu",
                        leadingIcon = Icons.Default.Email,
                        error = uiState.emailError,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    // Phone — prefix handled inline
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = onPhoneChange,
                        label = { Text("Phone Number") },
                        placeholder = { Text("10-digit mobile number") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        prefix = {
                            Text(
                                "+91 ",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        isError = uiState.phoneError != null,
                        supportingText = {
                            if (uiState.phoneError != null)
                                Text(uiState.phoneError, color = Crimson500)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = glassTextFieldColors()
                    )

                    // Hostel
                    HostelDropdown(
                        selectedHostel = uiState.hostelName,
                        onHostelSelected = onHostelChange,
                        error = uiState.hostelError
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    // ── Section: Set Password ───────────────
                    SectionPill(icon = Icons.Default.Lock, label = "Set Password")

                    // Password
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.passwordVisible)
                                        Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (uiState.passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        isError = uiState.passwordError != null,
                        supportingText = {
                            if (uiState.passwordError != null)
                                Text(uiState.passwordError, color = Crimson500)
                            else
                                Text(
                                    "Minimum 6 characters",
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = glassTextFieldColors()
                    )

                    // Confirm Password
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.confirmPassword.isNotEmpty() &&
                                    uiState.confirmPassword == uiState.password
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Passwords match",
                                        tint = Color(0xFF1DA462),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                IconButton(onClick = onToggleConfirmVisibility) {
                                    Icon(
                                        imageVector = if (uiState.confirmPasswordVisible)
                                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        visualTransformation = if (uiState.confirmPasswordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        isError = uiState.confirmPasswordError != null,
                        supportingText = {
                            if (uiState.confirmPasswordError != null)
                                Text(uiState.confirmPasswordError, color = Crimson500)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onRegisterClick()
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = glassTextFieldColors()
                    )

                    // Global error
                    AnimatedVisibility(visible = uiState.generalError != null) {
                        uiState.generalError?.let { ErrorBanner(message = it) }
                    }

                    // Create Account button
                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.35f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign in row — white text on image background
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.90f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onNavigateToLogin)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  Helper composables
// ─────────────────────────────────────────────

/**
 * Small crimson pill label used as section header inside the card.
 */
@Composable
fun SectionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onNext: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color.White
            )
        },
        isError = error != null,
        supportingText = {
            if (error != null) Text(error, color = Crimson500)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onNext?.invoke() }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            12.dp),
        colors = glassTextFieldColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostelDropdown(
    selectedHostel: String,
    onHostelSelected: (String) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedHostel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Hostel / Residence") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error != null,
            supportingText = {
                if (error != null) Text(error, color = Crimson500)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = glassTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                Color(0xFF1A1A1A)
            )
        ) {
            HOSTELS.forEach { hostel ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = hostel,
                            color = Color.White
                        )
                    },
                    onClick = {
                        onHostelSelected(hostel)
                        expanded = false
                    },
                    leadingIcon = if (hostel == selectedHostel) {
                        {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────

@Preview(showBackground = true, name = "Register – Light")
@Composable
private fun RegisterPreviewLight() {
    ThaparBitesTheme(darkTheme = false) {
        RegisterContent(
            uiState = RegisterUiState(),
            onNameChange = {}, onEmailChange = {}, onPhoneChange = {},
            onHostelChange = {}, onPasswordChange = {}, onConfirmPasswordChange = {},
            onRegisterClick = {}, onNavigateToLogin = {}
        )
    }
}