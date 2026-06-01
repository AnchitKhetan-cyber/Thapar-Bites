package com.example.thaparbites.repository

import com.example.thaparbites.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // ── Collection references ──────────────────────────────
    private val usersCol      = db.collection("users")
    private val canteensCol   = db.collection("canteens")
    private val menuItemsCol  = db.collection("menuItems")
    private val ordersCol     = db.collection("orders")
    private val reviewsCol    = db.collection("reviews")
    private fun cartDoc(uid: String)          = db.collection("cart").document(uid)
    private fun cartItemsCol(uid: String)     = cartDoc(uid).collection("cartItems")
    private fun orderItemsCol(orderId: String) = ordersCol.document(orderId).collection("orderItems")

    // ════════════════════════════════════════════════════════
    // USERS
    // ════════════════════════════════════════════════════════

    suspend fun createUser(user: User) {
        usersCol.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? =
        usersCol.document(uid).get().await().toObject(User::class.java)

    suspend fun updateFcmToken(uid: String, token: String) {
        usersCol.document(uid).update("fcmToken", token).await()
    }

    // ════════════════════════════════════════════════════════
    // CANTEENS
    // ════════════════════════════════════════════════════════

    /** Real-time list of all open canteens */
    fun observeOpenCanteens(): Flow<List<Canteen>> = callbackFlow {
        val listener = canteensCol
            .whereEqualTo("isOpen", true)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(Canteen::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllCanteens(): List<Canteen> =
        canteensCol.get().await().toObjects(Canteen::class.java)

    suspend fun getCanteen(canteenId: String): Canteen? =
        canteensCol.document(canteenId).get().await().toObject(Canteen::class.java)

    suspend fun setCanteenOpen(canteenId: String, isOpen: Boolean) {
        canteensCol.document(canteenId).update("isOpen", isOpen).await()
    }

    // ════════════════════════════════════════════════════════
    // MENU ITEMS
    // ════════════════════════════════════════════════════════

    /** Real-time menu for a canteen */
    fun observeMenu(canteenId: String): Flow<List<MenuItem>> = callbackFlow {
        val listener = menuItemsCol
            .whereEqualTo("canteenId", canteenId)
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(MenuItem::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun getMenuByCategory(canteenId: String, category: String): List<MenuItem> =
        menuItemsCol
            .whereEqualTo("canteenId", canteenId)
            .whereEqualTo("category", category)
            .whereEqualTo("isAvailable", true)
            .get().await()
            .toObjects(MenuItem::class.java)

    suspend fun addMenuItem(item: MenuItem): String {
        val ref = menuItemsCol.document()
        ref.set(item.copy(itemId = ref.id)).await()
        return ref.id
    }

    suspend fun updateItemAvailability(itemId: String, isAvailable: Boolean) {
        menuItemsCol.document(itemId).update("isAvailable", isAvailable).await()
    }

    // ════════════════════════════════════════════════════════
    // CART
    // ════════════════════════════════════════════════════════

    /** Real-time cart observer */
    fun observeCart(userId: String): Flow<Pair<Cart?, List<CartItem>>> = callbackFlow {
        val cartListener = cartDoc(userId).addSnapshotListener { snap, _ ->
            // ignore — cart header observed separately, items below
        }
        val itemsListener = cartItemsCol(userId)
            .addSnapshotListener { snap, _ ->
                val items = snap?.toObjects(CartItem::class.java) ?: emptyList()
                // Fetch cart header inline
                cartDoc(userId).get().addOnSuccessListener { cartSnap ->
                    trySend(Pair(cartSnap.toObject(Cart::class.java), items))
                }
            }
        awaitClose {
            cartListener.remove()
            itemsListener.remove()
        }
    }

    suspend fun addToCart(userId: String, cart: Cart, item: CartItem) {
        val batch = db.batch()
        // set/overwrite cart header (locks canteen)
        batch.set(cartDoc(userId), cart)
        // add or update cart item
        val itemRef = cartItemsCol(userId).document(item.itemId)
        val existing = itemRef.get().await().toObject(CartItem::class.java)
        if (existing != null) {
            batch.update(itemRef, "quantity", existing.quantity + item.quantity)
        } else {
            batch.set(itemRef, item.copy(cartItemId = item.itemId))
        }
        batch.commit().await()
    }

    suspend fun updateCartItemQuantity(userId: String, itemId: String, quantity: Int) {
        if (quantity <= 0) {
            cartItemsCol(userId).document(itemId).delete().await()
        } else {
            cartItemsCol(userId).document(itemId).update("quantity", quantity).await()
        }
    }

    suspend fun clearCart(userId: String) {
        val batch = db.batch()
        val items = cartItemsCol(userId).get().await()
        items.documents.forEach { batch.delete(it.reference) }
        batch.delete(cartDoc(userId))
        batch.commit().await()
    }

    // ════════════════════════════════════════════════════════
    // ORDERS
    // ════════════════════════════════════════════════════════

    /**
     * Atomically convert cart → order.
     * Creates order doc + orderItems, then clears the cart.
     */
    suspend fun placeOrder(
        order: Order,
        orderItems: List<OrderItem>,
        userId: String
    ): String {
        val batch = db.batch()

        // 1. Create order document
        val orderRef = ordersCol.document()
        val orderId  = orderRef.id
        batch.set(orderRef, order.copy(orderId = orderId))

        // 2. Create order item sub-documents
        orderItems.forEach { item ->
            val itemRef = orderItemsCol(orderId).document()
            batch.set(itemRef, item.copy(orderItemId = itemRef.id))
        }

        // 3. Delete cart items
        val cartItems = cartItemsCol(userId).get().await()
        cartItems.documents.forEach { batch.delete(it.reference) }
        batch.delete(cartDoc(userId))

        batch.commit().await()
        return orderId
    }

    /** Real-time order tracking for a student */
    fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersCol.document(orderId)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(Order::class.java))
            }
        awaitClose { listener.remove() }
    }

    /** Student's order history */
    suspend fun getUserOrders(userId: String): List<Order> =
        ordersCol
            .whereEqualTo("userId", userId)
            .orderBy("placedAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Order::class.java)

    /** Live incoming orders for canteen admin */
    fun observeCanteenOrders(canteenId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCol
            .whereEqualTo("canteenId", canteenId)
            .whereNotEqualTo("status", Order.Status.COMPLETED)
            .orderBy("status")
            .orderBy("placedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(Order::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    /** Update order status (canteen admin) */
    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        estimatedMinutes: Int? = null
    ) {
        val updates = mutableMapOf<String, Any>(
            "status"    to newStatus,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        estimatedMinutes?.let { updates["estimatedReadyTime"] = it }
        if (newStatus == Order.Status.COMPLETED) {
            updates["completedAt"] = com.google.firebase.Timestamp.now()
        }
        ordersCol.document(orderId).update(updates).await()
    }

    suspend fun getOrderItems(orderId: String): List<OrderItem> =
        orderItemsCol(orderId).get().await().toObjects(OrderItem::class.java)

    suspend fun cancelOrder(orderId: String) =
        updateOrderStatus(orderId, Order.Status.CANCELLED)

    // ════════════════════════════════════════════════════════
    // REVIEWS  (optional / future)
    // ════════════════════════════════════════════════════════

    suspend fun addReview(review: Review): String {
        val ref = reviewsCol.document()
        ref.set(review.copy(reviewId = ref.id)).await()
        return ref.id
    }

    suspend fun getReviewsForTarget(targetType: String, targetId: String): List<Review> =
        reviewsCol
            .whereEqualTo("targetType", targetType)
            .whereEqualTo("targetId", targetId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(Review::class.java)
}
