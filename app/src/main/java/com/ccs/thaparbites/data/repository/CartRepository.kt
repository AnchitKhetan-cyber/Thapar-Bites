package com.ccs.thaparbites.data.repository

import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // The storeId is locked once the first item is added.
    // Adding an item from a different store clears the cart first.
    private var lockedStoreId: String? = null

    // ── Read ─────────────────────────────────────────────────────────────────

    fun getActiveCart(): List<CartItem> = _cartItems.value

    fun getLockedStoreId(): String? = lockedStoreId

    fun itemCount(): Int = _cartItems.value.sumOf { it.quantity }

    fun subtotal(): Int = _cartItems.value.sumOf { it.menuItem.price * it.quantity }

    // ── Write ────────────────────────────────────────────────────────────────

    fun addItem(menuItem: MenuItem, storeId: String) {
        // If user switches store, clear the old cart automatically
        if (lockedStoreId != null && lockedStoreId != storeId) {
            clearCart()
        }
        lockedStoreId = storeId

        _cartItems.update { current ->
            val existing = current.indexOfFirst { it.menuItem.id == menuItem.id }
            if (existing >= 0) {
                current.toMutableList().also {
                    it[existing] = it[existing].copy(quantity = it[existing].quantity + 1)
                }
            } else {
                current + CartItem(menuItem = menuItem, quantity = 1)
            }
        }
    }

    fun removeItem(menuItemId: String) {
        _cartItems.update { current ->
            val updated = current.toMutableList()
            val index   = updated.indexOfFirst { it.menuItem.id == menuItemId }
            if (index >= 0) {
                val item = updated[index]
                if (item.quantity > 1) {
                    updated[index] = item.copy(quantity = item.quantity - 1)
                } else {
                    updated.removeAt(index)
                }
            }
            if (updated.isEmpty()) lockedStoreId = null
            updated
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        lockedStoreId    = null
    }
}