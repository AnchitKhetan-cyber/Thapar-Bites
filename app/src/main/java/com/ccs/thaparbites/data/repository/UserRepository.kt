package com.ccs.thaparbites.data.repository

import com.ccs.thaparbites.data.dummy.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUser(): UserProfile? {
        val uid = authRepository.currentUserId() ?: return null
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                UserProfile(
                    uid = uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    hostelName = doc.getString("hostelName") ?: ""
                )
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun updateUser(phone: String, hostelName: String): Boolean {
        val uid = authRepository.currentUserId() ?: return false
        return try {
            usersCollection.document(uid)
                .update(mapOf("phone" to phone, "hostelName" to hostelName))
                .await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun createUser(uid: String, name: String, email: String, phone: String, hostelName: String): Boolean {
        return try {
            usersCollection.document(uid).set(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "hostelName" to hostelName,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
            true
        } catch (e: Exception) { false }
    }
}