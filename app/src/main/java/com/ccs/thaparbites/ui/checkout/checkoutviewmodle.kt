package com.ccs.thaparbites.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.ccs.thaparbites.data.dummy.Store
import com.ccs.thaparbites.data.repository.OrderRepository
import com.ccs.thaparbites.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object Placing : CheckoutUiState()
    data class Success(val orderId: String) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState: StateFlow<CheckoutUiState> = _uiState

    private val _selectedPayment = MutableStateFlow(PaymentMethod.UPI)
    val selectedPayment: StateFlow<PaymentMethod> = _selectedPayment

    val deliveryFee = 10.0

    fun selectPayment(method: PaymentMethod) { _selectedPayment.value = method }

    fun placeOrder(
        cart: List<CartItem>,
        store: Store,
        subtotal: Double,
        onLaunchUpiIntent: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Placing
            val user = userRepository.getUser()
            if (user == null) {
                _uiState.value = CheckoutUiState.Error("Could not load user profile")
                return@launch
            }
            if (_selectedPayment.value == PaymentMethod.UPI) {
                onLaunchUpiIntent?.invoke()
            }
            val orderId = orderRepository.placeOrder(
                cart = cart,
                store = store,
                userProfile = user,
                paymentMethod = _selectedPayment.value,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                total = subtotal + deliveryFee
            )
            if (orderId != null) {
                _uiState.value = CheckoutUiState.Success(orderId)
            } else {
                _uiState.value = CheckoutUiState.Error("Failed to place order. Please try again.")
            }
        }
    }

    fun resetState() { _uiState.value = CheckoutUiState.Idle }
}