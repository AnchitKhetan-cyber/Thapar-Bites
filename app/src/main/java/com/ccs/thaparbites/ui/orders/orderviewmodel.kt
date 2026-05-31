package com.ccs.thaparbites.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.Order
import com.ccs.thaparbites.data.dummy.dummyOrders
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<Order>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

class OrdersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            try {
                // Simulate Firestore fetch:
                // val snapshot = firestore.collection("orders")
                //     .whereEqualTo("userId", auth.currentUser?.uid)
                //     .orderBy("placedAt", Query.Direction.DESCENDING)
                //     .get().await()
                // val orders = snapshot.toObjects(Order::class.java)
                delay(600) // simulate network
                _uiState.value = OrdersUiState.Success(dummyOrders.sortedByDescending { it.placedAt })
            } catch (e: Exception) {
                _uiState.value = OrdersUiState.Error(e.message ?: "Could not load orders")
            }
        }
    }

    fun selectOrder(order: Order) {
        _selectedOrder.value = order
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }
}


