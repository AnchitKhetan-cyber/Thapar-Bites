package com.ccs.thaparbites.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.ui.theme.*

private val HOSTELS = listOf(
    "Kailash Boys Hostel",
    "Himachal Boys Hostel",
    "Vindhyachal Boys Hostel",
    "Aravali Boys Hostel",
    "Satpura Boys Hostel",
    "Nilgiri Boys Hostel",
    "Shivalik Girls Hostel",
    "Manimahesh Girls Hostel",
    "Day Scholar"
)

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

    // One-shot event collector — same Channel pattern as Humble Contacts
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Crimson arc — smaller than login (top accent only)
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
        ) {
            drawArc(
                brush = Brush.verticalGradient(
                    colors = listOf(Crimson500, Crimson600)
                ),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(
                    -size.width * 0.1f,
                    -size.height * 0.6f
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.width * 1.2f,
                    size.height * 2f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // ── Compact brand bar ─────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ti",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Crimson500,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "THAPAR BITES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Create your account",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Form Card ─────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text(
                        text = "Personal Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

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

                    // Phone
                    AuthTextField(
                        value = uiState.phone,
                        onValueChange = onPhoneChange,
                        label = "Phone Number",
                        placeholder = "10-digit mobile number",
                        leadingIcon = Icons.Default.Phone,
                        error = uiState.phoneError,
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        prefix = "+91 "
                    )

                    // Hostel Name
                    HostelDropdown(
                        selectedHostel = uiState.hostelName,
                        onHostelSelected = onHostelChange,
                        error = uiState.hostelError
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Set Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Password
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.password.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (uiState.passwordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError = uiState.passwordError != null,
                        supportingText = {
                            if (uiState.passwordError != null) {
                                Text(
                                    uiState.passwordError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    "Minimum 6 characters",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                        shape = CompactCardShape,
                        colors = authTextFieldColors()
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
                                tint = if (uiState.confirmPassword.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
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
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                IconButton(onClick = onToggleConfirmVisibility) {
                                    Icon(
                                        imageVector = if (uiState.confirmPasswordVisible)
                                            Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        visualTransformation = if (uiState.confirmPasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError = uiState.confirmPasswordError != null,
                        supportingText = {
                            if (uiState.confirmPasswordError != null) {
                                Text(
                                    uiState.confirmPasswordError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
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
                        shape = CompactCardShape,
                        colors = authTextFieldColors()
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
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
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
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Login link ────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  Helper composables
// ─────────────────────────────────────────────

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
    onNext: (() -> Unit)? = null,
    prefix: String? = null
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
                tint = if (value.isNotEmpty())
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        prefix = prefix?.let { { Text(it) } },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onNext?.invoke() }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = CompactCardShape,
        colors = authTextFieldColors()
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
                    tint = if (selectedHostel.isNotEmpty())
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = error != null,
            supportingText = {
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = CompactCardShape,
            colors = authTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HOSTELS.forEach { hostel ->
                DropdownMenuItem(
                    text = { Text(hostel) },
                    onClick = {
                        onHostelSelected(hostel)
                        expanded = false
                    },
                    leadingIcon = if (hostel == selectedHostel) {
                        {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
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
//  Previews
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