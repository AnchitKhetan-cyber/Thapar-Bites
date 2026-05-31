package com.ccs.thaparbites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ccs.thaparbites.navigation.NavRoutes
import com.ccs.thaparbites.ui.splash.SplashScreen
import com.ccs.thaparbites.ui.theme.ThaparBitesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind system bars — theme handles bar colors via SideEffect
        enableEdgeToEdge()

        setContent {
            ThaparBitesTheme {
                ThaparBitesApp()
            }
        }
    }
}

@Composable
fun ThaparBitesApp() {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController    = navController,
            startDestination = NavRoutes.SPLASH,
        ) {

            // ── Splash ────────────────────────────────────────
            composable(NavRoutes.SPLASH) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(NavRoutes.LOGIN) {
                            // Clear splash from back stack — pressing back should exit the app
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    },
                )
            }

            // ── Login — add LoginScreen composable here ───────
            composable(NavRoutes.LOGIN) {
                // LoginScreen(onLoginSuccess = { navController.navigate(NavRoutes.HOME) { ... } })
            }

            // ── Home — add HomeScreen composable here ─────────
            composable(NavRoutes.HOME) {
                // HomeScreen(navController = navController)
            }

            // Add remaining routes as you build each screen
        }
    }
}



