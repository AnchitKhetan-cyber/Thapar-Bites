package com.ccs.thaparbites.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────
//  UI State
// ─────────────────────────────────────────────

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val hostelName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val hostelError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

// ─────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────

class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // ── Input handlers ────────────────────────

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null, generalError = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }

    fun onPhoneChange(value: String) {
        // Allow digits only, max 10
        val digits = value.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phone = digits, phoneError = null, generalError = null) }
    }

    fun onHostelChange(value: String) =
        _uiState.update { it.copy(hostelName = value, hostelError = null, generalError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }

    // ── Validation ────────────────────────────

    private fun validate(state: RegisterUiState): RegisterUiState {
        var s = state
        if (state.name.isBlank())
            s = s.copy(nameError = "Name is required")
        else if (state.name.trim().length < 2)
            s = s.copy(nameError = "Name is too short")

        if (state.email.isBlank())
            s = s.copy(emailError = "Email is required")
        else if (!state.email.endsWith("@thapar.edu"))
            s = s.copy(emailError = "Only @thapar.edu emails are allowed")

        if (state.phone.isBlank())
            s = s.copy(phoneError = "Phone number is required")
        else if (state.phone.length != 10)
            s = s.copy(phoneError = "Enter a valid 10-digit number")

        if (state.hostelName.isBlank())
            s = s.copy(hostelError = "Please select your hostel")

        if (state.password.isBlank())
            s = s.copy(passwordError = "Password is required")
        else if (state.password.length < 6)
            s = s.copy(passwordError = "Minimum 6 characters")

        if (state.confirmPassword.isBlank())
            s = s.copy(confirmPasswordError = "Please confirm your password")
        else if (state.confirmPassword != state.password)
            s = s.copy(confirmPasswordError = "Passwords do not match")

        return s
    }

    private fun RegisterUiState.hasErrors() =
        nameError != null || emailError != null || phoneError != null ||
                hostelError != null || passwordError != null || confirmPasswordError != null

    // ── Register ──────────────────────────────

    fun register(onSuccess: () -> Unit) {
        val validated = validate(_uiState.value)
        _uiState.value = validated
        if (validated.hasErrors()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                // 1. Create Firebase Auth user
                val result = auth.createUserWithEmailAndPassword(
                    validated.email.trim(),
                    validated.password
                ).await()

                val uid = result.user?.uid
                    ?: throw Exception("User creation failed")

                // 2. Send email verification
                result.user?.sendEmailVerification()

                // 3. Save profile to Firestore
                val userProfile = hashMapOf(
                    "uid" to uid,
                    "name" to validated.name.trim(),
                    "email" to validated.email.trim().lowercase(),
                    "phone" to validated.phone,
                    "hostelName" to validated.hostelName,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                firestore.collection("users")
                    .document(uid)
                    .set(userProfile)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = mapFirebaseError(e.message)
                    )
                }
            }
        }
    }

    // ── Error mapping ─────────────────────────

    private fun mapFirebaseError(message: String?): String {
        return when {
            message == null -> "Something went wrong. Please try again."
            "email address is already in use" in message.lowercase() ->
                "An account with this email already exists."
            "badly formatted" in message.lowercase() ->
                "Invalid email address."
            "network" in message.lowercase() ->
                "Network error. Check your connection."
            "weak password" in message.lowercase() ->
                "Password is too weak. Use at least 6 characters."
            else -> "Registration failed. Please try again."
        }
    }
}

