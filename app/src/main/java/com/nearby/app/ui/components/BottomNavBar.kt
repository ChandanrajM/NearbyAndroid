package com.nearby.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
)

@Composable
fun BottomNavBar(navController: NavController, cartCount: Int = 0) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom nav on immersive screens
    val immersiveRoutes = listOf("dashboard", "checkout", "shop_detail/", "cart", "scanner", "scan_redirect/")
    if (immersiveRoutes.any { currentRoute?.startsWith(it) == true }) return

    val items = listOf(
        BottomNavItem("home", "Home",
            { Icon(Icons.Filled.Home, "Home") },
            { Icon(Icons.Outlined.Home, "Home") }
        ),
        BottomNavItem("orders", "Orders",
            { Icon(Icons.Filled.Receipt, "Orders") },
            { Icon(Icons.Outlined.Receipt, "Orders") }
        ),
        BottomNavItem("cart", "Cart",
            { BadgedBox(badge = {
                if (cartCount > 0) Badge { Text(if (cartCount > 9) "9+" else "$cartCount") }
            }) { Icon(Icons.Filled.ShoppingCart, "Cart") } },
            { BadgedBox(badge = {
                if (cartCount > 0) Badge { Text(if (cartCount > 9) "9+" else "$cartCount") }
            }) { Icon(Icons.Outlined.ShoppingCart, "Cart") } }
        ),
        BottomNavItem("support", "Support",
            { Icon(Icons.Filled.SupportAgent, "Support") },
            { Icon(Icons.Outlined.SupportAgent, "Support") }
        ),
        BottomNavItem("profile", "Profile",
            { Icon(Icons.Filled.Person, "Profile") },
            { Icon(Icons.Outlined.Person, "Profile") }
        )
    )

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { if (selected) item.selectedIcon() else item.unselectedIcon() },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
