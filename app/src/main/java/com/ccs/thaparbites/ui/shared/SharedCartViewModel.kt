package com.ccs.thaparbites.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SharedCartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val totalQuantity: Int
        get() = _cartItems.value.sumOf { it.quantity }

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.menuItem.price * it.quantity.toDouble() }

    val deliveryFee: Double
        get() = if (_cartItems.value.isEmpty()) 0.0 else 10.0

    val total: Double
        get() = subtotal + deliveryFee

    fun addItem(menuItem: MenuItem) {
        Log.d("CART_DEBUG", "Adding item: ${menuItem.id}")

        _cartItems.update { current ->
            val existing = current.find { it.menuItem.id == menuItem.id }

            if (existing != null) {
                current.map {
                    if (it.menuItem.id == menuItem.id)
                        it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                current + CartItem(menuItem, 1)
            }
        }

        Log.d("CART_DEBUG", "Cart = ${_cartItems.value}")
    }

    fun removeItem(menuItem: MenuItem) {
        _cartItems.update { current ->
            val existing = current.find { it.menuItem.id == menuItem.id } ?: return@update current
            if (existing.quantity <= 1) current.filter { it.menuItem.id != menuItem.id }
            else current.map {
                if (it.menuItem.id == menuItem.id) it.copy(quantity = it.quantity - 1)
                else it
            }
        }
    }

    fun getQuantity(menuItemId: String): Int =
        _cartItems.value.find { it.menuItem.id == menuItemId }?.quantity ?: 0

    fun clearCart() { _cartItems.value = emptyList() }
}