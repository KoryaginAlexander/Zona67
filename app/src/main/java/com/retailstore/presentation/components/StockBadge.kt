package com.retailstore.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun StockBadge(stock: Int) {
    if (stock > 0) {
        Text(
            "В наличии: $stock шт.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF2E7D32)
        )
    } else {
        Text(
            "Нет в наличии",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
