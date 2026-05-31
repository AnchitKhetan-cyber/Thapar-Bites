package com.ccs.thaparbites.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun isSessionValid(): Boolean
    fun currentUserId(): String?
    suspend fun signOut()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun isSessionValid(): Boolean = auth.currentUser != null

    override fun currentUserId(): String? = auth.currentUser?.uid

    override suspend fun signOut() {
        auth.signOut()
    }
}