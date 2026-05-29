package com.retailstore.presentation.screens.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Order
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.OrderRepository
import com.retailstore.presentation.components.OrderStatusChip
import com.retailstore.presentation.theme.OrangePrimary
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

private val ALL_STATUSES = listOf(
    "PENDING" to "Новый",
    "CONFIRMED" to "Подтверждён",
    "PROCESSING" to "В обработке",
    "SHIPPED" to "Отправлен",
    "DELIVERED" to "Доставлен",
    "CANCELLED" to "Отменён"
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
    var pendingStatus by remember { mutableStateOf("") }

    LaunchedEffect(orderId) { viewModel.load(orderId) }
    LaunchedEffect(uiState.message) { uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() } }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Отменить заказ?") },
            text = { Text("Товары будут возвращены на склад. Действие необратимо.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.cancelOrder(orderId)
                }) {
                    Text("Отменить заказ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false; pendingStatus = "" }) { Text("Закрыть") }
            }
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "Детали заказа",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else uiState.order?.let { order ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Заказ #${order.id.takeLast(8)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OrderStatusChip(order.status)
                                Text(
                                    order.createdAt.take(10),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Сумма: ${order.totalAmount.toLong()} ₽",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OrangePrimary
                            )
                            order.deliveryAddress?.let {
                                Spacer(Modifier.height(4.dp))
                                Text("Адрес: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            order.comment?.let {
                                Spacer(Modifier.height(4.dp))
                                Text("Комментарий: $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                            }
                        }
                    }
                }

                val availableStatuses = ALL_STATUSES.filter { it.first != order.status }
                if (availableStatuses.isNotEmpty() && order.status != "CANCELLED") {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExposedDropdownMenuBox(
                                    expanded = statusExpanded,
                                    onExpandedChange = { statusExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = "Изменить статус...",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Статус") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = statusExpanded,
                                        onDismissRequest = { statusExpanded = false },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                    ) {
                                        availableStatuses.forEach { (status, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    statusExpanded = false
                                                    if (status == "CANCELLED") {
                                                        pendingStatus = status
                                                        showCancelDialog = true
                                                    } else {
                                                        viewModel.updateStatus(orderId, status)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Товары",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(order.items) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "${item.quantity} шт. × ${item.productPrice.toLong()} ₽",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                            }
                            Text(
                                "${(item.quantity * item.productPrice).toLong()} ₽",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OrangePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
