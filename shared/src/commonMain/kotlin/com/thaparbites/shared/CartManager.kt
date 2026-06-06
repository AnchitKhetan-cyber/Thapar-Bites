package com.thaparbites.shared

class CartManager {

    private val _items = mutableListOf<CartItem>()

    val items: List<CartItem> get() = _items.toList()
    val subtotal: Double get() = _items.sumOf { it.totalPrice }
    val itemCount: Int get() = _items.sumOf { it.quantity }
    val isEmpty: Boolean get() = _items.isEmpty()

    fun addItem(menuItem: MenuItem, quantity: Int = 1): Cart {
        val existing = _items.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existing >= 0) {
            _items[existing] = _items[existing].copy(
                quantity = _items[existing].quantity + quantity
            )
        } else {
            _items.add(CartItem(menuItem = menuItem, quantity = quantity))
        }
        return toCart()
    }

    fun removeItem(menuItemId: String): Cart {
        _items.removeAll { it.menuItem.id == menuItemId }
        return toCart()
    }

    fun updateQuantity(menuItemId: String, quantity: Int): Cart {
        if (quantity <= 0) return removeItem(menuItemId)
        val index = _items.indexOfFirst { it.menuItem.id == menuItemId }
        if (index >= 0) _items[index] = _items[index].copy(quantity = quantity)
        return toCart()
    }

    fun clearCart(): Cart {
        _items.clear()
        return toCart()
    }

    fun getQuantity(menuItemId: String): Int =
        _items.find { it.menuItem.id == menuItemId }?.quantity ?: 0

    private fun toCart() = Cart(items = _items.toList())
}

// Pure utility — no Android or iOS imports
object PriceFormatter {
    fun format(price: Double): String = "₹${"%.0f".format(price)}"
}

object Validator {
    fun isValidPhone(phone: String): Boolean =
        phone.length == 10 && phone.all { it.isDigit() }
    fun isValidName(name: String): Boolean = name.trim().length >= 2
}

fun OrderStatus.displayName(): String = when (this) {
    OrderStatus.PLACED     -> "Order Placed"
    OrderStatus.CONFIRMED  -> "Confirmed"
    OrderStatus.PREPARING  -> "Preparing"
    OrderStatus.READY      -> "Ready for Pickup"
    OrderStatus.PICKED_UP  -> "Picked Up"
    OrderStatus.CANCELLED  -> "Cancelled"
}