package com.ccs.thaparbites.data.repository

import com.ccs.thaparbites.data.dummy.MenuItem
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.ccs.thaparbites.data.dummy.Store
import com.ccs.thaparbites.data.dummy.StoreStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CanteenRepository {

    private val db = FirebaseFirestore.getInstance()
    private val canteensCol   = db.collection("canteens")
    private val menuItemsCol  = db.collection("menuItems")

    // ── Canteens → Store ──────────────────────────────────

    /** Real-time stream of all canteens as Store objects */
    fun observeStores(): Flow<List<Store>> = callbackFlow {
        val listener = canteensCol.addSnapshotListener { snap, error ->
            if (error != null || snap == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val stores = snap.documents.mapNotNull { doc ->
                runCatching { doc.toStore() }.getOrNull()
            }
            trySend(stores)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getAllStores(): List<Store> =
        canteensCol.get().await().documents.mapNotNull { doc ->
            runCatching { doc.toStore() }.getOrNull()
        }

    suspend fun getStore(canteenId: String): Store? =
        runCatching {
            canteensCol.document(canteenId).get().await().toStore()
        }.getOrNull()

    // ── Menu Items → MenuItem ─────────────────────────────

    /** Real-time stream of menu items for a canteen */
    fun observeMenuItems(canteenId: String): Flow<List<MenuItem>> = callbackFlow {
        val listener = menuItemsCol
            .whereEqualTo("canteenId", canteenId)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snap.documents.mapNotNull { doc ->
                    runCatching { doc.toMenuItem() }.getOrNull()
                }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getMenuItems(canteenId: String): List<MenuItem> =
        menuItemsCol
            .whereEqualTo("canteenId", canteenId)
            .get().await()
            .documents.mapNotNull { doc ->
                runCatching { doc.toMenuItem() }.getOrNull()
            }

    // ── Firestore doc → Store ─────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toStore(): Store {
        val isOpen  = getBoolean("isOpen") ?: true
        val rating  = getDouble("rating") ?: 0.0
        val opening = getString("openingTime") ?: "08:00"
        val closing = getString("closingTime")  ?: "22:00"

        return Store(
            id            = id,
            name          = getString("name")        ?: "",
            location      = mapFirestoreLocation(getString("location") ?: ""),
            description   = getString("location")    ?: "",   // used as subtitle
            rating        = rating.toFloat(),
            reviewCount   = getLong("totalRatings")?.toInt() ?: 0,
            status        = if (isOpen) StoreStatus.OPEN else StoreStatus.CLOSED,
            etaMinutes    = 10,                               // default ETA
            paymentMethod = PaymentMethod.CASH,               // default
            timings       = "$opening – $closing",
            emoji         = emojiForCanteen(getString("name") ?: ""),
            upiId         = ""
        )
    }

    // ── Firestore doc → MenuItem ──────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toMenuItem(): MenuItem {
        return MenuItem(
            id          = id,
            storeId     = getString("canteenId")   ?: "",
            name        = getString("name")         ?: "",
            description = getString("description")  ?: "",
            price       = getDouble("price")?.toInt() ?: 0,
            category    = getString("category")     ?: "Other",
            isVeg       = getBoolean("isVeg")       ?: true,
            isAvailable = getBoolean("isAvailable") ?: true,
            emoji       = emojiForCategory(getString("category") ?: "")
        )
    }

    // ── Helpers ───────────────────────────────────────────

    /**
     * Map the Firestore location string (e.g. "Near Main Gate, Block A")
     * to one of the campusLocations chips shown in HomeScreen.
     */
    private fun mapFirestoreLocation(firestoreLocation: String): String {
        return when {
            firestoreLocation.contains("Student Activity", ignoreCase = true) -> "Kravings"
            firestoreLocation.contains("Central", ignoreCase = true)          -> "COS"
            firestoreLocation.contains("LT", ignoreCase = true)               -> "G-Block"
            firestoreLocation.contains("Main Gate", ignoreCase = true)        -> "COS"
            else                                                               -> "Aahar"
        }
    }

    private fun emojiForCanteen(name: String): String = when {
        name.contains("Nescafe",  ignoreCase = true) -> "☕"
        name.contains("Subway",   ignoreCase = true) -> "🥪"
        name.contains("Food",     ignoreCase = true) -> "🍱"
        name.contains("Juice",    ignoreCase = true) -> "🥤"
        name.contains("Domino",   ignoreCase = true) -> "🍕"
        else                                          -> "🍴"
    }

    private fun emojiForCategory(category: String): String = when (category) {
        "Beverages" -> "🥤"
        "Snacks"    -> "🥟"
        "Meals"     -> "🍱"
        "Desserts"  -> "🍰"
        else        -> "🍴"
    }
}