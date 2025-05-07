package com.example.voltix.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tipografi dasar Material 3
val Typography = Typography(
    headlineSmall = TextStyle( // setara dengan h6
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    // Tambahkan lainnya sesuai kebutuhan
)
