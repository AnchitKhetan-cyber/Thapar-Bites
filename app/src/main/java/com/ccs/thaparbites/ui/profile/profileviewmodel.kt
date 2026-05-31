package com.ccs.thaparbites.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.UserProfile
import com.ccs.thaparbites.data.dummy.dummyUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileScreenState(
    val user: UserProfile = dummyUser,
    val editPhone: String = "",
    val editHostel: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileScreenState())
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            // TODO: Replace with Firestore fetch:
            // val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            // val doc = firestore.collection("users").document(uid).get().await()
            // val user = doc.toObject(UserProfile::class.java) ?: return@launch
            delay(400)
            val user = dummyUser
            _state.update { it.copy(user = user, editPhone = user.phone, editHostel = user.hostelName) }
        }
    }

    fun enterEditMode() {
        _state.update { it.copy(isEditMode = true, editPhone = it.user.phone, editHostel = it.user.hostelName) }
    }

    fun exitEditMode() {
        _state.update { it.copy(isEditMode = false, error = null) }
    }

    fun onPhoneChanged(phone: String) {
        _state.update { it.copy(editPhone = phone.filter { c -> c.isDigit() }.take(10)) }
    }

    fun onHostelChanged(hostel: String) {
        _state.update { it.copy(editHostel = hostel) }
    }

    fun saveChanges() {
        val s = _state.value
        if (s.editPhone.length != 10) {
            _state.update { it.copy(error = "Phone number must be 10 digits") }
            return
        }
        if (s.editHostel.isBlank()) {
            _state.update { it.copy(error = "Hostel cannot be empty") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                delay(800) // simulate network
                // TODO: Firestore update:
                // firestore.collection("users").document(uid).update(
                //     "phone" to editPhone,
                //     "hostelName" to editHostel
                // ).await()
                val updated = s.user.copy(phone = s.editPhone, hostelName = s.editHostel)
                _state.update { it.copy(user = updated, isSaving = false, isEditMode = false, saveSuccess = true) }
                delay(2000)
                _state.update { it.copy(saveSuccess = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Save failed") }
            }
        }
    }

    fun showSignOutDialog() = _state.update { it.copy(showSignOutDialog = true) }
    fun hideSignOutDialog() = _state.update { it.copy(showSignOutDialog = false) }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            // FirebaseAuth.getInstance().signOut()
            // GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().await()
            _state.update { it.copy(showSignOutDialog = false) }
            onSignedOut()
        }
    }
}

