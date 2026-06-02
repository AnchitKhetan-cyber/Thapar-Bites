package com.ccs.thaparbites.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.R
import com.ccs.thaparbites.ui.components.GlassCard
import com.ccs.thaparbites.ui.components.glassTextFieldColors
import com.ccs.thaparbites.ui.theme.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToPhoneSetup: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onLoginSuccess()
                is LoginEvent.NavigateToPhoneSetup -> onNavigateToPhoneSetup()
                is LoginEvent.LaunchGoogleSignIn -> {
                    launch {
                        val activity = context as? Activity ?: return@launch
                        val helper = GoogleSignInHelper(activity)
                        when (val result = helper.signIn()) {
                            is GoogleSignInHelper.GoogleSignInResult.Success ->
                                viewModel.onGoogleIdToken(result.idToken)
                            is GoogleSignInHelper.GoogleSignInResult.Error ->
                                viewModel.onGoogleSignInError(result.message)
                            is GoogleSignInHelper.GoogleSignInResult.Cancelled -> Unit
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::loginWithEmail,
        onGoogleSignInClick = viewModel::onGoogleSignInClicked,
        onNavigateToRegister = onNavigateToRegister,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onSendPasswordReset = { email, onResult ->
            viewModel.sendPasswordReset(email, onResult)
        }
    )
}

// ─────────────────────────────────────────────
//  Stateless content
// ─────────────────────────────────────────────

@Preview
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onTogglePasswordVisibility: () -> Unit = {},
    onSendPasswordReset: (email: String, onResult: (Boolean, String?) -> Unit) -> Unit = { _, _ -> }
) {
    val focusManager = LocalFocusManager.current
    var showForgotPassword by remember { mutableStateOf(false) }

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

        // ── 2. Gradient scrim — dark at top, heavier at bottom ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.55f),
                            0.38f to Color.Black.copy(alpha = 0.30f),
                            0.62f to Color.Black.copy(alpha = 0.45f),
                            1.0f to Color.Black.copy(alpha = 0.82f)
                        )
                    )
                )
        )

        // ── 3. Content ──────────────────────────────────────
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
                    Text(
                        text = "Welcome back 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Sign in with your @thapar.edu account",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Email
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        placeholder = { Text("yourname@thapar.edu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        isError = uiState.emailError != null,
                        supportingText = {
                            if (uiState.emailError != null)
                                Text(uiState.emailError, color = Crimson500)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
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
                                    contentDescription = if (uiState.passwordVisible)
                                        "Hide password" else "Show password",
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
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLoginClick()
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = glassTextFieldColors()
                    )

                    // Forgot password
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "Forgot password?",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Crimson500,
                            modifier = Modifier.clickable { showForgotPassword = true }
                        )
                    }

                    AnimatedVisibility(visible = uiState.generalError != null) {
                        uiState.generalError?.let { ErrorBanner(message = it) }
                    }

                    // Sign In button
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Crimson500,
                            contentColor = Color.White,
                            disabledContainerColor = Crimson200
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (uiState.isLoading && uiState.loadingSource == LoadingSource.EMAIL) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Charcoal200
                        )
                        Text(
                            text = "  or  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Charcoal500,
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Charcoal200
                        )
                    }

                    // Google button
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.30f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isLoading &&
                            uiState.loadingSource == LoadingSource.GOOGLE
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Continue with Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign up row — white text on the image background
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "New to Thapar Bites? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.90f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onNavigateToRegister)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        if (showForgotPassword) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPassword = false },
                onSend = { email, onResult -> onSendPasswordReset(email, onResult) }
            )
        }
    }
}

