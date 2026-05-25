package com.retailstore.presentation.screens.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductListScreen(
    onCreateProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: AdminProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Товары") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateProduct) { Icon(Icons.Default.Add, "Добавить товар") }
        }
    ) { padding ->
        when {
            uiState.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.products) { product ->
                    ListItem(
                        headlineContent = { Text(product.name) },
                        supportingContent = {
                            Column {
                                Text("${product.price.toLong()} ₽ | Остаток: ${product.stock}", style = MaterialTheme.typography.bodySmall)
                                if (!product.isActive) {
                                    Text("Деактивирован", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { onEditProduct(product.id) }) { Icon(Icons.Default.Edit, null) }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
