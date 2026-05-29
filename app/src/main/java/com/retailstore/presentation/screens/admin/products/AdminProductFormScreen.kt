package com.retailstore.presentation.screens.admin.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.presentation.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductFormScreen(
    productId: String?,
    onBack: () -> Unit,
    viewModel: AdminProductFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.init(productId) }
    LaunchedEffect(uiState.success) { if (uiState.success) onBack() }
    LaunchedEffect(uiState.error) { uiState.error?.let { snackbarHostState.showSnackbar(it) } }

    var name by remember(uiState.product) { mutableStateOf(uiState.product?.name ?: "") }
    var description by remember(uiState.product) { mutableStateOf(uiState.product?.description ?: "") }
    var price by remember(uiState.product) { mutableStateOf(uiState.product?.price?.toString() ?: "") }
    var stock by remember(uiState.product) { mutableStateOf(uiState.product?.stock?.toString() ?: "") }
    var brand by remember(uiState.product) { mutableStateOf(uiState.product?.brand ?: "") }
    var model by remember(uiState.product) { mutableStateOf(uiState.product?.model ?: "") }
    var selectedCategoryId by remember(uiState.product) { mutableStateOf(uiState.product?.categoryId ?: 0) }
    var specs by remember(uiState.product) { mutableStateOf<List<Pair<String, String>>>(uiState.product?.specs?.map { it.key to it.value } ?: emptyList()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OrangePrimary,
        unfocusedBorderColor = Color(0xFFDDDDDD)
    )
    val fieldShape = RoundedCornerShape(12.dp)

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = if (productId == null) "Новый товар" else "Редактировать товар",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = uiState.categories.find { it.id == selectedCategoryId }?.name ?: "Выберите категорию",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = fieldShape,
                    colors = fieldColors
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    uiState.categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.name) }, onClick = {
                            selectedCategoryId = cat.id
                            categoryExpanded = false
                        })
                    }
                }
            }

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors)
            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Бренд") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors)
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Модель") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors)
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = fieldShape, colors = fieldColors)
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Цена (₽)") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Остаток на складе") }, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            OutlinedButton(
                onClick = { imageLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (imageUri != null) OrangePrimary else Color(0xFF757575))
            ) {
                Text(if (imageUri != null) "Фото выбрано ✓" else "Выбрать фото")
            }

            Text(
                "Характеристики",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            specs.forEachIndexed { idx, (key, value) ->
                var k by remember(key) { mutableStateOf(key) }
                var v by remember(value) { mutableStateOf(value) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = k,
                        onValueChange = { k = it; specs = specs.toMutableList().also { l -> l[idx] = k to v } },
                        label = { Text("Ключ") },
                        modifier = Modifier.weight(1f),
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = v,
                        onValueChange = { v = it; specs = specs.toMutableList().also { l -> l[idx] = k to v } },
                        label = { Text("Значение") },
                        modifier = Modifier.weight(1f),
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    IconButton(onClick = { specs = specs.toMutableList().also { it.removeAt(idx) } }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            OutlinedButton(
                onClick = { specs = specs + ("" to "") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Добавить характеристику")
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    viewModel.uploadImageAndSave(
                        imageUri = imageUri,
                        categoryId = selectedCategoryId,
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        stock = stock.toIntOrNull() ?: 0,
                        brand = brand,
                        model = model,
                        specs = specs.filter { it.first.isNotBlank() },
                        existingImageUrls = uiState.product?.imageUrls ?: emptyList(),
                        productId = productId
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                enabled = !uiState.loading && name.isNotBlank() && selectedCategoryId > 0
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Сохранить")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
