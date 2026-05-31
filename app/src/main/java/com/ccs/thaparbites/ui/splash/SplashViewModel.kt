package com.ccs.thaparbites.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.repository.AuthRepository
import com.ccs.thaparbites.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Login   : SplashDestination()
    object Home    : SplashDestination()
}

class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    init {
        viewModelScope.launch {
            delay(2400)
            _destination.value = if (authRepository.isSessionValid())
                SplashDestination.Home
            else
                SplashDestination.Login
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = AuthRepositoryImpl(FirebaseAuth.getInstance())
            return SplashViewModel(repo) as T
        }
    }
}