package com.retailstore.presentation.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.domain.model.Order
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.OrderRepository
import com.retailstore.domain.repository.ReviewRepository
import com.retailstore.domain.repository.UserRepository
import com.retailstore.presentation.components.OrderStatusChip
import com.retailstore.presentation.theme.OrangePrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class OrderDetailUiState(
    val order: Order? = null,
    val loading: Boolean = true,
    val currentUserId: String? = null,
    val reviewedProductIds: Set<String> = emptySet(),
    val reviewDialogProductId: String? = null,
    val reviewRating: Int = 0,
    val reviewComment: String = "",
    val reviewLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    fun load(id: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        when (val r = orderRepository.getMyOrderById(id)) {
            is Result.Success -> {
                _uiState.update { it.copy(order = r.data, loading = false) }
                loadCurrentUser()
                loadReviewedProducts(r.data)
            }
            else -> _uiState.update { it.copy(loading = false) }
        }
    }

    private fun loadCurrentUser() = viewModelScope.launch {
        when (val r = userRepository.getMe()) {
            is Result.Success -> _uiState.update { it.copy(currentUserId = r.data.id) }
            else -> {}
        }
    }

    private fun loadReviewedProducts(order: Order) = viewModelScope.launch {
        val reviewed = mutableSetOf<String>()
        order.items.forEach { item ->
            when (val r = reviewRepository.getReviews(item.productId)) {
                is Result.Success -> {
                    val userId = _uiState.value.currentUserId
                    if (userId != null && r.data.any { it.userId == userId }) {
                        reviewed.add(item.productId)
                    }
                }
                else -> {}
            }
        }
        _uiState.update { it.copy(reviewedProductIds = reviewed) }
    }

    fun showReviewDialog(productId: String) = _uiState.update {
        it.copy(reviewDialogProductId = productId, reviewRating = 0, reviewComment = "")
    }

    fun dismissReviewDialog() = _uiState.update { it.copy(reviewDialogProductId = null) }

    fun setReviewRating(rating: Int) = _uiState.update { it.copy(reviewRating = rating) }
    fun setReviewComment(comment: String) = _uiState.update { it.copy(reviewComment = comment) }

    fun submitReview() = viewModelScope.launch {
        val productId = _uiState.value.reviewDialogProductId ?: return@launch
        val rating = _uiState.value.reviewRating
        if (rating == 0) { _uiState.update { it.copy(message = "Поставьте оценку") }; return@launch }
        _uiState.update { it.copy(reviewLoading = true) }
        when (val r = reviewRepository.addReview(productId, rating, _uiState.value.reviewComment)) {
            is Result.Success -> _uiState.update { it.copy(
                reviewLoading = false,
                reviewDialogProductId = null,
                reviewedProductIds = it.reviewedProductIds + productId,
                message = "Отзыв опубликован"
            ) }
            is Result.Error -> _uiState.update { it.copy(reviewLoading = false, message = r.message) }
            else -> _uiState.update { it.copy(reviewLoading = false) }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit = {},
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(orderId) { viewModel.load(orderId) }
    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    // Review bottom sheet
    uiState.reviewDialogProductId?.let {
        ReviewBottomSheet(
            rating = uiState.reviewRating,
            comment = uiState.reviewComment,
            loading = uiState.reviewLoading,
            onRatingChange = viewModel::setReviewRating,
            onCommentChange = viewModel::setReviewComment,
            onSubmit = { viewModel.submitReview() },
            onDismiss = { viewModel.dismissReviewDialog() }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.White).statusBarsPadding().height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color(0xFF1A1A1A))
                }
                Text("Детали заказа", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
            }
        }
    ) { padding ->
        if (uiState.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else uiState.order?.let { o ->
            val isDelivered = o.status == "DELIVERED"
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Заказ #${o.id.takeLast(8)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OrderStatusChip(o.status)
                                Text(o.createdAt.take(10), style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                            }
                            if (o.deliveryAddress != null) {
                                Spacer(Modifier.height(8.dp))
                                Text("Адрес: ${o.deliveryAddress}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1A1A1A))
                            }
                            if (o.comment != null) {
                                Spacer(Modifier.height(4.dp))
                                Text("Комментарий: ${o.comment}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                            }
                        }
                    }
                }

                item {
                    Text("Товары", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575), modifier = Modifier.padding(horizontal = 4.dp))
                }

                items(o.items) { item ->
                    val alreadyReviewed = uiState.reviewedProductIds.contains(item.productId)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { onProductClick(item.productId) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Medium
                                    )
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

                            if (isDelivered) {
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                if (alreadyReviewed) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                                        Text("Отзыв оставлен", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                                    }
                                } else {
                                    TextButton(
                                        onClick = { viewModel.showReviewDialog(item.productId) },
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Оставить отзыв", color = OrangePrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Итого:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("${o.totalAmount.toLong()} ₽", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewBottomSheet(
    rating: Int,
    comment: String,
    loading: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Оставить отзыв", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (star in 1..5) {
                    IconButton(onClick = { onRatingChange(star) }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (star <= rating) OrangePrimary else Color(0xFFCCCCCC),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Комментарий (необязательно)", color = Color(0xFF9E9E9E)) },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, unfocusedBorderColor = Color(0xFFDDDDDD))
            )

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Опубликовать", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
