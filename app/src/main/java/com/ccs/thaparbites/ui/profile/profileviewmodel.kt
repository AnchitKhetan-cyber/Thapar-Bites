package com.ccs.thaparbites.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.UserProfile
import com.ccs.thaparbites.data.repository.AuthRepository
import com.ccs.thaparbites.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user: StateFlow<UserProfile?> = _user

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    private val _editPhone = MutableStateFlow("")
    val editPhone: StateFlow<String> = _editPhone

    private val _editHostel = MutableStateFlow("")
    val editHostel: StateFlow<String> = _editHostel

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _showSignOutDialog = MutableStateFlow(false)
    val showSignOutDialog: StateFlow<Boolean> = _showSignOutDialog

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError

    init { loadUser() }

    private fun loadUser() {
        viewModelScope.launch {
            _user.value = userRepository.getUser()
            _editPhone.value = _user.value?.phone ?: ""
            _editHostel.value = _user.value?.hostelName ?: ""
        }
    }

    fun startEditing() { _isEditing.value = true }

    fun onPhoneChanged(value: String) {
        _editPhone.value = value
        _phoneError.value = null
    }

    fun onHostelChanged(value: String) { _editHostel.value = value }

    fun saveChanges() {
        val phone = _editPhone.value.trim()
        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            _phoneError.value = "Enter a valid 10-digit phone number"
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            val success = userRepository.updateUser(phone, _editHostel.value)
            if (success) {
                _user.value = _user.value?.copy(phone = phone, hostelName = _editHostel.value)
                _isEditing.value = false
            }
            _isSaving.value = false
        }
    }

    fun cancelEditing() {
        _isEditing.value = false
        _editPhone.value = _user.value?.phone ?: ""
        _editHostel.value = _user.value?.hostelName ?: ""
        _phoneError.value = null
    }

    fun showSignOutDialog() { _showSignOutDialog.value = true }
    fun dismissSignOutDialog() { _showSignOutDialog.value = false }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}