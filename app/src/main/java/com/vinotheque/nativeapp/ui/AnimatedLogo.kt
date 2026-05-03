package com.vinotheque.nativeapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineRed
import kotlinx.coroutines.delay

@Composable
fun AnimatedVinothequeLogo(
    modifier: Modifier = Modifier,
    isSmall: Boolean = false,
    startDelay: Long = 0
) {
    val circleProgress = remember { Animatable(0f) }
    val bottleProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        delay(startDelay)
        circleProgress.animateTo(1f, animationSpec = tween(1200, easing = LinearOutSlowInEasing))
        bottleProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, animationSpec = tween(800))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSmall) 60.dp else 120.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                },
            contentAlignment = Alignment.Center
        ) {
            val goldColor = WineGold
            val redColor = WineRed
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val centerY = h / 2f
                val strokeWidth = (if (isSmall) 1.5.dp else 3.dp).toPx()

                // 1. Draw Circle/Arc (Draws in)
                val arcRect = Rect(Offset(w * 0.1f, h * 0.1f), Size(w * 0.8f, h * 0.8f))
                drawArc(
                    color = goldColor,
                    startAngle = -240f,
                    sweepAngle = 300f * circleProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = arcRect.topLeft,
                    size = arcRect.size
                )

                // 2. Draw "V" (Fades or stays)
                if (circleProgress.value > 0.5f) {
                    val vAlpha = ((circleProgress.value - 0.5f) * 2f).coerceIn(0f, 1f)
                    val vPath = Path().apply {
                        // Left stroke
                        moveTo(w * 0.25f, h * 0.3f)
                        lineTo(centerX - (if (isSmall) 2.dp else 4.dp).toPx(), h * 0.7f)
                        // Right stroke
                        moveTo(w * 0.75f, h * 0.3f)
                        lineTo(centerX + (if (isSmall) 2.dp else 4.dp).toPx(), h * 0.7f)
                    }
                    drawPath(
                        path = vPath,
                        color = goldColor.copy(alpha = vAlpha),
                        style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
                    )
                }

                // 3. Draw Bottle (Fills with wine)
                val bottleWidth = w * 0.25f
                val bottleHeight = h * 0.5f
                val bottleRect = Rect(
                    centerX - bottleWidth / 2f,
                    centerY - bottleHeight / 2f,
                    centerX + bottleWidth / 2f,
                    centerY + bottleHeight / 2f
                )

                val bottlePath = Path().apply {
                    // Simple bottle shape
                    moveTo(centerX - bottleWidth * 0.2f, bottleRect.top) // Neck top left
                    lineTo(centerX + bottleWidth * 0.2f, bottleRect.top) // Neck top right
                    lineTo(centerX + bottleWidth * 0.2f, bottleRect.top + bottleHeight * 0.3f) // Neck bottom right
                    // Shoulder and body
                    cubicTo(
                        centerX + bottleWidth * 0.5f, bottleRect.top + bottleHeight * 0.4f,
                        centerX + bottleWidth * 0.5f, bottleRect.bottom,
                        centerX, bottleRect.bottom // Taper to point like the image
                    )
                    cubicTo(
                        centerX - bottleWidth * 0.5f, bottleRect.bottom,
                        centerX - bottleWidth * 0.5f, bottleRect.top + bottleHeight * 0.4f,
                        centerX - bottleWidth * 0.2f, bottleRect.top + bottleHeight * 0.3f
                    )
                    close()
                }

                // Draw bottle outline
                drawPath(
                    path = bottlePath,
                    color = goldColor.copy(alpha = 0.3f),
                    style = Stroke(width = strokeWidth / 2f)
                )

                // Fill bottle (from bottom)
                if (bottleProgress.value > 0f) {
                    clipPath(bottlePath) {
                        val fillHeight = bottleHeight * bottleProgress.value
                        drawRect(
                            color = redColor,
                            topLeft = Offset(bottleRect.left, bottleRect.bottom - fillHeight),
                            size = Size(bottleWidth, fillHeight)
                        )
                    }
                }
                
                // 4. Corkscrew (Fades in)
                if (bottleProgress.value > 0.8f) {
                    val corkAlpha = ((bottleProgress.value - 0.8f) * 5f).coerceIn(0f, 1f)
                    val corkPath = Path().apply {
                        var currY = bottleRect.bottom
                        moveTo(centerX, currY)
                        for (i in 1..4) {
                            val side = if (i % 2 == 0) 1f else -1f
                            val step = (if (isSmall) 3.dp else 6.dp).toPx()
                            relativeQuadraticBezierTo(
                                side * step, step / 2f,
                                0f, step
                            )
                            currY += step
                        }
                    }
                    drawPath(
                        path = corkPath,
                        color = goldColor.copy(alpha = corkAlpha),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }

        if (!isSmall) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "VINOTHEQUE",
                color = goldColor.copy(alpha = textAlpha.value),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Text(
                text = "PRO",
                color = goldColor.copy(alpha = textAlpha.value * 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 10.sp
            )
        }
    }
}
