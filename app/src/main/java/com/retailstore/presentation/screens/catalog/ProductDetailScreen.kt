package com.retailstore.presentation.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.retailstore.presentation.components.StockBadge

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
        topBar = {
            TopAppBar(
                title = { Text(uiState.product?.name ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist() }) {
                        Icon(Icons.Default.FavoriteBorder, "Избранное")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.product != null -> {
                val product = uiState.product!!
                LazyColumn(modifier = Modifier.padding(padding)) {
                    item {
                        AsyncImage(
                            model = product.firstImageUrl,
                            contentDescription = product.name,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        )
                    }
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            product.brand?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
                            Text(product.name, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${product.price.toLong()} ₽",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            StockBadge(product.stock)
                            Spacer(Modifier.height(16.dp))
                            product.description?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(16.dp))
                            }
                            if (product.specs.isNotEmpty()) {
                                Text("Характеристики", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                product.specs.forEach { spec ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                        Text(spec.key, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                        Text(spec.value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Divider()
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                            Button(
                                onClick = { viewModel.addToCart() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = product.isInStock
                            ) {
                                Text(if (product.isInStock) "Добавить в корзину" else "Нет в наличии")
                            }
                        }
                    }
                }
            }
        }
    }
}
