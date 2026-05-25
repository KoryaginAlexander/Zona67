package com.retailstore.presentation.screens.orders

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
import com.retailstore.domain.model.Result
import com.retailstore.domain.model.Order
import com.retailstore.domain.repository.OrderRepository
import com.retailstore.presentation.components.OrderStatusChip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun load(id: String) = viewModelScope.launch {
        _loading.value = true
        when (val r = orderRepository.getMyOrderById(id)) {
            is Result.Success -> { _order.value = r.data; _loading.value = false }
            else -> _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val order by viewModel.order.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(orderId) { viewModel.load(orderId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else order?.let { o ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                item {
                    Text("Заказ #${o.id.takeLast(8)}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OrderStatusChip(o.status)
                        Text(o.createdAt.take(10), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    o.deliveryAddress?.let { Text("Адрес: $it", style = MaterialTheme.typography.bodyMedium) }
                    o.comment?.let { Text("Комментарий: $it", style = MaterialTheme.typography.bodyMedium) }
                    Spacer(Modifier.height(16.dp))
                    Text("Товары", style = MaterialTheme.typography.titleMedium)
                    Divider()
                }
                items(o.items) { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                            Text("${item.quantity} шт. × ${item.productPrice.toLong()} ₽", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${(item.quantity * item.productPrice).toLong()} ₽", style = MaterialTheme.typography.titleSmall)
                    }
                    Divider()
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Итого:", style = MaterialTheme.typography.titleMedium)
                        Text("${o.totalAmount.toLong()} ₽", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
