package com.retailstore.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.presentation.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onMyOrders: () -> Unit,
    onAdminPanel: () -> Unit,
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loggedOut) { if (uiState.loggedOut) onLogout() }
    LaunchedEffect(uiState.message) { uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() } }

    var editName by remember(uiState.user) { mutableStateOf(uiState.user?.fullName ?: "") }
    var editAddress by remember(uiState.user) { mutableStateOf(uiState.user?.address ?: "") }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
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
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                )
                if (!uiState.editing && uiState.user != null) {
                    IconButton(
                        onClick = { viewModel.setEditing(true) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = OrangePrimary)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Тема всегда доступна
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Тёмная тема", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangePrimary
                        )
                    )
                }
            }

            if (!uiState.loading && uiState.user == null) {
                // Гостевое состояние
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color(0xFFBBBBBB)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Войдите в аккаунт",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Чтобы делать заказы,\nотслеживать историю и добавлять в избранное",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) { Text("Войти") }
                    OutlinedButton(
                        onClick = onRegister,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Зарегистрироваться") }
                }
            }

            uiState.user?.let { user ->
                // Avatar + email header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user.fullName?.firstOrNull() ?: user.email.firstOrNull() ?: '?')
                                .uppercaseChar().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.fullName ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575)
                        )
                    }
                }

                if (uiState.editing) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Имя") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            OutlinedTextField(
                                value = editAddress,
                                onValueChange = { editAddress = it },
                                label = { Text("Адрес доставки") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.setEditing(false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Отмена") }
                                Button(
                                    onClick = { viewModel.updateProfile(editName, editAddress) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) { Text("Сохранить") }
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            ListItem(
                                headlineContent = { Text("Имя", style = MaterialTheme.typography.labelMedium, color = Color(0xFF757575)) },
                                supportingContent = { Text(user.fullName ?: "—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            ListItem(
                                headlineContent = { Text("Адрес", style = MaterialTheme.typography.labelMedium, color = Color(0xFF757575)) },
                                supportingContent = { Text(user.address ?: "—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                            )
                        }
                    }
                }
            }

            if (uiState.user != null) {
                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onMyOrders,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) { Text("Мои заказы") }

                if (uiState.user?.isAdmin == true) {
                    Button(
                        onClick = onAdminPanel,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                    ) { Text("Панель администратора") }
                }

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}
