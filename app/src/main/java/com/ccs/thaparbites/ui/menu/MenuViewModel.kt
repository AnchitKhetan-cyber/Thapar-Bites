package com.ccs.thaparbites.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.MenuItem
import com.ccs.thaparbites.data.dummy.Store
import com.ccs.thaparbites.data.repository.CanteenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────

sealed class MenuUiState {
    object Loading : MenuUiState()
    data class Success(
        val store: Store,
        val menuItems: List<MenuItem>
    ) : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}

// ── ViewModel ─────────────────────────────────────────────

class MenuViewModel(
    private val canteenId: String,
    private val repository: CanteenRepository = CanteenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadMenu()
    }

    private fun loadMenu() {
        viewModelScope.launch {
            try {
                // Load store details first
                val store = repository.getStore(canteenId)
                if (store == null) {
                    _uiState.value = MenuUiState.Error("Canteen not found")
                    return@launch
                }

                // Then stream menu items in real-time
                repository.observeMenuItems(canteenId).collect { items ->
                    _uiState.value = MenuUiState.Success(
                        store     = store,
                        menuItems = items
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MenuUiState.Error(
                    e.message ?: "Failed to load menu"
                )
            }
        }
    }

    fun retry() {
        _uiState.value = MenuUiState.Loading
        loadMenu()
    }

    // Factory — passes canteenId into the VM
    class Factory(private val canteenId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MenuViewModel(canteenId) as T
    }
}