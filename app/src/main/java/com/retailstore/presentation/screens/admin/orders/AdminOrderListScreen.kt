package com.retailstore.presentation.screens.admin.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

private val statusFilters = listOf(
    null to "Все",
    "PENDING" to "Новые",
    "CONFIRMED" to "Подтверждены",
    "PROCESSING" to "В обработке",
    "SHIPPED" to "Отправлены",
    "DELIVERED" to "Доставлены",
    "CANCELLED" to "Отменены"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderListScreen(
    onOrderClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: AdminOrderListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Заказы") },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statusFilters.size) { i ->
                        val (status, label) = statusFilters[i]
                        FilterChip(
                            selected = uiState.selectedStatus == status,
                            onClick = { viewModel.load(status) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                            Text("${order.totalAmount.toLong()} ₽", style = MaterialTheme.typography.titleSmall)
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
