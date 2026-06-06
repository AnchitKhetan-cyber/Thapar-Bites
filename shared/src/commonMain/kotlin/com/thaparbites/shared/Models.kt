package com.thaparbites.shared

import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isVeg: Boolean = true
)

@Serializable
data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val specialNote: String = ""
) {
    val totalPrice: Double get() = menuItem.price * quantity
}

@Serializable
data class Cart(
    val items: List<CartItem> = emptyList(),
    val outletId: String = ""
) {
    val subtotal: Double get() = items.sumOf { it.totalPrice }
    val itemCount: Int get() = items.sumOf { it.quantity }
    val isEmpty: Boolean get() = items.isEmpty()
}

@Serializable
data class Outlet(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val isOpen: Boolean = true,
    val openTime: String = "",
    val closeTime: String = ""
)

@Serializable
data class Order(
    val id: String = "",
    val userId: String = "",
    val outletId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PLACED,
    val createdAt: Long = 0L,
    val specialInstructions: String = ""
)

@Serializable
enum class OrderStatus {
    PLACED, CONFIRMED, PREPARING, READY, PICKED_UP, CANCELLED
}