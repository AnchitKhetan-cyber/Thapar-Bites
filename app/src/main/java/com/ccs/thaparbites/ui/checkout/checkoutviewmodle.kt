package com.ccs.thaparbites.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class CheckoutUiState {
    object Idle    : CheckoutUiState()
    object Loading : CheckoutUiState()
    data class Success(val orderId: String) : CheckoutUiState()
    data class Error(val message: String)   : CheckoutUiState()
}

class CheckoutViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun placeOrder(
        cartItems: List<CartItem>,
        subtotal: Double,
        deliveryFee: Double,
        total: Double,
        paymentMethod: PaymentMethod,
        hostel: String,
        roomNumber: String
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = CheckoutUiState.Error("Not logged in. Please sign in again.")
            return
        }
        if (cartItems.isEmpty()) {
            _uiState.value = CheckoutUiState.Error("Your cart is empty.")
            return
        }

        _uiState.value = CheckoutUiState.Loading

        viewModelScope.launch {
            try {
                val orderData = hashMapOf(
                    "userId"        to uid,
                    "storeName"     to (cartItems.first().menuItem.storeId),
                    "items"         to cartItems.map { ci ->
                        mapOf(
                            "itemId"   to ci.menuItem.id,
                            "name"     to ci.menuItem.name,
                            "emoji"    to ci.menuItem.emoji,
                            "price"    to ci.menuItem.price,
                            "quantity" to ci.quantity
                        )
                    },
                    "subtotal"      to subtotal,
                    "deliveryFee"   to deliveryFee,
                    "total"         to total,
                    "paymentMethod" to paymentMethod.name,
                    "hostel"        to hostel,
                    "roomNumber"    to roomNumber,
                    "status"        to "PLACED",
                    "placedAt"      to FieldValue.serverTimestamp()
                )

                val ref = db.collection("orders").add(orderData).await()
                _uiState.value = CheckoutUiState.Success(ref.id)

            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error(
                    e.message ?: "Failed to place order. Please try again."
                )
            }
        }
    }

    // Manual factory — no Hilt needed, matches your MainActivity pattern
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CheckoutViewModel() as T
    }
}