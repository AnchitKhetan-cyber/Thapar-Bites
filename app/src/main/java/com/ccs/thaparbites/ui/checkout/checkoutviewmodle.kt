package com.ccs.thaparbites.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.Order
import com.ccs.thaparbites.data.dummy.OrderStatus
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.ccs.thaparbites.data.dummy.Store
import com.ccs.thaparbites.data.dummy.UserProfile
import com.ccs.thaparbites.data.dummy.dummyUser
import com.ccs.thaparbites.data.dummy.dummyUserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object Placing : CheckoutUiState()
    data class Success(val orderId: String) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

data class CheckoutScreenState(
    val cart: List<CartItem> = emptyList(),
    val store: Store? = null,
    val user: UserProfile = dummyUser,
    val selectedPayment: PaymentMethod = PaymentMethod.UPI_ONLY,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 10.0,
    val total: Double = 0.0,
    val uiState: CheckoutUiState = CheckoutUiState.Idle
)

class CheckoutViewModel : ViewModel() {

    private val _state = MutableStateFlow(CheckoutScreenState())
    val state: StateFlow<CheckoutScreenState> = _state.asStateFlow()

    /**
     * Call this from the NavGraph when navigating to Checkout,
     * passing the current cart and the store.
     */
    fun init(cart: List<CartItem>, store: Store) {
        val subtotal = cart.sumOf { it.menuItem.price * it.quantity }
        val total = subtotal + 10.0
        val defaultPayment = store.paymentMethod
        _state.value = CheckoutScreenState(
            cart = cart,
            store = store,
            user = dummyUser,          // replace with Firestore fetch in prod
            selectedPayment = defaultPayment,
            subtotal = subtotal,
            total = total
        )
    }

    fun selectPayment(method: PaymentMethod) {
        _state.value = _state.value.copy(selectedPayment = method)
    }

    /**
     * Places the order.
     * In production: write to Firestore orders/ collection.
     * Returns the orderId via UiState.Success so the caller can navigate.
     */
    fun placeOrder(onUpiIntent: (upiId: String, storeName: String, amount: Double) -> Unit) {
        val s = _state.value
        if (s.store == null) return

        _state.value = s.copy(uiState = CheckoutUiState.Placing)

        viewModelScope.launch {
            try {
                // Simulate network write
                delay(1200)

                if (s.selectedPayment != PaymentMethod.CASH) {
                    val upiId = s.store.upiId ?: "merchant@upi"
                    onUpiIntent(upiId, s.store.name, s.total)
                }

                val orderId = "TB-${UUID.randomUUID().toString().take(8).uppercase()}"

                // TODO: Replace with actual Firestore write:
                // val order = buildOrder(s, orderId)
                // firestore.collection("orders").document(orderId).set(order).await()

                _state.value = s.copy(uiState = CheckoutUiState.Success(orderId))
            } catch (e: Exception) {
                _state.value = s.copy(
                    uiState = CheckoutUiState.Error(e.message ?: "Failed to place order")
                )
            }
        }
    }

    fun dismissError() {
        _state.value = _state.value.copy(uiState = CheckoutUiState.Idle)
    }

    private fun buildOrder(s: CheckoutScreenState, orderId: String): Order {
        return Order(
            id = orderId,
            storeId = s.store!!.id,
            storeName = s.store.name,
            storeEmoji = s.store.emoji,
            userId = s.user.uid,
            items = s.cart,
            subtotal = s.subtotal,
            deliveryFee = s.deliveryFee,
            total = s.total,
            status = OrderStatus.PLACED,
            paymentMethod = s.selectedPayment,
            placedAt = Date()
        )
    }
}