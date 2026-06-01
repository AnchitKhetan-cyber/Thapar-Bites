package com.example.thaparbites.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// ─────────────────────────────────────────────
// Collection: users/{uid}
// ─────────────────────────────────────────────
data class User(
    @DocumentId val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "student",          // "student" | "canteen_admin" | "super_admin"
    val fcmToken: String = "",             // for push notifications
    val profileImageUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    // Roles
    companion object {
        const val ROLE_STUDENT       = "student"
        const val ROLE_CANTEEN_ADMIN = "canteen_admin"
        const val ROLE_SUPER_ADMIN   = "super_admin"
    }
}

// ─────────────────────────────────────────────
// Collection: canteens/{canteenId}
// ─────────────────────────────────────────────
data class Canteen(
    @DocumentId val canteenId: String = "",
    val name: String = "",
    val location: String = "",             // e.g. "Near LT1", "Block C Ground Floor"
    val imageUrl: String = "",
    val isOpen: Boolean = true,
    val openingTime: String = "08:00",     // HH:mm
    val closingTime: String = "22:00",
    val adminUid: String = "",             // uid of canteen_admin user
    val rating: Double = 0.0,             // avg rating (updated via cloud function)
    val totalRatings: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

// ─────────────────────────────────────────────
// Collection: menuItems/{itemId}
// ─────────────────────────────────────────────
data class MenuItem(
    @DocumentId val itemId: String = "",
    val canteenId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",             // "Snacks" | "Meals" | "Beverages" | "Desserts"
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isVeg: Boolean = true,
    val preparationTimeMinutes: Int = 10,
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        const val CATEGORY_SNACKS    = "Snacks"
        const val CATEGORY_MEALS     = "Meals"
        const val CATEGORY_BEVERAGES = "Beverages"
        const val CATEGORY_DESSERTS  = "Desserts"
    }
}

// ─────────────────────────────────────────────
// Collection: cart/{userId}
// Subcollection: cart/{userId}/cartItems/{cartItemId}
// ─────────────────────────────────────────────
data class Cart(
    @DocumentId val userId: String = "",
    val canteenId: String = "",            // cart is locked to one canteen at a time
    val canteenName: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class CartItem(
    @DocumentId val cartItemId: String = "",
    val itemId: String = "",
    val canteenId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val isVeg: Boolean = true,
    @ServerTimestamp val addedAt: Timestamp? = null
) {
    val totalPrice: Double get() = unitPrice * quantity
}

// ─────────────────────────────────────────────
// Collection: orders/{orderId}
// Subcollection: orders/{orderId}/orderItems/{orderItemId}
// ─────────────────────────────────────────────
data class Order(
    @DocumentId val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val canteenId: String = "",
    val canteenName: String = "",
    val status: String = Status.PLACED,
    val totalAmount: Double = 0.0,
    val itemCount: Int = 0,
    val paymentMethod: String = "cash",    // "cash" | "upi"
    val paymentStatus: String = PaymentStatus.PENDING,
    val specialInstructions: String = "",
    val estimatedReadyTime: Int = 0,       // minutes from placement
    @ServerTimestamp val placedAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val completedAt: Timestamp? = null
) {
    object Status {
        const val PLACED     = "placed"       // student placed order
        const val ACCEPTED   = "accepted"     // canteen confirmed
        const val PREPARING  = "preparing"    // being cooked
        const val READY      = "ready"        // ready for pickup
        const val COMPLETED  = "completed"    // student picked up
        const val CANCELLED  = "cancelled"    // cancelled by either party
    }

    object PaymentStatus {
        const val PENDING   = "pending"
        const val PAID      = "paid"
        const val REFUNDED  = "refunded"
    }
}

data class OrderItem(
    @DocumentId val orderItemId: String = "",
    val itemId: String = "",
    val name: String = "",                 // snapshotted at order time
    val imageUrl: String = "",
    val unitPrice: Double = 0.0,           // snapshotted at order time
    val quantity: Int = 1,
    val isVeg: Boolean = true
) {
    val totalPrice: Double get() = unitPrice * quantity
}

// ─────────────────────────────────────────────
// Collection: reviews/{reviewId}
// (optional — for future ratings feature)
// ─────────────────────────────────────────────
data class Review(
    @DocumentId val reviewId: String = "",
    val userId: String = "",
    val userName: String = "",
    val targetType: String = "",           // "canteen" | "item"
    val targetId: String = "",             // canteenId or itemId
    val orderId: String = "",              // which order this review is for
    val rating: Int = 5,                  // 1–5
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
