package com.vinotheque.nativeapp.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Dynamic gold color shifting
    val colorShift by infiniteTransition.animateColor(
        initialValue = WineGold,
        targetValue = Color(0xFFFFD700), // Bright gold
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )

    Canvas(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = 0.99f // Force software layer for potentially smoother edges
            }
    ) {
        val centerPos = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.7f
        
        // Deep background glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorShift.copy(alpha = 0.2f), Color.Transparent),
                center = centerPos,
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f,
            center = centerPos
        )

        // Outer rotating ring
        rotate(rotation) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(colorShift, Color(0xFFB8860B), colorShift)
                ),
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        // Secondary counter-rotating accent
        rotate(-rotation * 0.5f) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, colorShift.copy(alpha = 0.3f), Color.Transparent)
                ),
                radius = radius + 6.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Polished core
        rotate(-rotation / 1.5f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colorShift.copy(alpha = 0.5f), Color.Transparent),
                    center = centerPos,
                    radius = radius * 0.85f
                ),
                radius = radius * 0.85f,
                center = centerPos
            )
        }
        
        // Central high-light
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, colorShift.copy(alpha = 0.4f), Color.Transparent),
                center = centerPos,
                radius = radius * 0.4f
            ),
            radius = radius * 0.4f,
            center = centerPos
        )
    }
}
