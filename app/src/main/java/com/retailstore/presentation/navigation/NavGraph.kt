package com.retailstore.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.retailstore.presentation.screens.admin.AdminPanelScreen
import com.retailstore.presentation.screens.admin.orders.AdminOrderDetailScreen
import com.retailstore.presentation.screens.admin.orders.AdminOrderListScreen
import com.retailstore.presentation.screens.admin.products.AdminProductFormScreen
import com.retailstore.presentation.screens.admin.products.AdminProductListScreen
import com.retailstore.presentation.screens.auth.LoginScreen
import com.retailstore.presentation.screens.auth.RegisterScreen
import com.retailstore.presentation.screens.cart.CartScreen
import com.retailstore.presentation.screens.cart.CheckoutScreen
import com.retailstore.presentation.screens.catalog.CatalogScreen
import com.retailstore.presentation.screens.catalog.ProductDetailScreen
import com.retailstore.presentation.screens.orders.OrderDetailScreen
import com.retailstore.presentation.screens.orders.OrdersScreen
import com.retailstore.presentation.screens.profile.ProfileScreen
import com.retailstore.presentation.screens.wishlist.WishlistScreen
import com.retailstore.presentation.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Catalog : Screen("catalog")
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(id: String) = "product/$id"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order_success")
    object Wishlist : Screen("wishlist")
    object Profile : Screen("profile")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order/{orderId}") {
        fun createRoute(id: String) = "order/$id"
    }
    object AdminPanel : Screen("admin")
    object AdminProductList : Screen("admin/products")
    object AdminProductCreate : Screen("admin/products/create")
    object AdminProductEdit : Screen("admin/products/{productId}/edit") {
        fun createRoute(id: String) = "admin/products/$id/edit"
    }
    object AdminOrderList : Screen("admin/orders")
    object AdminOrderDetail : Screen("admin/orders/{orderId}") {
        fun createRoute(id: String) = "admin/orders/$id"
    }
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Catalog, "Каталог", Icons.Default.Home),
    BottomNavItem(Screen.Cart, "Корзина", Icons.Default.ShoppingCart),
    BottomNavItem(Screen.Wishlist, "Избранное", Icons.Default.Favorite),
    BottomNavItem(Screen.Profile, "Профиль", Icons.Default.Person)
)

@Composable
fun RetailStoreNavGraph() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
    val cartCount by mainViewModel.cartCount.collectAsState()

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Catalog.route, Screen.Cart.route, Screen.Wishlist.route, Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                if (item.screen == Screen.Cart && cartCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            },
                            label = { Text(item.label) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.Catalog.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Catalog.route else Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) }
                )
            }
            composable(Screen.ProductDetail.route) { backStack ->
                val productId = backStack.arguments?.getString("productId") ?: return@composable
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onLoginRequired = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    onCheckout = { navController.navigate(Screen.Checkout.route) },
                    onLoginRequired = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onOrderPlaced = {
                        navController.navigate(Screen.OrderSuccess.route) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.OrderSuccess.route) {
                OrderSuccessScreen(
                    onGoHome = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                    onLoginRequired = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onMyOrders = { navController.navigate(Screen.Orders.route) },
                    onAdminPanel = { navController.navigate(Screen.AdminPanel.route) }
                )
            }
            composable(Screen.Orders.route) {
                OrdersScreen(
                    onOrderClick = { navController.navigate(Screen.OrderDetail.createRoute(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.OrderDetail.route) { backStack ->
                val orderId = backStack.arguments?.getString("orderId") ?: return@composable
                OrderDetailScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminPanel.route) {
                AdminPanelScreen(
                    onProducts = { navController.navigate(Screen.AdminProductList.route) },
                    onOrders = { navController.navigate(Screen.AdminOrderList.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminProductList.route) {
                AdminProductListScreen(
                    onCreateProduct = { navController.navigate(Screen.AdminProductCreate.route) },
                    onEditProduct = { navController.navigate(Screen.AdminProductEdit.createRoute(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminProductCreate.route) {
                AdminProductFormScreen(productId = null, onBack = { navController.popBackStack() })
            }
            composable(Screen.AdminProductEdit.route) { backStack ->
                val productId = backStack.arguments?.getString("productId") ?: return@composable
                AdminProductFormScreen(productId = productId, onBack = { navController.popBackStack() })
            }
            composable(Screen.AdminOrderList.route) {
                AdminOrderListScreen(
                    onOrderClick = { navController.navigate(Screen.AdminOrderDetail.createRoute(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminOrderDetail.route) { backStack ->
                val orderId = backStack.arguments?.getString("orderId") ?: return@composable
                AdminOrderDetailScreen(orderId = orderId, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun OrderSuccessScreen(onGoHome: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Заказ оформлен!", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Ваш заказ успешно создан и ожидает подтверждения.")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onGoHome) { Text("На главную") }
        }
    }
}
