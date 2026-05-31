package com.ccs.thaparbites.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ── Result wrapper (from Humble Contacts) ────────────────────────────────────

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

// ── Interface ─────────────────────────────────────────────────────────────────

interface AuthRepository {
    fun isSessionValid(): Boolean
    fun currentUserId(): String?
    suspend fun signOut()
    suspend fun signInWithEmail(email: String, password: String): AuthResult<Unit>
    suspend fun registerWithEmail(email: String, password: String): AuthResult<Unit>
    suspend fun signInWithGoogle(idToken: String): AuthResult<Unit>
}

// ── Implementation ────────────────────────────────────────────────────────────

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun isSessionValid(): Boolean = auth.currentUser != null

    override fun currentUserId(): String? = auth.currentUser?.uid

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(Unit)
        }.getOrElse { AuthResult.Error(mapError(it)) }

    override suspend fun registerWithEmail(email: String, password: String): AuthResult<Unit> =
        runCatching {
            auth.createUserWithEmailAndPassword(email, password).await()
            AuthResult.Success(Unit)
        }.getOrElse { AuthResult.Error(mapError(it)) }

    override suspend fun signInWithGoogle(idToken: String): AuthResult<Unit> =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            // Enforce @thapar.edu domain for Google Sign-In
            val email = result.user?.email.orEmpty()
            if (!email.endsWith("@thapar.edu")) {
                auth.signOut()
                AuthResult.Error("Only @thapar.edu Google accounts are allowed")
            } else {
                AuthResult.Success(Unit)
            }
        }.getOrElse { AuthResult.Error(mapError(it)) }

    private fun mapError(e: Throwable): String = when {
        e.message == null -> "Something went wrong. Please try again."
        "no user record" in e.message!!.lowercase() -> "No account found with this email."
        "password is invalid" in e.message!!.lowercase() -> "Incorrect password."
        "badly formatted" in e.message!!.lowercase() -> "Invalid email address."
        "email address is already in use" in e.message!!.lowercase() ->
            "An account with this email already exists."
        "network" in e.message!!.lowercase() -> "Network error. Check your connection."
        "blocked" in e.message!!.lowercase() -> "Too many attempts. Try again later."
        else -> "Authentication failed. Please try again."
    }
}