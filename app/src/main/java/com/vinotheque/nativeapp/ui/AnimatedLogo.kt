package com.vinotheque.nativeapp.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun AnimatedVinothequeLogo(modifier: Modifier = Modifier.size(120.dp)) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val colorShift by infiniteTransition.animateColor(
        initialValue = primaryColor,
        targetValue = primaryColor.copy(alpha = 0.6f),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )

    Canvas(
        modifier = modifier
            .graphicsLayer {
                alpha = 0.99f
            }
    ) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        
        // 1. Arch Path (Stone Cellar Arch)
        val archPath = Path().apply {
            moveTo(w * 0.15f, h * 0.9f)
            lineTo(w * 0.15f, h * 0.5f)
            cubicTo(
                w * 0.15f, h * 0.1f,
                w * 0.85f, h * 0.1f,
                w * 0.85f, h * 0.5f
            )
            lineTo(w * 0.85f, h * 0.9f)
        }

        // Deep background glow behind the arch
        drawPath(
            path = archPath,
            brush = Brush.verticalGradient(
                listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)
            )
        )

        // 2. Draw wine bottles inside the arch (Neatly arranged)
        val bottleWidth = w * 0.08f
        val bottleHeight = h * 0.25f
        val startX = w * 0.25f
        val endX = w * 0.75f
        val bottleCount = 5
        val spacing = (endX - startX) / (bottleCount - 1)

        for (i in 0 until bottleCount) {
            val x = startX + i * spacing
            // Bottle body
            drawRoundRect(
                color = colorShift.copy(alpha = 0.6f),
                topLeft = Offset(x - bottleWidth / 2, h * 0.55f),
                size = Size(bottleWidth, bottleHeight),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
            // Bottle neck
            drawRect(
                color = colorShift.copy(alpha = 0.6f),
                topLeft = Offset(x - bottleWidth / 4, h * 0.45f),
                size = Size(bottleWidth / 2, h * 0.1f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 3. Draw the Arch line art
        drawPath(
            path = archPath,
            color = colorShift,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // 4. Subtle "Stone" markers on the arch
        for (i in 0..8) {
            val angle = Math.PI + (i * Math.PI / 8)
            val rx = w * 0.35f
            val ry = h * 0.4f
            val px = centerX + rx * Math.cos(angle).toFloat()
            val py = h * 0.5f + ry * Math.sin(angle).toFloat()
            
            drawCircle(
                color = colorShift.copy(alpha = glowAlpha),
                radius = 2.dp.toPx(),
                center = Offset(px, py)
            )
        }

        // 5. Refined central glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorShift.copy(alpha = 0.15f * glowAlpha), Color.Transparent),
                center = Offset(centerX, h * 0.4f),
                radius = w * 0.4f
            ),
            radius = w * 0.4f,
            center = Offset(centerX, h * 0.4f)
        )
    }
}
