package com.retailstore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OrderStatusChip(status: String) {
    val (color, label) = when (status) {
        "PENDING" -> Color(0xFFFFF3E0) to "Новый"
        "CONFIRMED" -> Color(0xFFE3F2FD) to "Подтверждён"
        "PROCESSING" -> Color(0xFFF3E5F5) to "В обработке"
        "SHIPPED" -> Color(0xFFE8F5E9) to "Отправлен"
        "DELIVERED" -> Color(0xFF2E7D32) to "Доставлен"
        "CANCELLED" -> Color(0xFFFFEBEE) to "Отменён"
        else -> Color(0xFFEEEEEE) to status
    }
    val textColor = if (status == "DELIVERED") Color.White else Color.Black

    Text(
        text = label,
        modifier = Modifier
            .background(color, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = textColor
    )
}
