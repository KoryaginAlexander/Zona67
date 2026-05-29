package com.retailstore.presentation.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.presentation.components.ProductCard
import com.retailstore.presentation.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (String) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyGridState = rememberLazyGridState()
    var showFilters by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.cartMessage) {
        uiState.cartMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCartMessage()
        }
    }

    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= uiState.products.size - 4) {
                    viewModel.loadNextPage()
                }
            }
    }

    if (showFilters) {
        FilterBottomSheet(
            currentMinPrice = uiState.minPrice,
            currentMaxPrice = uiState.maxPrice,
            currentInStockOnly = uiState.inStockOnly,
            currentSortBy = uiState.sortBy,
            onApply = { minPrice, maxPrice, inStockOnly, sortBy ->
                viewModel.setFilters(uiState.brand, minPrice, maxPrice, inStockOnly, sortBy)
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }

    val hasActiveFilters = uiState.minPrice != null || uiState.maxPrice != null || uiState.inStockOnly || uiState.sortBy != null

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val onSurface = MaterialTheme.colorScheme.onSurface
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("Zona")
                            }
                            withStyle(SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("67")
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::setSearch,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { searchFocused = it.isFocused },
                        placeholder = { Text("Поиск по названию, бренду...", color = Color(0xFF9E9E9E)) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF9E9E9E)) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearch("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = Color(0xFF9E9E9E))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    BadgedBox(
                        badge = {
                            if (hasActiveFilters) Badge(containerColor = OrangePrimary) {}
                        }
                    ) {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Фильтры",
                                tint = if (hasActiveFilters) OrangePrimary else Color(0xFF757575)
                            )
                        }
                    }
                }
                CategoryChips(
                    categories = uiState.categories,
                    selectedId = uiState.selectedCategory,
                    onSelect = viewModel::setCategory
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (searchFocused && uiState.searchQuery.isBlank() && searchHistory.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "История поиска",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF757575)
                        )
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Очистить", color = OrangePrimary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                items(searchHistory) { query ->
                    ListItem(
                        headlineContent = { Text(query, style = MaterialTheme.typography.bodyMedium) },
                        leadingContent = { Icon(Icons.Default.Search, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.clickable { viewModel.selectHistoryItem(query) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))
                }
            }
        } else if (uiState.loading && uiState.products.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (uiState.error != null && uiState.products.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Не удалось загрузить товары",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Button(
                        onClick = { viewModel.loadProducts(resetPage = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Повторить")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyGridState,
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = { viewModel.addToCart(product) },
                        isInWishlist = uiState.wishlistIds.contains(product.id),
                        onWishlistToggle = { viewModel.toggleWishlist(product) }
                    )
                }
                if (uiState.loading) {
                    item(span = { GridItemSpan(2) }) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = OrangePrimary)
                        }
                    }
                }
            }
        }
    }
}

private val SORT_OPTIONS = listOf(
    null to "По умолчанию",
    "price_asc" to "Цена: по возрастанию",
    "price_desc" to "Цена: по убыванию"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentMinPrice: Double?,
    currentMaxPrice: Double?,
    currentInStockOnly: Boolean,
    currentSortBy: String?,
    onApply: (Double?, Double?, Boolean, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var minPrice by remember { mutableStateOf(currentMinPrice?.toLong()?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(currentMaxPrice?.toLong()?.toString() ?: "") }
    var inStockOnly by remember { mutableStateOf(currentInStockOnly) }
    var sortBy by remember { mutableStateOf(currentSortBy) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Фильтры",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )

            // In stock toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Только в наличии",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1A1A1A)
                )
                Switch(
                    checked = inStockOnly,
                    onCheckedChange = { inStockOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OrangePrimary
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Sort
            Text(
                "Сортировка",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF757575)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SORT_OPTIONS.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sortBy = value },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortBy == value,
                            onClick = { sortBy = value },
                            colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (sortBy == value) OrangePrimary else Color(0xFF1A1A1A)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Price range
            Text(
                "Цена, ₽",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF757575)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { minPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("От") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                )
                Text("—", color = Color(0xFF757575))
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("До") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary)
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        minPrice = ""
                        maxPrice = ""
                        inStockOnly = false
                        sortBy = null
                        onApply(null, null, false, null)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Сбросить") }

                Button(
                    onClick = {
                        onApply(
                            minPrice.toDoubleOrNull(),
                            maxPrice.toDoubleOrNull(),
                            inStockOnly,
                            sortBy
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) { Text("Применить") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChips(
    categories: List<com.retailstore.domain.model.Category>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text("Все") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
        items(categories.size) { i ->
            val cat = categories[i]
            FilterChip(
                selected = selectedId == cat.id,
                onClick = { onSelect(cat.id) },
                label = { Text(cat.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
