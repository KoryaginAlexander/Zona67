package com.retailstore.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.retailstore.domain.model.CartItem

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.productImageUrl,
            contentDescription = item.productName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Text("${item.productPrice.toLong()} ₽", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                }
                Text("${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                }
            }
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
        }
    }
}
