package com.ccs.thaparbites.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────
//  UI State
// ─────────────────────────────────────────────

enum class LoadingSource { EMAIL, GOOGLE }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val loadingSource: LoadingSource? = null
)

// ─────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────

class LoginViewModel : ViewModel() {

    // FIX 1: lazy init — FirebaseAuth.getInstance() is no longer called
    // during ViewModel construction, avoiding blocking the main thread.
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ── Input handlers ────────────────────────

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    // ── Validation ────────────────────────────

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

    // ── Email/Password Login ──────────────────

    fun loginWithEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingSource = LoadingSource.EMAIL,
                    generalError = null
                )
            }
            try {
                auth.signInWithEmailAndPassword(state.email.trim(), state.password).await()
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingSource = null,
                        generalError = mapFirebaseError(e.message)
                    )
                }
            }
        }
    }

    // ── Google Sign-In ────────────────────────
    // FIX 2: No longer sets isLoading when idToken is null.
    // Previously, returning null early left the loading spinner stuck forever
    // if the user cancelled the Google account picker.
    // Now loading only starts once we actually have a token to exchange.

    fun loginWithGoogle(onSuccess: () -> Unit, idToken: String? = null) {
        if (idToken == null) {
            // Signal UI to launch Google Sign-In launcher — do NOT touch loading state here.
            // The launcher will call back with a token (or call onGoogleSignInFailed).
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingSource = LoadingSource.GOOGLE,
                    generalError = null
                )
            }
            try {
                val result = auth.signInWithCredential(credential).await()
                val email = result.user?.email.orEmpty()
                if (!email.endsWith("@thapar.edu")) {
                    auth.signOut()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingSource = null,
                            generalError = "Only @thapar.edu Google accounts are allowed"
                        )
                    }
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingSource = null,
                        generalError = mapFirebaseError(e.message)
                    )
                }
            }
        }
    }

    fun onGoogleSignInFailed() {
        _uiState.update {
            it.copy(
                isLoading = false,
                loadingSource = null,
                generalError = "Google Sign-In failed. Try again."
            )
        }
    }

    // ── Error mapping ─────────────────────────

    private fun mapFirebaseError(message: String?): String {
        return when {
            message == null -> "Something went wrong. Please try again."
            "no user record" in message.lowercase() -> "No account found with this email."
            "password is invalid" in message.lowercase() -> "Incorrect password."
            "badly formatted" in message.lowercase() -> "Invalid email address."
            "network" in message.lowercase() -> "Network error. Check your connection."
            "blocked" in message.lowercase() -> "Too many attempts. Try again later."
            else -> "Sign-in failed. Please try again."
        }
    }
}