// ─────────────────────────────────────────────
//  ForgotPasswordDialog — unchanged
// ─────────────────────────────────────────────

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSend: (email: String, onResult: (success: Boolean, error: String?) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val isValidThaparEmail = email.trim().endsWith("@thapar.edu")

    fun attemptSend() {
        when {
            email.isBlank() -> emailError = "Email is required"
            !isValidThaparEmail -> emailError = "Only @thapar.edu emails are allowed"
            else -> {
                isLoading = true
                emailError = null
                onSend(email.trim()) { success, error ->
                    isLoading = false
                    if (success) sent = true else emailError = error
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Reset password",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter your @thapar.edu email and we'll send a reset link.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Charcoal600
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = { Text("Email") },
                    placeholder = { Text("yourname@thapar.edu") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Charcoal400)
                    },
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) Text(emailError!!, color = Crimson500)
                    },
                    singleLine = true,
                    enabled = !sent && !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(onSend = { attemptSend() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = swiggyTextFieldColors()
                )
                AnimatedVisibility(visible = sent) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFD4F5E4))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF1DA462),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Reset link sent! Check your inbox.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1DA462),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (sent) onDismiss() else attemptSend() },
                shape = RoundedCornerShape(50),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Crimson500)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                } else {
                    Text(if (sent) "Done" else "Send link", fontWeight = FontWeight.ExtraBold)
                }
            }
        },
        dismissButton = {
            if (!sent) {
                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text("Cancel", color = Charcoal600)
                }
            }
        }
    )
}

// ─────────────────────────────────────────────
//  Sub-composables
// ─────────────────────────────────────────────

/**
 * Brand header rendered over the campus image.
 * Uses white text + a semi-transparent pill background for legibility.
 */
@Composable
internal fun BrandHeaderOnImage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Logo mark — white card with crimson text
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ti",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Crimson500
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "THAPAR BITES",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Subtle pill tag line
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.18f)
        ) {
            Text(
                text = "GOOD FOOD · GREAT CAMPUS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }
    }
}

// GoogleLogo, ErrorBanner, swiggyTextFieldColors
// (GoogleLogo and ErrorBanner keep their existing implementations;
//  authTextFieldColors renamed to swiggyTextFieldColors for clarity)

@Composable
internal fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.15f)
        val oval = androidx.compose.ui.geometry.Offset(s * 0.05f, s * 0.05f)
        val ovalSize = androidx.compose.ui.geometry.Size(s * 0.9f, s * 0.9f)
        drawArc(color = Color(0xFF4285F4), startAngle = -50f, sweepAngle = 130f, useCenter = false, style = stroke, topLeft = oval, size = ovalSize)
        drawArc(color = Color(0xFFEA4335), startAngle = -170f, sweepAngle = 120f, useCenter = false, style = stroke, topLeft = oval, size = ovalSize)
        drawArc(color = Color(0xFFFBBC05), startAngle = -50f, sweepAngle = -120f, useCenter = false, style = stroke, topLeft = oval, size = ovalSize)
        drawArc(color = Color(0xFF34A853), startAngle = 80f, sweepAngle = 100f, useCenter = false, style = stroke, topLeft = oval, size = ovalSize)
        drawLine(color = Color(0xFF4285F4), start = androidx.compose.ui.geometry.Offset(s * 0.5f, s * 0.5f), end = androidx.compose.ui.geometry.Offset(s * 0.9f, s * 0.5f), strokeWidth = s * 0.15f)
    }
}

@Composable
fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Crimson50)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⚠  $message",
            style = MaterialTheme.typography.bodySmall,
            color = Crimson600,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun swiggyTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Crimson500,
    focusedLabelColor = Crimson500,
    cursorColor = Crimson500,
    unfocusedBorderColor = Charcoal200,
    unfocusedContainerColor = Charcoal50,
    focusedContainerColor = Crimson50,
    errorBorderColor = Crimson500,
    errorLabelColor = Crimson500,
)

// Keep old name as alias so RegisterScreen still compiles
@Composable
fun authTextFieldColors() = swiggyTextFieldColors()

// ─────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────

@Preview(showBackground = true, name = "Login – Light")
@Composable
private fun LoginPreviewLight() {
    ThaparBitesTheme(darkTheme = false) {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {}, onPasswordChange = {},
            onLoginClick = {}, onGoogleSignInClick = {}, onNavigateToRegister = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Login – Dark"
)
@Composable
private fun LoginPreviewDark() {
    ThaparBitesTheme(darkTheme = true) {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {}, onPasswordChange = {},
            onLoginClick = {}, onGoogleSignInClick = {}, onNavigateToRegister = {}
        )
    }
}