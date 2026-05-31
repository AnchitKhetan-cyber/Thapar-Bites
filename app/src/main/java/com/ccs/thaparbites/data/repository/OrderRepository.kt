package com.ccs.thaparbites.data.repository

import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.Order
import com.ccs.thaparbites.data.dummy.OrderStatus
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    private val ordersCollection = firestore.collection("orders")

    // Real-time listener — use in OrdersViewModel
    fun observeOrders(): Flow<List<Order>> = callbackFlow {
        val uid = authRepository.currentUserId()
        if (uid == null) { trySend(emptyList()); close(); return@callbackFlow }

        val listener = ordersCollection
            .whereEqualTo("userId", uid)
            .orderBy("placedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val orders = snapshot.documents.mapNotNull { doc ->
                    try {
                        Order(
                            id = doc.id,
                            storeName = doc.getString("storeName") ?: "",
                            storeEmoji = doc.getString("storeEmoji") ?: "🍽️",
                            items = emptyList(), // deserialize if needed
                            subtotal = doc.getDouble("subtotal") ?: 0.0,
                            deliveryFee = doc.getDouble("deliveryFee") ?: 0.0,
                            total = doc.getDouble("total") ?: 0.0,
                            status = OrderStatus.valueOf(
                                doc.getString("status") ?: OrderStatus.PLACED.name
                            ),
                            paymentMethod = PaymentMethod.valueOf(
                                doc.getString("paymentMethod") ?: PaymentMethod.CASH.name
                            ),
                            placedAt = (doc.getTimestamp("placedAt")?.toDate()?.toString()) ?: ""
                        )
                    } catch (e: Exception) { null }
                }
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    suspend fun placeOrder(
        cart: List<CartItem>,
        store: com.ccs.thaparbites.data.dummy.Store,
        userProfile: UserProfile,
        paymentMethod: PaymentMethod,
        subtotal: Double,
        deliveryFee: Double,
        total: Double
    ): String? {
        val uid = authRepository.currentUserId() ?: return null
        return try {
            val doc = ordersCollection.add(
                mapOf(
                    "userId" to uid,
                    "storeId" to store.id,
                    "storeName" to store.name,
                    "storeEmoji" to store.emoji,
                    "items" to cart.map {
                        mapOf(
                            "itemId" to it.menuItem.id,
                            "name" to it.menuItem.name,
                            "price" to it.menuItem.price,
                            "quantity" to it.quantity,
                            "emoji" to it.menuItem.emoji
                        )
                    },
                    "subtotal" to subtotal,
                    "deliveryFee" to deliveryFee,
                    "total" to total,
                    "status" to OrderStatus.PLACED.name,
                    "paymentMethod" to paymentMethod.name,
                    "placedAt" to Timestamp.now(),
                    "studentName" to userProfile.name,
                    "studentPhone" to userProfile.phone,
                    "hostelName" to userProfile.hostelName
                )
            ).await()
            doc.id
        } catch (e: Exception) { null }
    }
}