package com.ccs.thaparbites.ui.shared

import androidx.lifecycle.ViewModel
import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedCartViewModel @Inject constructor() : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _store = MutableStateFlow<Store?>(null)
    val store: StateFlow<Store?> = _store.asStateFlow()

    val itemCount: Int get() = _cart.value.sumOf { it.quantity }
    val totalPrice: Int get() = _cart.value.sumOf { it.menuItem.price * it.quantity }

    fun setCart(items: List<CartItem>, store: Store) {
        _cart.value = items
        _store.value = store
    }

    fun addItem(item: com.ccs.thaparbites.data.dummy.MenuItem) {
        val current = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.menuItem.id == item.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + 1)
        } else {
            current.add(CartItem(menuItem = item, quantity = 1))
        }
        _cart.value = current
    }

    fun removeItem(item: com.ccs.thaparbites.data.dummy.MenuItem) {
        val current = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.menuItem.id == item.id }
        if (existing >= 0) {
            val newQty = current[existing].quantity - 1
            if (newQty <= 0) current.removeAt(existing)
            else current[existing] = current[existing].copy(quantity = newQty)
        }
        _cart.value = current
    }

    fun clear() {
        _cart.value = emptyList()
        _store.value = null
    }
}