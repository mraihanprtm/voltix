package com.example.voltix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Definisikan warna-warna yang akan digunakan di tema
private val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun VoltixTheme(content: @Composable () -> Unit) {
    // Gunakan MaterialTheme dengan warna yang telah didefinisikan
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
