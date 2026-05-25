package com.retailstore.presentation.screens.admin.orders

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Order
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.OrderRepository
import com.retailstore.presentation.components.OrderStatusChip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOrderDetailUiState(
    val loading: Boolean = true,
    val order: Order? = null,
    val error: String? = null,
    val message: String? = null
)

private val ORDER_TRANSITIONS = mapOf(
    "PENDING" to listOf("CONFIRMED", "CANCELLED"),
    "CONFIRMED" to listOf("PROCESSING", "CANCELLED"),
    "PROCESSING" to listOf("SHIPPED", "CANCELLED"),
    "SHIPPED" to listOf("DELIVERED", "CANCELLED"),
    "DELIVERED" to emptyList(),
    "CANCELLED" to emptyList()
)

@HiltViewModel
class AdminOrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminOrderDetailUiState())
    val uiState: StateFlow<AdminOrderDetailUiState> = _uiState.asStateFlow()

    fun load(id: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        when (val r = orderRepository.getOrderById(id)) {
            is Result.Success -> _uiState.update { it.copy(loading = false, order = r.data) }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = r.message) }
            else -> {}
        }
    }

    fun updateStatus(id: String, status: String) = viewModelScope.launch {
        when (val r = orderRepository.updateOrderStatus(id, status)) {
            is Result.Success -> _uiState.update { it.copy(order = r.data, message = "Статус изменён") }
            is Result.Error -> _uiState.update { it.copy(error = r.message) }
            else -> {}
        }
    }

    fun cancelOrder(id: String) = viewModelScope.launch {
        when (val r = orderRepository.cancelOrder(id)) {
            is Result.Success -> _uiState.update { it.copy(order = r.data, message = "Заказ отменён") }
            is Result.Error -> _uiState.update { it.copy(error = r.message) }
            else -> {}
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: AdminOrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelDialog by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) { viewModel.load(orderId) }
    LaunchedEffect(uiState.message) { uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() } }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Отменить заказ?") },
            text = { Text("Товары будут возвращены на склад. Действие необратимо.") },
            confirmButton = {
                TextButton(onClick = { showCancelDialog = false; viewModel.cancelOrder(orderId) }) { Text("Отменить заказ") }
            },
            dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("Закрыть") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else uiState.order?.let { order ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                item {
                    Text("Заказ #${order.id.takeLast(8)}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OrderStatusChip(order.status)
                        Text(order.createdAt.take(10), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Сумма: ${order.totalAmount.toLong()} ₽", style = MaterialTheme.typography.titleMedium)
                    order.deliveryAddress?.let { Text("Адрес: $it") }
                    order.comment?.let { Text("Комментарий: $it") }
                    Spacer(Modifier.height(16.dp))

                    val allowedStatuses = ORDER_TRANSITIONS[order.status] ?: emptyList()
                    if (allowedStatuses.isNotEmpty()) {
                        ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                            OutlinedTextField(
                                value = "Изменить статус...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Статус") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                                allowedStatuses.filter { it != "CANCELLED" }.forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = {
                                        statusExpanded = false
                                        viewModel.updateStatus(orderId, s)
                                    })
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (order.status != "CANCELLED" && order.status != "DELIVERED") {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Отменить заказ") }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Товары", style = MaterialTheme.typography.titleMedium)
                    Divider()
                }
                items(order.items) { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                            Text("${item.quantity} шт. × ${item.productPrice.toLong()} ₽", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${(item.quantity * item.productPrice).toLong()} ₽")
                    }
                    Divider()
                }
            }
        }
    }
}
