package com.retailstore.presentation.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.retailstore.domain.model.Review
import com.retailstore.presentation.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onLoginRequired: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) { viewModel.loadProduct(productId) }
    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            uiState.product?.let { product ->
                Surface(shadowElevation = 12.dp, color = Color.White) {
                    Button(
                        onClick = { viewModel.addToCart() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        enabled = product.isInStock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            disabledContainerColor = Color(0xFFDDDDDD)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = if (product.isInStock) "Добавить в корзину" else "Нет в наличии",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }

            uiState.product != null -> {
                val product = uiState.product!!
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

                    // Hero image
                    item {
                        Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(280.dp)) {
                            AsyncImage(
                                model = product.firstImageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier.padding(start = 12.dp, top = 10.dp).size(40.dp)
                                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).align(Alignment.TopStart),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Box(
                                modifier = Modifier.padding(end = 12.dp, top = 10.dp).size(40.dp)
                                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = { viewModel.toggleWishlist() }) {
                                    Icon(
                                        imageVector = if (uiState.isInWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Избранное",
                                        tint = if (uiState.isInWishlist) Color.Red else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Content card
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .offset(y = (-16).dp)
                                .padding(horizontal = 20.dp)
                                .padding(top = 24.dp, bottom = 8.dp)
                        ) {
                            product.brand?.let {
                                Text(it.uppercase(), style = MaterialTheme.typography.labelMedium, color = OrangePrimary, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), lineHeight = 28.sp)
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${product.price.toLong()} ₽", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = OrangePrimary)
                                StockChip(stock = product.stock)
                            }
                            if (uiState.reviews.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                val avg = uiState.reviews.map { it.rating }.average()
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StarRow(rating = avg.toFloat(), size = 16.dp)
                                    Text("%.1f".format(avg), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                                    Text("· ${uiState.reviews.size} отз.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                                }
                            }
                        }
                    }

                    // Description
                    product.description?.let { desc ->
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
                                SectionTitle("Описание")
                                Spacer(Modifier.height(8.dp))
                                Text(desc, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF444444), lineHeight = 22.sp)
                            }
                        }
                    }

                    // Specs
                    if (product.specs.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                                SectionTitle("Характеристики")
                                Spacer(Modifier.height(10.dp))
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        product.specs.forEachIndexed { idx, spec ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(spec.key, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575), modifier = Modifier.weight(1f))
                                                Text(spec.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A), modifier = Modifier.weight(1f))
                                            }
                                            if (idx < product.specs.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reviews
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 8.dp)) {
                            SectionTitle("Отзывы")
                        }
                    }

                    if (uiState.reviews.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text("Отзывов пока нет", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9E9E9E))
                            }
                        }
                    } else {
                        items(uiState.reviews.size) { idx ->
                            ReviewCard(review = uiState.reviews[idx])
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(review.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
            Spacer(Modifier.height(2.dp))
            StarRow(rating = review.rating.toFloat(), size = 14.dp)
            review.comment?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFF444444), lineHeight = 20.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(review.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
        }
    }
}

@Composable
internal fun StarRow(rating: Float, size: androidx.compose.ui.unit.Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (star in 1..5) {
            Icon(
                imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (star <= rating) OrangePrimary else Color(0xFFCCCCCC),
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
}

@Composable
private fun StockChip(stock: Int) {
    val (bg, text, label) = if (stock > 0)
        Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "В наличии: $stock шт.")
    else
        Triple(Color(0xFFFFEBEE), Color(0xFFB71C1C), "Нет в наличии")
    Box(modifier = Modifier.background(bg, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = text, fontWeight = FontWeight.Medium)
    }
}
