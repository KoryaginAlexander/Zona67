package com.retailstore.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onMyOrders: () -> Unit,
    onAdminPanel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loggedOut) { if (uiState.loggedOut) onLogout() }
    LaunchedEffect(uiState.message) { uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() } }

    var editName by remember(uiState.user) { mutableStateOf(uiState.user?.fullName ?: "") }
    var editAddress by remember(uiState.user) { mutableStateOf(uiState.user?.address ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    if (!uiState.editing) {
                        IconButton(onClick = { viewModel.setEditing(true) }) { Icon(Icons.Default.Edit, null) }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.user?.let { user ->
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                if (uiState.editing) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Адрес доставки") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        minLines = 2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.setEditing(false) }, modifier = Modifier.weight(1f)) { Text("Отмена") }
                        Button(onClick = { viewModel.updateProfile(editName, editAddress) }, modifier = Modifier.weight(1f)) { Text("Сохранить") }
                    }
                } else {
                    ListItem(headlineContent = { Text("Имя") }, supportingContent = { Text(user.fullName ?: "—") })
                    ListItem(headlineContent = { Text("Адрес") }, supportingContent = { Text(user.address ?: "—") })
                }
            }
            Divider()
            Button(onClick = onMyOrders, modifier = Modifier.fillMaxWidth()) { Text("Мои заказы") }
            if (uiState.user?.isAdmin == true) {
                Button(onClick = onAdminPanel, modifier = Modifier.fillMaxWidth()) { Text("Панель администратора") }
            }
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Выйти")
            }
        }
    }
}
