package com.retailstore.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.retailstore.domain.model.Product
import com.retailstore.presentation.theme.OrangePrimary

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
    isInWishlist: Boolean = false,
    onWishlistToggle: (() -> Unit)? = null,
    index: Int? = null
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.firstImageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
                if (onWishlistToggle != null) {
                    IconButton(
                        onClick = onWishlistToggle,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isInWishlist) "Убрать из избранного" else "В избранное",
                            tint = if (isInWishlist) Color.Red else Color.White
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                product.brand?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${product.price.toLong()} ₽",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(11.dp))
                    Text(
                        if (product.averageRating > 0) "%.1f".format(product.averageRating) else "0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF757575)
                    )
                    Text(
                        "(${product.reviewCount} отз.)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
                Spacer(Modifier.height(2.dp))
                StockBadge(stock = product.stock)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = product.isInStock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        disabledContainerColor = Color(0xFFDDDDDD)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(
                        text = if (product.isInStock) "В корзину" else "Нет в наличии",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
