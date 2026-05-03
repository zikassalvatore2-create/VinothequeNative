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
    
    // Animation for the liquid level oscillation
    val liquidOscillation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquid"
    )

    // Animation for the "glint" on the glass rim
    val glintPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glint"
    )

    // Animation for subtle glow pulse
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(
        modifier = modifier
            .graphicsLayer {
                alpha = 0.99f
            }
    ) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f
        
        // 1. Draw the Background Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(centerX, h * 0.45f),
                radius = (w * 0.45f) * glowScale
            ),
            radius = (w * 0.45f) * glowScale,
            center = Offset(centerX, h * 0.45f)
        )

        // 2. Define the Glass Bowl Path
        val bowlPath = Path().apply {
            moveTo(w * 0.3f, h * 0.25f) // Top left rim
            cubicTo(
                w * 0.3f, h * 0.65f, // Control point 1
                w * 0.7f, h * 0.65f, // Control point 2
                w * 0.7f, h * 0.25f  // Top right rim
            )
        }

        // 3. Draw the "Liquid" inside the bowl
        val liquidPath = Path().apply {
            val liquidLevel = h * 0.45f + liquidOscillation
            moveTo(w * 0.35f, liquidLevel)
            cubicTo(
                w * 0.35f, h * 0.62f,
                w * 0.65f, h * 0.62f,
                w * 0.65f, liquidLevel
            )
            // Surface curve
            quadraticBezierTo(centerX, liquidLevel - 4.dp.toPx(), w * 0.35f, liquidLevel)
        }
        
        drawPath(
            path = liquidPath,
            brush = Brush.verticalGradient(
                listOf(primaryColor.copy(alpha = 0.4f), primaryColor.copy(alpha = 0.1f))
            )
        )

        // 4. Draw Glass Outline (Stem and Base)
        val glassOutlineColor = primaryColor.copy(alpha = 0.8f)
        val strokeWidth = 1.5.dp.toPx()

        // Stem
        drawLine(
            color = glassOutlineColor,
            start = Offset(centerX, h * 0.58f),
            end = Offset(centerX, h * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Base
        drawPath(
            path = Path().apply {
                moveTo(w * 0.35f, h * 0.85f)
                quadraticBezierTo(centerX, h * 0.82f, w * 0.65f, h * 0.85f)
            },
            color = glassOutlineColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bowl Outline
        drawPath(
            path = bowlPath,
            color = glassOutlineColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 5. Elegant "V" logo inside the glass
        drawPath(
            path = Path().apply {
                moveTo(w * 0.45f, h * 0.35f)
                lineTo(centerX, h * 0.48f)
                lineTo(w * 0.55f, h * 0.35f)
            },
            color = primaryColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // 6. Rim "Glint" Animation
        // We find a point along the top arc for the glint
        val glintX = centerX + (w * 0.2f) * Math.cos(Math.PI + glintPosition * Math.PI).toFloat()
        val glintY = h * 0.25f + (h * 0.05f) * Math.sin(glintPosition * Math.PI * 2).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.9f), Color.Transparent),
                center = Offset(glintX, glintY),
                radius = 4.dp.toPx()
            ),
            radius = 4.dp.toPx(),
            center = Offset(glintX, glintY)
        )

        // 7. Subtle rising bubbles
        val time = System.currentTimeMillis()
        for (i in 0..2) {
            val bubbleX = centerX + (i - 1) * 12.dp.toPx() + (Math.sin(time / 500.0 + i) * 4.dp.toPx()).toFloat()
            val bubbleY = h * 0.55f - ((time / 20 + i * 100) % 100) / 100f * (h * 0.2f)
            
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 1.dp.toPx(),
                center = Offset(bubbleX, bubbleY)
            )
        }
    }
}
