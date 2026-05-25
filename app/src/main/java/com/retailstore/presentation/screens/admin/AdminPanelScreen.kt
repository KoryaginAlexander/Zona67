package com.retailstore.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onProducts: () -> Unit,
    onOrders: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Панель администратора") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                onClick = onProducts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(36.dp))
                    Column {
                        Text("Управление товарами", style = MaterialTheme.typography.titleMedium)
                        Text("Создание, редактирование, деактивация", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Card(
                onClick = onOrders,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(36.dp))
                    Column {
                        Text("Управление заказами", style = MaterialTheme.typography.titleMedium)
                        Text("Просмотр, смена статуса, отмена", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
