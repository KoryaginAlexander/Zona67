package com.retailstore.presentation.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.presentation.components.CartItemRow
import com.retailstore.presentation.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onLoginRequired: () -> Unit,
    onProductClick: (String) -> Unit = {},
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCart() }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val onSurface = MaterialTheme.colorScheme.onSurface
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                            append("Zona")
                        }
                        withStyle(SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                            append("67")
                        }
                    }
                )
                Text(
                    text = "Корзина",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                )
            }
        }
    ) { padding ->
        when {
            uiState.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                                onRemove = { viewModel.removeItem(item.productId) },
                                onClick = { onProductClick(item.productId) }
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
                            Button(
                                onClick = { if (uiState.isGuest) onLoginRequired() else onCheckout() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Оформить заказ")
                            }
                        }
                    }
                }
            }
        }
    }
}
