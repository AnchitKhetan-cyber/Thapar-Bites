package com.ccs.thaparbites.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.repository.AuthRepository
import com.ccs.thaparbites.data.repository.AuthRepositoryImpl
import com.ccs.thaparbites.data.repository.AuthResult
import com.ccs.thaparbites.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val hostelName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    // Inline field errors
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val hostelError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    // Global
    val generalError: String? = null,
    val isLoading: Boolean = false
)

// ── One-shot events ───────────────────────────────────────────────────────────

sealed class RegisterEvent {
    object NavigateToHome : RegisterEvent()
    data class ShowSnackbar(val message: String) : RegisterEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── Input handlers ────────────────────────────────────────────────────────

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null, generalError = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phone = digits, phoneError = null, generalError = null) }
    }

    fun onHostelChange(value: String) =
        _uiState.update { it.copy(hostelName = value, hostelError = null, generalError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }

    fun togglePasswordVisibility() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun toggleConfirmPasswordVisibility() =
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }

    fun dismissError() =
        _uiState.update { it.copy(generalError = null) }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validate(s: RegisterUiState): RegisterUiState {
        var result = s
        if (s.name.isBlank())
            result = result.copy(nameError = "Name is required")
        else if (s.name.trim().length < 2)
            result = result.copy(nameError = "Name is too short")

        if (s.email.isBlank())
            result = result.copy(emailError = "Email is required")
        else if (!s.email.endsWith("@thapar.edu"))
            result = result.copy(emailError = "Only @thapar.edu emails are allowed")

        if (s.phone.isBlank())
            result = result.copy(phoneError = "Phone number is required")
        else if (s.phone.length != 10)
            result = result.copy(phoneError = "Enter a valid 10-digit number")

        if (s.hostelName.isBlank())
            result = result.copy(hostelError = "Please select your hostel")

        if (s.password.isBlank())
            result = result.copy(passwordError = "Password is required")
        else if (s.password.length < 6)
            result = result.copy(passwordError = "Minimum 6 characters")

        if (s.confirmPassword.isBlank())
            result = result.copy(confirmPasswordError = "Please confirm your password")
        else if (s.confirmPassword != s.password)
            result = result.copy(confirmPasswordError = "Passwords do not match")

        return result
    }

    private fun RegisterUiState.hasErrors() =
        nameError != null || emailError != null || phoneError != null ||
                hostelError != null || passwordError != null || confirmPasswordError != null

    // ── Register ──────────────────────────────────────────────────────────────

    fun register() {
        val validated = validate(_uiState.value)
        _uiState.value = validated
        if (validated.hasErrors()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val result = authRepository.registerWithEmail(
                validated.email.trim(),
                validated.password
            )

            when (result) {
                is AuthResult.Success -> {
                    // Save user profile to Firestore via UserRepository
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        userRepository.createUser(
                            uid        = uid,
                            name       = validated.name.trim(),
                            email      = validated.email.trim().lowercase(),
                            phone      = validated.phone,
                            hostelName = validated.hostelName
                        )
                        // Also send email verification
                        com.google.firebase.auth.FirebaseAuth.getInstance()
                            .currentUser?.sendEmailVerification()
                    }
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, generalError = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firebaseAuth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val authRepo = AuthRepositoryImpl(firebaseAuth)
            val userRepo = UserRepository(firestore, authRepo)
            return RegisterViewModel(authRepo, userRepo) as T
        }
    }
}