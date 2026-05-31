package com.ccs.thaparbites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ccs.thaparbites.navigation.NavRoutes
import com.ccs.thaparbites.ui.auth.LoginScreen
import com.ccs.thaparbites.ui.auth.LoginViewModel
import com.ccs.thaparbites.ui.cart.CartScreen
import com.ccs.thaparbites.ui.checkout.CheckoutScreen
import com.ccs.thaparbites.ui.checkout.CheckoutViewModel
import com.ccs.thaparbites.ui.home.HomeScreen
import com.ccs.thaparbites.ui.menu.MenuScreen
import com.ccs.thaparbites.ui.orders.OrdersScreen
import com.ccs.thaparbites.ui.profile.ProfileScreen
import com.ccs.thaparbites.ui.splash.SplashScreen
import com.ccs.thaparbites.ui.splash.SplashViewModel
import com.ccs.thaparbites.ui.theme.ThaparBitesTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.ui.auth.RegisterScreen
import com.ccs.thaparbites.ui.auth.buildGoogleSignInClient
import com.ccs.thaparbites.ui.auth.rememberGoogleSignInLauncher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThaparBitesTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {

        // ── Splash ────────────────────────────────────────────────────────
        composable(NavRoutes.SPLASH) {
            val vm: SplashViewModel = viewModel()
            SplashScreen(
                viewModel = vm,
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ─────────────────────────────────────────────────────────
        composable(NavRoutes.LOGIN) {
            val vm: LoginViewModel = viewModel()
            val context = LocalContext.current
            val googleClient = remember { buildGoogleSignInClient(context, "YOUR_WEB_CLIENT_ID") }
            val launchGoogle = rememberGoogleSignInLauncher(
                googleSignInClient = googleClient,
                onToken = { token -> vm.loginWithGoogle(
                    onSuccess = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    idToken = token
                ) },
                onFailed = { vm.onGoogleSignInFailed() }
            )
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onGoogleSignInClick = { launchGoogle() }
            )
        }

        // ── Register ──────────────────────────────────────────────────────
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(NavRoutes.HOME) {
            HomeScreen(
                onStoreClick = { storeId ->
                    navController.navigate(NavRoutes.menu(storeId))
                },
                onNavigateOrders = { navController.navigate(NavRoutes.ORDERS) },
                onNavigateCart   = { navController.navigate(NavRoutes.CART) },
                onNavigateProfile = { navController.navigate(NavRoutes.PROFILE) }
            )
        }

        // ── Menu ──────────────────────────────────────────────────────────
        composable(NavRoutes.MENU + "/{storeId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            // Pass storeId to MenuScreen to filter items.
            // MenuScreen's local CartViewModel is scoped to this backstack entry.
            MenuScreen(
                storeId = storeId,
                onBack = { navController.popBackStack() },
                onNavigateToCheckout = { cart, store ->
                    // Use shared CartViewModel or pass args as needed.
                    // For MVP, push to CHECKOUT route; CheckoutViewModel is scoped to CHECKOUT.
                    navController.navigate(NavRoutes.CHECKOUT)
                    // In a real flow, use a shared CartViewModel:
                    // sharedCartViewModel.setCart(cart, store)
                }
            )
        }

        // ── Cart ──────────────────────────────────────────────────────────
        composable(NavRoutes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onProceedToCheckout = {
                    navController.navigate(NavRoutes.CHECKOUT)
                }
            )
        }

        // ── Checkout ──────────────────────────────────────────────────────
        composable(NavRoutes.CHECKOUT) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(NavRoutes.ORDERS) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // ── Orders ────────────────────────────────────────────────────────
        composable(NavRoutes.ORDERS) {
            OrdersScreen(
                onNavigateHome    = { navController.navigate(NavRoutes.HOME) { launchSingleTop = true } },
                onNavigateCart    = { navController.navigate(NavRoutes.CART) },
                onNavigateProfile = { navController.navigate(NavRoutes.PROFILE) }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────
        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onNavigateHome   = { navController.navigate(NavRoutes.HOME) { launchSingleTop = true } },
                onNavigateOrders = { navController.navigate(NavRoutes.ORDERS) },
                onNavigateCart   = { navController.navigate(NavRoutes.CART) },
                onSignedOut = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}