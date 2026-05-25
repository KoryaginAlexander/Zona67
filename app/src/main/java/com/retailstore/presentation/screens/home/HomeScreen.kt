package com.retailstore.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.retailstore.domain.model.Category
import com.retailstore.domain.model.Product
import com.retailstore.presentation.theme.OrangePrimary
import com.retailstore.presentation.theme.SurfaceGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onSearch: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.cartMessage) {
        uiState.cartMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCartMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("Zona")
                            }
                            withStyle(SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("67")
                            }
                        }
                    )
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    placeholder = { Text("Поиск техники Zona67...", color = Color(0xFF9E9E9E)) },
                    leadingIcon = {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            if (searchQuery.isNotBlank()) onSearch(searchQuery)
                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = if (searchQuery.isNotBlank()) OrangePrimary else Color(0xFF9E9E9E)
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        if (searchQuery.isNotBlank()) onSearch(searchQuery)
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SurfaceGray,
                        unfocusedContainerColor = SurfaceGray
                    )
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            CategorySection(
                categories = uiState.categories,
                selectedId = uiState.selectedCategory,
                onSelect = viewModel::filterByCategory
            )
            Spacer(Modifier.height(20.dp))
            FeaturedSection(
                products = uiState.featuredProducts,
                loading = uiState.loading,
                wishlistIds = uiState.wishlistIds,
                onProductClick = onProductClick,
                onAddToCart = { viewModel.addToCart(it) },
                onWishlistToggle = { viewModel.toggleWishlist(it) }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategorySection(
    categories: List<Category>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit
) {
    Column {
        Text(
            text = "Категории",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { CategoryItem(name = "Все", icon = Icons.Default.Apps, selected = selectedId == null) { onSelect(null) } }
            items(categories.size) { i ->
                val cat = categories[i]
                CategoryItem(
                    name = cat.name,
                    icon = categoryIcon(cat.name),
                    selected = selectedId == cat.id,
                    onClick = { onSelect(cat.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(name: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (selected) OrangePrimary else SurfaceGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = name,
                tint = if (selected) Color.White else Color(0xFF757575),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp)
        )
    }
}

private fun categoryIcon(name: String): ImageVector = when {
    name.contains("смарт", ignoreCase = true) || name.contains("телефон", ignoreCase = true) -> Icons.Default.PhoneAndroid
    name.contains("ноутбук", ignoreCase = true) -> Icons.Default.Laptop
    name.contains("планшет", ignoreCase = true) -> Icons.Default.TabletMac
    name.contains("наушник", ignoreCase = true) || name.contains("аудио", ignoreCase = true) -> Icons.Default.Headset
    name.contains("аксессуар", ignoreCase = true) -> Icons.Default.Cable
    name.contains("ТВ", ignoreCase = true) || name.contains("телевизор", ignoreCase = true) -> Icons.Default.Tv
    else -> Icons.Default.Devices
}

@Composable
private fun FeaturedSection(
    products: List<Product>,
    loading: Boolean,
    wishlistIds: Set<String>,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onWishlistToggle: (Product) -> Unit
) {
    Column {
        Text(
            text = "Рекомендуем",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        if (loading && products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products.size) { i ->
                    HomeProductCard(
                        product = products[i],
                        isInWishlist = wishlistIds.contains(products[i].id),
                        onClick = { onProductClick(products[i].id) },
                        onBuy = { onAddToCart(products[i]) },
                        onWishlistToggle = { onWishlistToggle(products[i]) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeProductCard(
    product: Product,
    isInWishlist: Boolean,
    onClick: () -> Unit,
    onBuy: () -> Unit,
    onWishlistToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Box {
                AsyncImage(
                    model = product.firstImageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    product.brand?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${product.price.toLong()} ₽",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }
                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = product.isInStock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        disabledContainerColor = Color(0xFFDDDDDD)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = if (product.isInStock) "В корзину" else "Нет",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
