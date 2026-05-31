package com.ccs.thaparbites.ui.auth

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Builds a [GoogleSignInClient].
 *
 * Replace [webClientId] with the OAuth 2.0 Web Client ID from:
 * Firebase Console → Project Settings → Your apps → google-services.json
 * Use the "Web" client ID, NOT the Android client ID.
 */
fun buildGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, options)
}

/**
 * Returns a lambda that launches the Google Sign-In flow.
 *
 * FIX: Calls [googleSignInClient].signOut() before launching the intent.
 *
 * Without this, returning users skip the account picker entirely because
 * Google silently reuses the cached signed-in account. The app then hangs
 * waiting for a Firebase credential exchange that was never triggered
 * visibly — making sign-in feel broken or very slow.
 *
 * signOut() clears the cached account so the picker always appears,
 * giving a consistent, predictable UX.
 *
 * Usage in LoginScreen:
 *
 *   val launchGoogleSignIn = rememberGoogleSignInLauncher(
 *       googleSignInClient = googleClient,
 *       onToken = { token -> viewModel.loginWithGoogle(onLoginSuccess, idToken = token) },
 *       onFailed = { viewModel.onGoogleSignInFailed() }
 *   )
 *
 *   OutlinedButton(onClick = launchGoogleSignIn) { ... }
 */
@Composable
fun rememberGoogleSignInLauncher(
    googleSignInClient: GoogleSignInClient,
    onToken: (String) -> Unit,
    onFailed: () -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                onToken(idToken)
            } else {
                onFailed()
            }
        } catch (e: ApiException) {
            onFailed()
        }
    }

    // FIX: signOut() before launching so the account picker always shows.
    // Intent is created inside the lambda (call-time), not at remember-time,
    // so it always reflects the post-signOut state.
    return remember(launcher, googleSignInClient) {
        {
            googleSignInClient.signOut().addOnCompleteListener {
                launcher.launch(googleSignInClient.signInIntent)
            }
        }
    }
}