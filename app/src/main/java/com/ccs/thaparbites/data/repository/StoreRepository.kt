package com.ccs.thaparbites.data.repository

import com.ccs.thaparbites.data.dummy.Store
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val firestore:       FirebaseFirestore,
    private val cartRepository:  CartRepository          // to resolve the locked storeId
) {

    // ── Fetch single store by ID (used by checkout) ───────────────────────────

    suspend fun getCurrentStore(): Store? {
        val storeId = cartRepository.getLockedStoreId() ?: return null
        return getStoreById(storeId)
    }

    suspend fun getStoreById(storeId: String): Store? {
        return try {
            val doc = firestore.collection("stores")
                .document(storeId)
                .get()
                .await()
            doc.toObject(Store::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    // ── Fetch all stores (for your store listing screen) ─────────────────────

    suspend fun getAllStores(): List<Store> {
        return try {
            firestore.collection("stores")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Store::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}