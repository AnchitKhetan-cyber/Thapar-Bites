package com.ccs.thaparbites.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = Color.White.copy(alpha = 0.04f),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.10f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}

@Composable
fun glassTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        // Border
        focusedBorderColor = Color.White.copy(alpha = 0.35f),
        unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
        errorBorderColor = Color(0xFFFF6B6B),

        // Container
        focusedContainerColor = Color.White.copy(alpha = 0.03f),
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,

        // Text
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White.copy(alpha = 0.95f),

        // Label
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.65f),

        // Placeholder
        focusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.45f),

        // Cursor
        cursorColor = Color.White,

        // Icons
        focusedLeadingIconColor = Color.White,
        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.65f),

        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.65f)
    )