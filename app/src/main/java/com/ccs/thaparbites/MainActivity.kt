package com.ccs.thaparbites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ccs.thaparbites.navigation.NavRoutes
import com.ccs.thaparbites.ui.auth.LoginScreen
import com.ccs.thaparbites.ui.auth.RegisterScreen
import com.ccs.thaparbites.ui.cart.CartScreen
import com.ccs.thaparbites.ui.checkout.CheckoutScreen
import com.ccs.thaparbites.ui.home.HomeScreen
import com.ccs.thaparbites.ui.menu.MenuScreen
import com.ccs.thaparbites.ui.orders.OrdersScreen
import com.ccs.thaparbites.ui.profile.ProfileScreen
import com.ccs.thaparbites.ui.shared.SharedCartViewModel
import com.ccs.thaparbites.ui.splash.SplashScreen
import com.ccs.thaparbites.ui.theme.ThaparBitesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThaparBitesTheme {
                val navController = rememberNavController()
                // Shared cart scoped to Activity
                val sharedCartViewModel: SharedCartViewModel = hiltViewModel()

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.SPLASH
                ) {
                    composable(NavRoutes.SPLASH) {
                        SplashScreen(
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

                    composable(NavRoutes.LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.REGISTER)
                            }
                        )
                    }

                    composable(NavRoutes.REGISTER) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.HOME) {
                        HomeScreen(
                            sharedCartViewModel = sharedCartViewModel,
                            onNavigateToStore = { storeId ->
                                navController.navigate(NavRoutes.menu(storeId))
                            },
                            onNavigateToCart = { navController.navigate(NavRoutes.CART) },
                            onNavigateToOrders = { navController.navigate(NavRoutes.ORDERS) },
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE) }
                        )
                    }

                    composable(NavRoutes.MENU + "/{storeId}") { backStackEntry ->
                        val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                        MenuScreen(
                            storeId = storeId,
                            sharedCartViewModel = sharedCartViewModel,
                            onNavigateToCart = { navController.navigate(NavRoutes.CART) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.CART) {
                        CartScreen(
                            sharedCartViewModel = sharedCartViewModel,
                            onNavigateToCheckout = { navController.navigate(NavRoutes.CHECKOUT) },
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToOrders = { navController.navigate(NavRoutes.ORDERS) },
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE) },
                            onNavigateToHome = { navController.navigate(NavRoutes.HOME) }
                        )
                    }

                    composable(NavRoutes.CHECKOUT) {
                        CheckoutScreen(
                            sharedCartViewModel = sharedCartViewModel,
                            onOrderPlaced = {
                                sharedCartViewModel.clear()
                                navController.navigate(NavRoutes.ORDERS) {
                                    popUpTo(NavRoutes.HOME) { inclusive = false }
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.ORDERS) {
                        OrdersScreen(
                            onNavigateToHome = { navController.navigate(NavRoutes.HOME) },
                            onNavigateToCart = { navController.navigate(NavRoutes.CART) },
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE) }
                        )
                    }

                    composable(NavRoutes.PROFILE) {
                        ProfileScreen(
                            onSignOut = {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToHome = { navController.navigate(NavRoutes.HOME) },
                            onNavigateToCart = { navController.navigate(NavRoutes.CART) },
                            onNavigateToOrders = { navController.navigate(NavRoutes.ORDERS) }
                        )
                    }
                }
            }
        }
    }
}