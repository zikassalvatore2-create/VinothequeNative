package com.vinotheque.nativeapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
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
    val doorRotation = remember { Animatable(0f) } // 0 to 90
    val glowIntensity = remember { Animatable(0f) } // 0 to 1
    val textAlpha = remember { Animatable(0f) } // 0 to 1
    
    val infiniteTransition = rememberInfiniteTransition(label = "flicker")
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker"
    )

    LaunchedEffect(Unit) {
        delay(startDelay)
        // 1. Doors open
        doorRotation.animateTo(1f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
        // 2. Light glows
        glowIntensity.animateTo(1f, animationSpec = tween(1000))
        // 3. Text fades in
        textAlpha.animateTo(1f, animationSpec = tween(1000))
    }

    val goldColor = WineGold
    val darkGold = goldColor.copy(alpha = 0.6f)
    val wineColor = WineRed

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(if (isSmall) 80.dp else 160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val centerY = h / 2f
                
                // Arch dimensions
                val archWidth = w * 0.7f
                val archHeight = h * 0.8f
                val archRect = androidx.compose.ui.geometry.Rect(
                    centerX - archWidth / 2f,
                    h * 0.1f,
                    centerX + archWidth / 2f,
                    h * 0.1f + archHeight
                )

                // 1. Draw the Stone Archway (Background)
                val archPath = Path().apply {
                    addArc(
                        androidx.compose.ui.geometry.Rect(
                            archRect.left, archRect.top, archRect.right, archRect.top + archWidth
                        ),
                        180f, 180f
                    )
                    lineTo(archRect.right, archRect.bottom)
                    lineTo(archRect.left, archRect.bottom)
                    close()
                }

                // Draw arch stones (segments)
                val strokeWidth = (if (isSmall) 2.dp else 4.dp).toPx()
                drawPath(archPath, goldColor, style = Stroke(width = strokeWidth))
                
                // Add stone lines
                for (i in 0..10) {
                    val angle = 180f + i * 18f
                    val rad = Math.toRadians(angle.toDouble())
                    val x1 = centerX + (archWidth / 2f) * Math.cos(rad).toFloat()
                    val y1 = (archRect.top + archWidth / 2f) + (archWidth / 2f) * Math.sin(rad).toFloat()
                    val x2 = centerX + (archWidth / 2f + strokeWidth * 2) * Math.cos(rad).toFloat()
                    val y2 = (archRect.top + archWidth / 2f) + (archWidth / 2f + strokeWidth * 2) * Math.sin(rad).toFloat()
                    drawLine(goldColor, androidx.compose.ui.geometry.Offset(x1, y1), androidx.compose.ui.geometry.Offset(x2, y2), strokeWidth / 2f)
                }

                // 2. Draw Shelves and Bottles (Inside)
                clipPath(archPath) {
                    drawRect(Color.Black.copy(alpha = 0.8f))
                    // Grid
                    val rows = 4
                    val cols = 4
                    val cellW = archWidth / cols
                    val cellH = (archHeight - archWidth / 2f) / rows
                    val startY = archRect.top + archWidth / 2f
                    
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val x = archRect.left + c * cellW
                            val y = startY + r * cellH
                            // Draw a tiny bottle silhouette
                            val bW = cellW * 0.4f
                            val bH = cellH * 0.6f
                            drawRoundRect(
                                wineColor.copy(alpha = 0.3f),
                                androidx.compose.ui.geometry.Offset(x + (cellW - bW) / 2f, y + (cellH - bH) / 2f),
                                androidx.compose.ui.geometry.Size(bW, bH),
                                androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                        }
                    }
                    
                    // Shelving lines
                    for (i in 1 until rows) {
                        val y = startY + i * cellH
                        drawLine(darkGold, androidx.compose.ui.geometry.Offset(archRect.left, y), androidx.compose.ui.geometry.Offset(archRect.right, y), 1f)
                    }
                    for (i in 1 until cols) {
                        val x = archRect.left + i * cellW
                        drawLine(darkGold, androidx.compose.ui.geometry.Offset(x, startY), androidx.compose.ui.geometry.Offset(x, archRect.bottom), 1f)
                    }
                }

                // 3. Draw Center Bottle & Glow
                val mainBottleW = archWidth * 0.15f
                val mainBottleH = archHeight * 0.4f
                val mainBottleX = centerX - mainBottleW / 2f
                val mainBottleY = archRect.bottom - mainBottleH - 4.dp.toPx()

                if (glowIntensity.value > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(goldColor.copy(alpha = 0.4f * glowIntensity.value * flicker), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(centerX, mainBottleY + mainBottleH / 2f),
                            radius = archWidth * 0.4f
                        ),
                        radius = archWidth * 0.4f,
                        center = androidx.compose.ui.geometry.Offset(centerX, mainBottleY + mainBottleH / 2f)
                    )
                }

                // Bottle outline
                drawRoundRect(
                    goldColor,
                    androidx.compose.ui.geometry.Offset(mainBottleX, mainBottleY),
                    androidx.compose.ui.geometry.Size(mainBottleW, mainBottleH),
                    androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
                // Bottle neck
                drawRect(
                    goldColor,
                    androidx.compose.ui.geometry.Offset(centerX - mainBottleW * 0.2f, mainBottleY - mainBottleH * 0.15f),
                    androidx.compose.ui.geometry.Size(mainBottleW * 0.4f, mainBottleH * 0.15f)
                )

                // 4. Draw Doors (Animated)
                val doorW = archWidth / 2f
                val doorH = archHeight
                
                // Left Door
                rotate(degrees = -90f * doorRotation.value, pivot = androidx.compose.ui.geometry.Offset(archRect.left, centerY)) {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(goldColor.copy(alpha = 0.9f), darkGold)),
                        topLeft = androidx.compose.ui.geometry.Offset(archRect.left, archRect.top),
                        size = androidx.compose.ui.geometry.Size(doorW, doorH)
                    )
                    drawRect(
                        goldColor,
                        topLeft = androidx.compose.ui.geometry.Offset(archRect.left, archRect.top),
                        size = androidx.compose.ui.geometry.Size(doorW, doorH),
                        style = Stroke(width = strokeWidth / 2f)
                    )
                }
                
                // Right Door
                rotate(degrees = 90f * doorRotation.value, pivot = androidx.compose.ui.geometry.Offset(archRect.right, centerY)) {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(darkGold, goldColor.copy(alpha = 0.9f))),
                        topLeft = androidx.compose.ui.geometry.Offset(centerX, archRect.top),
                        size = androidx.compose.ui.geometry.Size(doorW, doorH)
                    )
                    drawRect(
                        goldColor,
                        topLeft = androidx.compose.ui.geometry.Offset(centerX, archRect.top),
                        size = androidx.compose.ui.geometry.Size(doorW, doorH),
                        style = Stroke(width = strokeWidth / 2f)
                    )
                }
            }
        }

        if (!isSmall) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { alpha = textAlpha.value }) {
                Text(
                    text = "VINOTHEQUE",
                    color = goldColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp
                )
                
                // Decorative line with grapes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(40.dp).height(1.dp).graphicsLayer { background(goldColor) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("\uD83C\uDF47", fontSize = 16.sp) // Grape emoji
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(40.dp).height(1.dp).graphicsLayer { background(goldColor) })
                }
            }
        }
    }
}
