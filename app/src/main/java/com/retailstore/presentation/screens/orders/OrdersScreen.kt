package com.retailstore.presentation.screens.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.presentation.components.OrderStatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заказы") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        when {
            uiState.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.orders.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Заказов пока нет")
            }
            else -> LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.orders) { order ->
                    ListItem(
                        modifier = Modifier.clickable { onOrderClick(order.id) },
                        headlineContent = { Text("Заказ #${order.id.takeLast(8)}") },
                        supportingContent = {
                            Column {
                                Text(order.createdAt.take(10))
                                OrderStatusChip(order.status)
                            }
                        },
                        trailingContent = {
                            Text("${order.totalAmount.toLong()} ₽", style = MaterialTheme.typography.titleMedium)
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
