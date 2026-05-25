package com.retailstore.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    error = Color(0xFFB00020)
)

@Composable
fun RetailStoreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
