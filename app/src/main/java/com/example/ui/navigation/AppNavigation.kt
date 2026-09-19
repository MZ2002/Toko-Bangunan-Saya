package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AddEditProductScreen
import com.example.ui.screens.BackupSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProductListScreen
import com.example.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Beranda", Icons.Filled.Store, Icons.Outlined.Store)
    data object Products : Screen("products", "Daftar Harga", Icons.Filled.Inventory, Icons.Outlined.Inventory2)
    data object Expenses : Screen("expenses", "Pengeluaran", Icons.Filled.Payment, Icons.Outlined.Payment)
    data object Settings : Screen("settings", "Cadangan", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Dashboard,
    Screen.Products,
    Screen.Expenses,
    Screen.Settings
)

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on root tab destinations
    val showBottomBar = BOTTOM_NAV_ITEMS.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BOTTOM_NAV_ITEMS.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title, fontSize = 12.sp) },
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddProduct = {
                        navController.navigate("add_edit_product")
                    },
                    onNavigateToProductList = { category ->
                        viewModel.selectedCategory.value = category
                        navController.navigate(Screen.Products.route)
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigate("product_detail/$productId")
                    },
                    onNavigateToExpenses = {
                        navController.navigate(Screen.Expenses.route)
                    }
                )
            }

            // Product List
            composable(Screen.Products.route) {
                ProductListScreen(
                    viewModel = viewModel,
                    onNavigateToAddProduct = {
                        navController.navigate("add_edit_product")
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }

            // Expenses
            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Settings & Backup
            composable(Screen.Settings.route) {
                BackupSettingsScreen(
                    viewModel = viewModel
                )
            }

            // Product Detail
            composable(
                route = "product_detail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { id ->
                        navController.navigate("add_edit_product?productId=$id")
                    }
                )
            }

            // Add/Edit Product
            composable(
                route = "add_edit_product?productId={productId}",
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId")?.takeIf { it > 0 }
                AddEditProductScreen(
                    productId = productId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaved = { savedId ->
                        navController.popBackStack()
                        if (productId == null) {
                            // If it was a new product, open its detail screen
                            navController.navigate("product_detail/$savedId")
                        }
                    }
                )
            }
        }
    }
}
