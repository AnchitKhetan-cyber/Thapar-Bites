package com.ccs.thaparbites.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.UserProfile
import com.ccs.thaparbites.data.repository.AuthRepository
import com.ccs.thaparbites.data.repository.AuthRepositoryImpl
import com.ccs.thaparbites.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI state ──────────────────────────────────────────────────────────────────

data class ProfileUiState(
    // All fields have safe defaults — no TODO() anywhere
    val user: UserProfile = UserProfile(),
    val isEditMode: Boolean = false,
    val editPhone: String = "",
    val editHostel: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val showSignOutDialog: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { loadUser() }

    // ── Load ──────────────────────────────────────────────────────────────────

    private fun loadUser() {
        viewModelScope.launch {
            // Graceful fallback: empty UserProfile if Firestore unavailable
            val user = userRepository.getUser() ?: UserProfile()
            _state.update {
                it.copy(
                    user       = user,
                    editPhone  = user.phone,
                    editHostel = user.hostelName
                )
            }
        }
    }

    // ── Edit mode ─────────────────────────────────────────────────────────────

    fun enterEditMode() {
        _state.update {
            it.copy(
                isEditMode  = true,
                editPhone   = it.user.phone,
                editHostel  = it.user.hostelName,
                error       = null,
                saveSuccess = false
            )
        }
    }

    fun exitEditMode() {
        _state.update {
            it.copy(
                isEditMode = false,
                editPhone  = it.user.phone,
                editHostel = it.user.hostelName,
                error      = null
            )
        }
    }

    // ── Field changes ─────────────────────────────────────────────────────────

    fun onPhoneChanged(value: String) {
        _state.update { it.copy(editPhone = value, error = null) }
    }

    fun onHostelChanged(value: String) {
        _state.update { it.copy(editHostel = value) }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveChanges() {
        val phone = _state.value.editPhone.trim()
        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            _state.update { it.copy(error = "Enter a valid 10-digit phone number") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null, saveSuccess = false) }

            val hostel  = _state.value.editHostel
            val success = userRepository.updateUser(phone, hostel)

            if (success) {
                _state.update {
                    it.copy(
                        user        = it.user.copy(phone = phone, hostelName = hostel),
                        isEditMode  = false,
                        isSaving    = false,
                        saveSuccess = true
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error    = "Failed to save. Please try again."
                    )
                }
            }
        }
    }

    // ── Sign-out dialog ───────────────────────────────────────────────────────

    fun showSignOutDialog() { _state.update { it.copy(showSignOutDialog = true) } }

    fun hideSignOutDialog() { _state.update { it.copy(showSignOutDialog = false) } }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firebaseAuth = FirebaseAuth.getInstance()
            val firestore    = FirebaseFirestore.getInstance()
            val authRepo     = AuthRepositoryImpl(firebaseAuth)
            val userRepo     = UserRepository(firestore, authRepo)
            return ProfileViewModel(authRepo, userRepo) as T
        }
    }
}