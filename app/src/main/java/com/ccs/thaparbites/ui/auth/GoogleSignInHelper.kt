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
 * Helper to build a [GoogleSignInClient].
 *
 * Replace [webClientId] with the OAuth 2.0 Web Client ID from your
 * Firebase Console → Project Settings → Your apps → google-services.json
 * (it is the one of type "Web", NOT the Android client ID).
 */
fun buildGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, options)
}

/**
 * A composable hook that returns a lambda to launch the Google Sign-In flow.
 * Pass in [onToken] to receive the idToken and [onFailed] for error handling.
 *
 * Usage in LoginScreen:
 *
 *   val launchGoogleSignIn = rememberGoogleSignInLauncher(
 *       onToken = { token -> viewModel.loginWithGoogle(onLoginSuccess, idToken = token) },
 *       onFailed = { viewModel.onGoogleSignInFailed() }
 *   )
 *
 *   // Then in the Google button's onClick:
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

    return remember(googleSignInClient) {
        { launcher.launch(googleSignInClient.signInIntent) }
    }
}

// ─────────────────────────────────────────────
// How to wire Google Sign-In in LoginScreen:
// ─────────────────────────────────────────────
//
// 1. Add to build.gradle (app):
//    implementation("com.google.android.gms:play-services-auth:21.2.0")
//
// 2. In LoginScreen (or its parent), build the client once:
//
//    val context = LocalContext.current
//    val googleClient = remember {
//        buildGoogleSignInClient(context, webClientId = "YOUR_WEB_CLIENT_ID_HERE")
//    }
//
//    val launchGoogleSignIn = rememberGoogleSignInLauncher(
//        googleSignInClient = googleClient,
//        onToken = { token -> viewModel.loginWithGoogle(onLoginSuccess, idToken = token) },
//        onFailed = { viewModel.onGoogleSignInFailed() }
//    )
//
// 3. Pass launchGoogleSignIn as onGoogleSignInClick to LoginContent.
//
// 4. The @thapar.edu domain check is already inside LoginViewModel.loginWithGoogle().

