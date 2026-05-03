package com.vinotheque.nativeapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.vinotheque.nativeapp.ui.theme.WineGold

@Composable
fun AnimatedVinothequeLogo(modifier: Modifier = Modifier.size(120.dp)) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val goldGradients = listOf(
        WineGold,
        Color(0xFFFFD700), // Pure Gold
        Color(0xFFB8860B), // Dark Goldenrod
        WineGold
    )

    Canvas(modifier = modifier) {
        val centerPos = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.5f * scale
        
        // Outer ring
        rotate(rotation) {
            drawCircle(
                brush = Brush.sweepGradient(goldGradients),
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        // Inner "V" or stylized emblem
        rotate(-rotation / 2) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WineGold.copy(alpha = 0.3f), Color.Transparent),
                    center = centerPos,
                    radius = radius * 0.8f
                ),
                radius = radius * 0.8f,
                center = centerPos
            )
        }
        
        // The core glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(WineGold.copy(alpha = 0.8f), Color.Transparent),
                center = centerPos,
                radius = radius * 0.4f
            ),
            radius = radius * 0.4f,
            center = centerPos
        )
    }
}
