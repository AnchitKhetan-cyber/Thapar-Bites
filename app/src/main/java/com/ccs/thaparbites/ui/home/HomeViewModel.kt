package com.ccs.thaparbites.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.Store
import com.ccs.thaparbites.data.repository.CanteenRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val stores: List<Store>, val userName: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// ── ViewModel ─────────────────────────────────────────────

class HomeViewModel(
    private val repository: CanteenRepository = CanteenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStores()
    }

    private fun loadStores() {
        viewModelScope.launch {
            try {
                // Get current user's display name from Firebase Auth
                val userName = FirebaseAuth.getInstance().currentUser?.displayName
                    ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
                    ?: "Student"

                // Collect real-time store updates
                repository.observeStores().collect { stores ->
                    _uiState.value = HomeUiState.Success(
                        stores   = stores,
                        userName = userName
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Failed to load canteens"
                )
            }
        }
    }

    fun retry() {
        _uiState.value = HomeUiState.Loading
        loadStores()
    }

    // Factory so MainActivity can instantiate without Hilt on this VM
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel() as T
    }
}