package com.example.kitaplikapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AuthBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val brush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1B0B3A), // koyu mor
            Color(0xFF2D145C), // orta mor
            Color(0xFF0F0724)  // daha koyu mor
        ),
        center = Offset(0.5f, 0.2f),
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush),
        content = content
    )
}
