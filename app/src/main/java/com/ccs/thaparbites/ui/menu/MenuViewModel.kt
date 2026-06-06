package com.ccs.thaparbites.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thaparbites.shared.MenuItem   // ← changed
import com.thaparbites.shared.Canteen    // ← changed (was Store)
import com.ccs.thaparbites.data.repository.CanteenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MenuUiState {
    object Loading : MenuUiState()
    data class Success(
        val canteen: Canteen,        // ← was store: Store
        val menuItems: List<MenuItem>
    ) : MenuUiState()
    data class Error(val message: String) : MenuUiState()
}

class MenuViewModel(
    private val canteenId: String,
    private val repository: CanteenRepository = CanteenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init { loadMenu() }

    private fun loadMenu() {
        viewModelScope.launch {
            try {
                val canteen = repository.getStore(canteenId)  // repository call unchanged
                if (canteen == null) {
                    _uiState.value = MenuUiState.Error("Canteen not found")
                    return@launch
                }

                repository.observeMenuItems(canteenId).collect { items ->
                    _uiState.value = MenuUiState.Success(
                        canteen   = canteen,   // ← was store = store
                        menuItems = items
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MenuUiState.Error(e.message ?: "Failed to load menu")
            }
        }
    }

    fun retry() {
        _uiState.value = MenuUiState.Loading
        loadMenu()
    }

    class Factory(private val canteenId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MenuViewModel(canteenId) as T
    }
}