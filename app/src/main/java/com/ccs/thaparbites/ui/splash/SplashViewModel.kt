package com.ccs.thaparbites.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// SPLASH DESTINATION
// Where to navigate after the splash is done.
// ─────────────────────────────────────────────────────────────

sealed class SplashDestination {
    /** Still waiting — keep showing the splash */
    object Loading : SplashDestination()

    /** No session found → send to Login */
    object Login : SplashDestination()

    /** Valid session exists → send to Home */
    object Home : SplashDestination()
}

// ─────────────────────────────────────────────────────────────
// VIEWMODEL
//
// Inject your AuthRepository here when ready.
// For now it simulates an auth check with a fixed delay so the
// logo animation has time to finish before navigation fires.
// ─────────────────────────────────────────────────────────────

class SplashViewModel : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    companion object {
        /** Total splash duration in ms. Keep ≥ 2000 for animation to complete. */
        private const val SPLASH_DURATION_MS = 2400L
    }

    init {
        checkAuthAndNavigate()
    }

    private fun checkAuthAndNavigate() {
        viewModelScope.launch {
            // Hold for the animation duration first
            delay(SPLASH_DURATION_MS)

            // ── Replace this block with your real auth check ──
            // Example with a repository:
            //
            //   val isLoggedIn = authRepository.isSessionValid()
            //   _destination.value = if (isLoggedIn) SplashDestination.Home
            //                        else            SplashDestination.Login
            //
            // For now, always navigate to Login:
            _destination.value = SplashDestination.Login
        }
    }
}