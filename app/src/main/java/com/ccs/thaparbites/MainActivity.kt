package com.ccs.thaparbites

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ccs.thaparbites.navigation.NavRoutes
import com.ccs.thaparbites.ui.auth.LoginScreen
import com.ccs.thaparbites.ui.auth.RegisterScreen
import com.ccs.thaparbites.ui.cart.CartScreen
import com.ccs.thaparbites.ui.checkout.CheckoutScreen
import com.ccs.thaparbites.ui.checkout.CheckoutViewModel
import com.ccs.thaparbites.ui.home.HomeScreen
import com.ccs.thaparbites.ui.menu.MenuScreen
import com.ccs.thaparbites.ui.orders.OrdersScreen
import com.ccs.thaparbites.ui.orders.OrdersViewModel
import com.ccs.thaparbites.ui.profile.ProfileScreen
import com.ccs.thaparbites.ui.profile.ProfileViewModel
import com.ccs.thaparbites.ui.shared.SharedCartViewModel
import com.ccs.thaparbites.ui.splash.SplashScreen
import com.ccs.thaparbites.ui.theme.ThaparBitesTheme
import androidx.lifecycle.ViewModelProvider.Factory
import com.example.thaparbites.util.FirestoreSeedData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThaparBitesTheme {
                val navController = rememberNavController()

                // Shared cart scoped to Activity — no Hilt needed
                val sharedCartViewModel: SharedCartViewModel = viewModel()

                NavHost(
                    navController    = navController,
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
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(NavRoutes.HOME) {
                        HomeScreen(
                            onStoreClick = { store ->
                                navController.navigate(NavRoutes.menu(store.id))
                            },
                            onCartClick = {
                                navController.navigate(NavRoutes.CART)
                            },
                            onOrdersClick = {
                                navController.navigate(NavRoutes.ORDERS)
                            },
                            onProfileClick = {
                                navController.navigate(NavRoutes.PROFILE)
                            }
                        )
                    }

                    // NOTE: NavRoutes.MENU is "menu/{storeId}" — navArgument is inline here
                    composable(
                        route = "menu/{storeId}",
                        arguments = listOf(navArgument("storeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                        MenuScreen(
                            storeId   = storeId,
                            onBack    = { navController.popBackStack() },
                            onViewCart = { navController.navigate(NavRoutes.CART) }
                        )
                    }

                    composable(NavRoutes.CART) {
                        CartScreen(
                            onBack = { navController.popBackStack() },
                            onCheckout = { navController.navigate(NavRoutes.CHECKOUT) }
                        )
                    }

                    composable(NavRoutes.CHECKOUT) {
                        CheckoutScreen(
                            onBack = { navController.popBackStack() },
                            onOrderPlaced = { _ ->
                                navController.navigate(NavRoutes.ORDERS) {
                                    popUpTo(NavRoutes.HOME) { inclusive = false }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.ORDERS) {
                        OrdersScreen(
                            viewModel        = viewModel(factory = OrdersViewModel.Factory()),
                            onNavigateHome   = { navController.navigate(NavRoutes.HOME) },
                            onNavigateCart   = { navController.navigate(NavRoutes.CART) },
                            onNavigateProfile = { navController.navigate(NavRoutes.PROFILE) }
                        )
                    }

                    composable(NavRoutes.PROFILE) {
                        ProfileScreen(
                            viewModel       = viewModel(factory = ProfileViewModel.Factory()),
                            onSignedOut     = {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateHome  = { navController.navigate(NavRoutes.HOME) },
                            onNavigateCart  = { navController.navigate(NavRoutes.CART) },
                            onNavigateOrders = { navController.navigate(NavRoutes.ORDERS) }
                        )
                    }
                }
            }
        }
    }
}