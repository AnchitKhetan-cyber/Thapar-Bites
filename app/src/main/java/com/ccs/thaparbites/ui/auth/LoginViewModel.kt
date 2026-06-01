package com.ccs.thaparbites.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.repository.AuthRepository
import com.ccs.thaparbites.data.repository.AuthRepositoryImpl
import com.ccs.thaparbites.data.repository.AuthResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── UI State ──────────────────────────────────────────────────────────────────

enum class LoadingSource { EMAIL, GOOGLE }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val loadingSource: LoadingSource? = null,
    val passwordVisible: Boolean = false
)

// ── One-shot events (same Channel pattern as Humble Contacts) ─────────────────

sealed class LoginEvent {
    object NavigateToHome : LoginEvent()
    object NavigateToPhoneSetup : LoginEvent()
    object LaunchGoogleSignIn : LoginEvent()
    data class ShowSnackbar(val message: String) : LoginEvent()
}
// ── ViewModel ─────────────────────────────────────────────────────────────────

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── Input handlers ────────────────────────────────────────────────────────

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun dismissError() {
        _uiState.update { it.copy(generalError = null) }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!email.endsWith("@thapar.edu")) return "Only @thapar.edu emails are allowed"
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    // ── Email Login ───────────────────────────────────────────────────────────

    fun loginWithEmail() {
        val state = _uiState.value
        val emailError = validateEmail(state.email.trim())
        val passwordError = validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, loadingSource = LoadingSource.EMAIL, generalError = null)
            }

            val result = authRepository.signInWithEmail(state.email.trim(), state.password)
            _uiState.update { it.copy(isLoading = false, loadingSource = null) }

            when (result) {
                is AuthResult.Success -> checkPhoneAndNavigate()
                is AuthResult.Error   -> _uiState.update { it.copy(generalError = result.message) }
                else                  -> Unit
            }
        }
    }

    // ── Google Sign-In ────────────────────────────────────────────────────────

    fun onGoogleSignInClicked() {
        viewModelScope.launch {
            _events.send(LoginEvent.LaunchGoogleSignIn)
        }
    }

    fun onGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, loadingSource = LoadingSource.GOOGLE, generalError = null)
            }

            val result = authRepository.signInWithGoogle(idToken)
            _uiState.update { it.copy(isLoading = false, loadingSource = null) }

            when (result) {
                is AuthResult.Success -> checkPhoneAndNavigate()
                is AuthResult.Error   -> _uiState.update { it.copy(generalError = result.message) }
                else                  -> Unit
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isLoading = false, loadingSource = null, generalError = message) }
    }

    // LoginViewModel.kt
    fun sendPasswordReset(email: String, onResult: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                Firebase.auth.sendPasswordResetEmail(email).await()
                onResult(true, null)
            } catch (e: FirebaseAuthInvalidUserException) {
                onResult(false, "No account found with this email.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Something went wrong. Try again.")
            }
        }
    }

    private suspend fun checkPhoneAndNavigate() {

        val uid = authRepository.currentUserId() ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val phone = document.getString("phone")

                viewModelScope.launch {

                    if (phone.isNullOrBlank()) {
                        _events.send(LoginEvent.NavigateToPhoneSetup)
                    } else {
                        _events.send(LoginEvent.NavigateToHome)
                    }
                }
            }
    }


    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val auth = AuthRepositoryImpl(FirebaseAuth.getInstance())
            return LoginViewModel(auth) as T
        }
    }
}