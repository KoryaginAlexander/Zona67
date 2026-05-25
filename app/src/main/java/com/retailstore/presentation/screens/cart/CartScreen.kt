package com.retailstore.presentation.screens.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.presentation.components.CartItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onLoginRequired: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCart() }

    Scaffold(topBar = { TopAppBar(title = { Text("Корзина") }) }) { padding ->
        when {
            uiState.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.isGuest -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Войдите, чтобы оформить заказ")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onLoginRequired) { Text("Войти") }
                }
            }
            uiState.cart?.items.isNullOrEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Корзина пуста")
            }
            else -> {
                val cart = uiState.cart!!
                Column(modifier = Modifier.padding(padding)) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(cart.items) { item ->
                            CartItemRow(
                                item = item,
                                onIncrease = { viewModel.updateQuantity(item.productId, item.quantity + 1) },
                                onDecrease = { viewModel.updateQuantity(item.productId, item.quantity - 1) },
                                onRemove = { viewModel.removeItem(item.productId) }
                            )
                            Divider()
                        }
                    }
                    Surface(shadowElevation = 8.dp) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Итого:", style = MaterialTheme.typography.titleMedium)
                                Text("${cart.total.toLong()} ₽", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
                                Text("Оформить заказ")
                            }
                        }
                    }
                }
            }
        }
    }
}
