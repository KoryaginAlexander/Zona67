package com.retailstore.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.retailstore.domain.model.CartItem
import com.retailstore.presentation.theme.OrangePrimary

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    val atMaxStock = item.quantity >= item.stock

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            Text(
                item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            Text(
                "${item.productPrice.toLong()} ₽",
                style = MaterialTheme.typography.titleSmall,
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold
            )
            if (atMaxStock) {
                Text(
                    "Максимум: ${item.stock} шт.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .wrapContentWidth()
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Уменьшить",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF1A1A1A)
                    )
                }
                Text(
                    "${item.quantity}",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(32.dp),
                    enabled = !atMaxStock
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Увеличить",
                        modifier = Modifier.size(16.dp),
                        tint = if (atMaxStock) Color(0xFFCCCCCC) else Color(0xFF1A1A1A)
                    )
                }
            }
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
        }
    }
}
