package com.nearby.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.nearby.app.ui.components.BottomNavBar
import com.nearby.app.ui.screens.*
import com.nearby.app.ui.viewmodel.CartViewModel

@Composable
fun NearbyNavGraph() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                cartCount = cartState.count
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Home ──
            composable("home") {
                HomeScreen(
                    onShopClick = { shopId ->
                        navController.navigate("shop_detail/$shopId")
                    },
                    onScanClick = {
                        navController.navigate("scanner")
                    }
                )
            }

            // ── Auth ──
            composable("auth") {
                AuthScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // ── Shop Detail ──
            composable(
                route = "shop_detail/{shopId}",
                arguments = listOf(navArgument("shopId") { type = NavType.StringType })
            ) { backStackEntry ->
                val shopId = backStackEntry.arguments?.getString("shopId") ?: return@composable
                ShopDetailScreen(
                    shopId = shopId,
                    onBack = { navController.popBackStack() },
                    onCartClick = { navController.navigate("cart") }
                )
            }

            // ── Cart ──
            composable("cart") {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate("checkout") },
                    onSignIn = { navController.navigate("auth") },
                    onBrowse = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    viewModel = cartViewModel
                )
            }

            // ── Checkout ──
            composable("checkout") {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onHome = {
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSignIn = { navController.navigate("auth") }
                )
            }

            // ── Orders ──
            composable("orders") {
                OrdersScreen(
                    onBack = { navController.popBackStack() },
                    onSignIn = { navController.navigate("auth") },
                    onBrowse = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            // ── Profile ──
            composable("profile") {
                ProfileScreen(
                    onSignIn = { navController.navigate("auth") },
                    onDashboard = { navController.navigate("dashboard") },
                    onAdmin = { navController.navigate("admin") },
                    onRegisterShop = { navController.navigate("register_shop") },
                    onOrders = { navController.navigate("orders") },
                    onSupport = { navController.navigate("support") }
                )
            }

            // ── Register Shop ──
            composable("register_shop") {
                RegisterShopScreen(
                    onBack = { navController.popBackStack() },
                    onSignIn = { navController.navigate("auth") }
                )
            }

            // ── Dashboard ──
            composable("dashboard") {
                DashboardScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Admin ──
            composable("admin") {
                AdminScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Scanner ──
            composable("scanner") {
                ScannerScreen(
                    onClose = { navController.popBackStack() },
                    onShopFound = { shopId ->
                        navController.navigate("shop_detail/$shopId") {
                            popUpTo("scanner") { inclusive = true }
                        }
                    },
                    onScanRedirect = { code ->
                        navController.navigate("scan_redirect/$code") {
                            popUpTo("scanner") { inclusive = true }
                        }
                    }
                )
            }

            // ── Scan Redirect ──
            composable(
                route = "scan_redirect/{code}",
                arguments = listOf(navArgument("code") { type = NavType.StringType })
            ) { backStackEntry ->
                val code = backStackEntry.arguments?.getString("code") ?: return@composable
                ScanRedirectScreen(
                    qrCode = code,
                    onShopFound = { shopId ->
                        navController.navigate("shop_detail/$shopId") {
                            popUpTo("scan_redirect/$code") { inclusive = true }
                        }
                    },
                    onNotFound = {
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Support ──
            composable("support") {
                SupportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
