package com.retailstore.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OrangePrimary = Color(0xFFFF6B00)
val DarkText = Color(0xFF1A1A1A)
val SurfaceGray = Color(0xFFF5F5F7)

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDC0),
    onPrimaryContainer = DarkText,
    secondary = DarkText,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = DarkText,
    surface = SurfaceGray,
    onSurface = DarkText,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = Color(0xFF757575),
    error = Color(0xFFB00020)
)

@Composable
fun RetailStoreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
