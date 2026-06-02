package com.ccs.thaparbites.ui.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.ui.auth.authTextFieldColors
import com.ccs.thaparbites.ui.home.HomeBottomBar
import com.ccs.thaparbites.ui.theme.Crimson500
import com.ccs.thaparbites.ui.theme.ThaparBitesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateHome: () -> Unit,
    onNavigateOrders: () -> Unit,
    onNavigateExpenses: () -> Unit,
    onSignedOut: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    val focusManager = LocalFocusManager.current

    // Show success snackbar
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) snackbarHostState.showSnackbar("Profile updated ✓")
    }

    Scaffold(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ){
            focusManager.clearFocus()
        },
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    if (!state.isEditMode) {
                        IconButton(onClick = { viewModel.enterEditMode() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { viewModel.showSignOutDialog() }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Sign Out", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Crimson500,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            HomeBottomBar(
                currentRoute = "profile",
                onHomeClick = onNavigateHome,
                onOrdersClick = onNavigateOrders,
                onExpensesClick = onNavigateExpenses,
                onProfileClick = {}
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Avatar header ──────────────────────────────────────────────
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Initials circle
                val initials = state.user.name
                    .split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString("")

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Crimson500),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    state.user.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    state.user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Error banner ───────────────────────────────────────────────
            AnimatedVisibility(visible = state.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠️ ${state.error}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // ── Info section ───────────────────────────────────────────────
            ProfileSectionCard(title = "Account Info") {
                AnimatedContent(
                    targetState = state.isEditMode,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }
                ) { editing ->
                    if (!editing) {
                        // View mode
                        Column {
                            ProfileRow(label = "Name", value = state.user.name)
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            ProfileRow(label = "Email", value = state.user.email)
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            ProfileRow(label = "Phone", value = "+91 ${state.user.phone}")
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            ProfileRow(label = "Hostel", value = state.user.hostelName)
                        }
                    } else {
                        // Edit mode
                        Column {
                            // Non-editable
                            ProfileRow(label = "Name", value = state.user.name)
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            ProfileRow(label = "Email", value = state.user.email)
                            Spacer(Modifier.height(12.dp))

                            // Phone
                            OutlinedTextField(
                                value = state.editPhone,
                                onValueChange = { viewModel.onPhoneChanged(it) },
                                label = { Text("Phone") },
                                prefix = { Text("+91 ") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),

                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))

                            // Hostel dropdown (reuse RegisterScreen's HostelDropdown if desired)
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = {
                                    expanded = !expanded
                                }
                            ) {

                                OutlinedTextField(
                                    value = state.editHostel,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    label = {
                                        Text("Hostel")
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
                                                viewModel.onHostelChanged(hostelName)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(
                                    onClick = { viewModel.exitEditMode() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = { viewModel.saveChanges() },
                                    enabled = !state.isSaving,
                                    colors = ButtonDefaults.buttonColors(containerColor = Crimson500),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (state.isSaving) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Check, contentDescription = null,
                                                modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Save", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── App info ───────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Text(
                "Thapar Bites • v1.0.0",  // Replace BuildConfig.VERSION_NAME in real build
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Sign-out confirmation dialog ───────────────────────────────────────
    if (state.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideSignOutDialog() },
            title = { Text("Sign out?") },
            text = { Text("You'll be returned to the login screen.") },
            confirmButton = {
                TextButton(onClick = { viewModel.signOut(onSignedOut) }) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideSignOutDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileSectionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Profile Light")
@Composable
private fun ProfilePreviewLight() {
    ThaparBitesTheme(darkTheme = false) {
        ProfileScreen(
            onNavigateHome = {},
            onNavigateOrders = {},
            onNavigateExpenses = {},
            onSignedOut = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfilePreviewDark() {
    ThaparBitesTheme(darkTheme = true) {
        ProfileScreen(
            onNavigateHome = {},
            onNavigateOrders = {},
            onNavigateExpenses = {},
            onSignedOut = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile Edit Mode")
@Composable
private fun ProfilePreviewEdit() {
    ThaparBitesTheme(darkTheme = false) {
        // To preview edit mode, set isEditMode via ViewModel or pass a fake state
        ProfileScreen(
            onNavigateHome = {},
            onNavigateOrders = {},
            onNavigateExpenses = {},
            onSignedOut = {}
        )
    }
}

