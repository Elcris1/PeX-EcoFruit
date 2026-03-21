package com.example.ecofruit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GeneralButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(colorScheme.primary, colorScheme.secondary)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = typography.labelLarge.copy(
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun OutlinedGeneralButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent
        )
    ) {
        Text(
            text,
            style = typography.labelLarge.copy(color = colorScheme.onSurfaceVariant)
        )
    }
}


@Composable
fun BubbleBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
){
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(colors.primaryContainer.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 70.dp)
                .clip(CircleShape)
                .background(colors.secondaryContainer.copy(alpha = 0.35f))
        )
        content()
    }
}


@Composable
fun AnimatedCheck() {
    // Animación de aparición del check
    val checkScale = remember { Animatable(0f) }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        checkScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .graphicsLayer { scaleX = checkScale.value; scaleY = checkScale.value }
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            colorScheme.primaryContainer,
                            Color.Transparent
                        )
                    ),
                    radius = 60.dp.toPx()
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        listOf(colorScheme.primary, colorScheme.secondary),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    radius = 36.dp.toPx()
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text("✓", style = TextStyle(fontSize = 28.sp, color = colorScheme.onPrimary, fontWeight = FontWeight.Black))
    }
}
