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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val focusManager = LocalFocusManager.current

    // One-shot event collector — same pattern as Humble Contacts
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {

                is LoginEvent.NavigateToHome -> {
                    onLoginSuccess()
                }

                is LoginEvent.NavigateToPhoneSetup -> {
                    onNavigateToPhoneSetup()
                }

                is LoginEvent.LaunchGoogleSignIn -> {
                    // Launch in a child coroutine so the collector is not blocked
                    launch {
                        val activity = context as? Activity ?: return@launch

                        // Use Credential-Manager-based helper from Humble Contacts
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
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility
    )
}

// ─────────────────────────────────────────────
//  Stateless content (Preview-friendly)
// ─────────────────────────────────────────────

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onTogglePasswordVisibility: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ){
                focusManager.clearFocus()
            }
    ) {
        // Crimson arc decoration at top
        Canvas(modifier = Modifier
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
                topLeft = androidx.compose.ui.geometry.Offset(
                    -size.width * 0.1f,
                    -size.height * 0.8f
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.width * 1.2f,
                    size.height * 1.8f
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

            Spacer(modifier = Modifier.height(56.dp))

            // ── Brand Header ──────────────────────────────
            BrandHeader()

            Spacer(modifier = Modifier.height(40.dp))

            // ── Login Card ────────────────────────────────
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Use your @thapar.edu account",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        placeholder = { Text("yourname@thapar.edu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (uiState.email.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        isError = uiState.emailError != null,
                        supportingText = {
                            if (uiState.emailError != null) {
                                Text(
                                    text = uiState.emailError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
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
                        shape = CompactCardShape,
                        colors = authTextFieldColors()
                    )

                    // Password Field
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
                                    contentDescription = if (uiState.passwordVisible)
                                        "Hide password" else "Show password",
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
                                    text = uiState.passwordError,
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
                                onLoginClick()
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = CompactCardShape,
                        colors = authTextFieldColors()
                    )

                    // Global error banner
                    AnimatedVisibility(visible = uiState.generalError != null) {
                        uiState.generalError?.let { error ->
                            ErrorBanner(message = error)
                        }
                    }

                    // Sign In Button
                    Button(
                        onClick = onLoginClick,
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
                        if (uiState.isLoading && uiState.loadingSource == LoadingSource.EMAIL) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // ── Divider ───────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "  or  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    // ── Google Sign-In ─────────────────────
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !uiState.isLoading,
                        shape = PillShape,
                        border = BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        if (uiState.isLoading && uiState.loadingSource == LoadingSource.GOOGLE) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                GoogleLogo(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Register link ─────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "New to Thapar Bites? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  Reusable sub-composables
// ─────────────────────────────────────────────

@Composable
internal fun BrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ti",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Crimson500,
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "THAPAR BITES",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "GOOD FOOD. GREAT CAMPUS.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
            letterSpacing = 1.sp
        )
    }
}

/** Google "G" mark drawn with Canvas — no drawable needed */
@Composable
internal fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        drawArc(
            color = Color(0xFF4285F4), startAngle = -50f, sweepAngle = 130f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.15f),
            topLeft = androidx.compose.ui.geometry.Offset(s * 0.05f, s * 0.05f),
            size = androidx.compose.ui.geometry.Size(s * 0.9f, s * 0.9f)
        )
        drawArc(
            color = Color(0xFFEA4335), startAngle = -170f, sweepAngle = 120f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.15f),
            topLeft = androidx.compose.ui.geometry.Offset(s * 0.05f, s * 0.05f),
            size = androidx.compose.ui.geometry.Size(s * 0.9f, s * 0.9f)
        )
        drawArc(
            color = Color(0xFFFBBC05), startAngle = -50f, sweepAngle = -120f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.15f),
            topLeft = androidx.compose.ui.geometry.Offset(s * 0.05f, s * 0.05f),
            size = androidx.compose.ui.geometry.Size(s * 0.9f, s * 0.9f)
        )
        drawArc(
            color = Color(0xFF34A853), startAngle = 80f, sweepAngle = 100f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.15f),
            topLeft = androidx.compose.ui.geometry.Offset(s * 0.05f, s * 0.05f),
            size = androidx.compose.ui.geometry.Size(s * 0.9f, s * 0.9f)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = androidx.compose.ui.geometry.Offset(s * 0.5f, s * 0.5f),
            end = androidx.compose.ui.geometry.Offset(s * 0.9f, s * 0.5f),
            strokeWidth = s * 0.15f
        )
    }
}

@Composable
fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CompactCardShape)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⚠  $message",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error,
)

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