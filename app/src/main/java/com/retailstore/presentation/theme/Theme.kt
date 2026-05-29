package com.retailstore.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
    background = Color(0xFFF2F2F2),
    onBackground = DarkText,
    surface = Color.White,
    onSurface = DarkText,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = Color(0xFF757575),
    surfaceTint = Color.White,
    error = Color(0xFFB00020)
)

private val DarkColors = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5A2800),
    onPrimaryContainer = Color(0xFFFFDDC0),
    secondary = Color(0xFFEEEEEE),
    onSecondary = Color(0xFF1A1A1A),
    background = Color(0xFF111111),
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF252525),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFBBBBBB),
    outline = Color(0xFF666666),
    surfaceTint = Color(0xFF252525),
    error = Color(0xFFCF6679)
)

@Composable
fun RetailStoreTheme(isDarkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColors else LightColors,
        content = content
    )
}
