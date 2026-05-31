package com.ccs.thaparbites.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.Order
import com.ccs.thaparbites.data.repository.AuthRepositoryImpl
import com.ccs.thaparbites.data.repository.CartRepository
import com.ccs.thaparbites.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
    data class Success(val orders: List<Order>) : OrdersUiState()
}

class OrdersViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder

    init { loadOrders() }

    fun loadOrders() {
        _uiState.value = OrdersUiState.Loading
        orderRepository.observeOrders()
            .onEach  { orders -> _uiState.value = OrdersUiState.Success(orders) }
            .catch   { e -> _uiState.value = OrdersUiState.Error(e.message ?: "Failed to load orders") }
            .launchIn(viewModelScope)
    }

    fun selectOrder(order: Order) { _selectedOrder.value = order }
    fun clearSelectedOrder()      { _selectedOrder.value = null }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firebaseAuth = FirebaseAuth.getInstance()
            val firestore    = FirebaseFirestore.getInstance()
            val authRepo     = AuthRepositoryImpl(firebaseAuth)
            val cartRepo     = CartRepository()
            val orderRepo    = OrderRepository(firestore, authRepo)
            return OrdersViewModel(orderRepo) as T
        }
    }
}