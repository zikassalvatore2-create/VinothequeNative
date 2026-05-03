package com.vinotheque.nativeapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineRed
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AnimatedVinothequeLogo(
    modifier: Modifier = Modifier,
    isSmall: Boolean = false,
    startDelay: Long = 0
) {
    val doorRotation = remember { Animatable(0f) }
    val glowIntensity = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val dustAlpha = remember { Animatable(0f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "cinematic")
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker"
    )

    // Dust particles state (Position to Speed)
    val particles = remember {
        List<Pair<Offset, Float>>(30) {
            Offset(Random.nextFloat(), Random.nextFloat()) to Random.nextFloat()
        }
    }

    LaunchedEffect(Unit) {
        delay(startDelay)
        // cinematic sequence
        doorRotation.animateTo(1f, animationSpec = tween(2500, easing = FastOutSlowInEasing))
        dustAlpha.animateTo(0.6f, animationSpec = tween(2000))
        glowIntensity.animateTo(1f, animationSpec = tween(1500))
        textAlpha.animateTo(1f, animationSpec = tween(1500))
    }

    val goldColor = WineGold
    val warmLight = Color(0xFFFFD700)
    val woodColor = Color(0xFF3E2723)
    val woodColorLight = Color(0xFF5D4037)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(if (isSmall) 100.dp else 240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val centerY = h / 2f
                
                // 1. FLOOR REFLECTION (Drawn first to be behind)
                translate(top = h * 0.15f) {
                    scale(1f, -0.4f, pivot = Offset(centerX, h * 0.8f)) {
                        // We will draw a simplified version of the arch here later if needed
                        // For now, just a gradient "puddle"
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(goldColor.copy(alpha = 0.1f * glowIntensity.value), Color.Transparent),
                                center = Offset(centerX, h * 0.8f),
                                radius = w * 0.4f
                            ),
                            topLeft = Offset(centerX - w * 0.4f, h * 0.7f),
                            size = Size(w * 0.8f, h * 0.2f)
                        )
                    }
                }

                // Arch dimensions
                val archWidth = w * 0.65f
                val archHeight = h * 0.7f
                val archRect = Rect(
                    centerX - archWidth / 2f,
                    h * 0.15f,
                    centerX + archWidth / 2f,
                    h * 0.15f + archHeight
                )

                val archPath = Path().apply {
                    addArc(Rect(archRect.left, archRect.top, archRect.right, archRect.top + archWidth), 180f, 180f)
                    lineTo(archRect.right, archRect.bottom)
                    lineTo(archRect.left, archRect.bottom)
                    close()
                }

                // 2. WARM LIGHT SPILL
                if (glowIntensity.value > 0f) {
                    drawPath(
                        path = archPath,
                        brush = Brush.radialGradient(
                            colors = listOf(warmLight.copy(alpha = 0.3f * glowIntensity.value * flicker), Color.Transparent),
                            center = Offset(centerX, archRect.top + archWidth / 2f),
                            radius = archWidth * 1.2f
                        )
                    )
                }

                // 3. BACKGROUND (Inside Cellar)
                clipPath(archPath) {
                    drawRect(Color.Black)
                    // Shelves grid (Cinematic organization)
                    val rows = 5
                    val cols = 6
                    val cellW = archWidth / cols
                    val cellH = (archHeight - archWidth / 2f) / rows
                    val startY = archRect.top + archWidth / 2f
                    
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val x = archRect.left + c * cellW
                            val y = startY + r * cellH
                            // Organized bottles
                            drawOval(
                                Color(0xFF1A1A1A),
                                Offset(x + cellW * 0.3f, y + cellH * 0.2f),
                                Size(cellW * 0.4f, cellH * 0.7f)
                            )
                        }
                    }
                    
                    // Shelving lines (Fine details)
                    for (i in 0..rows) {
                        val y = startY + i * cellH
                        drawLine(Color(0xFF222222), Offset(archRect.left, y), Offset(archRect.right, y), 0.5f)
                    }
                }

                // 4. THE CENTER BOTTLE (Foreground Focus)
                val bottleW = archWidth * 0.18f
                val bottleH = archHeight * 0.45f
                val bottleX = centerX - bottleW / 2f
                val bottleY = archRect.bottom - bottleH - 5.dp.toPx()
                
                // Bottle Shadow/Glow behind
                if (glowIntensity.value > 0.5f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(warmLight.copy(alpha = 0.2f * flicker), Color.Transparent),
                            center = Offset(centerX, bottleY + bottleH / 2f),
                            radius = bottleW * 2f
                        ),
                        radius = bottleW * 2f,
                        center = Offset(centerX, bottleY + bottleH / 2f)
                    )
                }

                // Draw Bottle silhouette
                val bottlePath = Path().apply {
                    moveTo(centerX - bottleW * 0.2f, bottleY)
                    lineTo(centerX + bottleW * 0.2f, bottleY)
                    lineTo(centerX + bottleW * 0.2f, bottleY + bottleH * 0.3f)
                    cubicTo(centerX + bottleW * 0.5f, bottleY + bottleH * 0.4f, centerX + bottleW * 0.5f, bottleY + bottleH, centerX, bottleY + bottleH)
                    cubicTo(centerX - bottleW * 0.5f, bottleY + bottleH, centerX - bottleW * 0.5f, bottleY + bottleH * 0.4f, centerX - bottleW * 0.2f, bottleY + bottleH * 0.3f)
                    close()
                }
                drawPath(bottlePath, Color(0xFF0A0A0A))
                drawPath(bottlePath, goldColor.copy(alpha = 0.5f), style = Stroke(width = 1f))
                
                // Label on bottle
                drawRect(
                    goldColor.copy(alpha = 0.8f),
                    Offset(centerX - bottleW * 0.25f, bottleY + bottleH * 0.55f),
                    Size(bottleW * 0.5f, bottleH * 0.25f)
                )

                // 5. STONE ARCH TEXTURE
                val strokeW = 3.dp.toPx()
                drawPath(archPath, goldColor, style = Stroke(width = strokeW))
                // Stone segments with texture
                for (i in 0..12) {
                    val angle = 180f + i * 15f
                    val rad = Math.toRadians(angle.toDouble())
                    val x1 = centerX + (archWidth / 2f) * Math.cos(rad).toFloat()
                    val y1 = (archRect.top + archWidth / 2f) + (archWidth / 2f) * Math.sin(rad).toFloat()
                    val x2 = centerX + (archWidth / 2f + 8.dp.toPx()) * Math.cos(rad).toFloat()
                    val y2 = (archRect.top + archWidth / 2f) + (archWidth / 2f + 8.dp.toPx()) * Math.sin(rad).toFloat()
                    drawLine(goldColor.copy(alpha = 0.7f), Offset(x1, y1), Offset(x2, y2), strokeW / 2f)
                }

                // 6. SHIMMERING DUST PARTICLES
                if (dustAlpha.value > 0f) {
                    particles.forEach { pair ->
                        val pos = pair.first
                        val speed = pair.second
                        val move = (System.currentTimeMillis() % 10000) / 10000f * speed
                        val px = archRect.left + archWidth * pos.x
                        val py = archRect.top + archHeight * ((pos.y + move) % 1f)
                        drawCircle(
                            Color.White.copy(alpha = 0.15f * dustAlpha.value * flicker),
                            radius = 1.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                // 7. WOODEN DOORS (Open Outward)
                val doorW = archWidth / 2f
                val doorH = archHeight
                
                // Left Door
                rotate(degrees = -100f * doorRotation.value, pivot = Offset(archRect.left, centerY)) {
                    // Wood planks texture
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(woodColor, woodColorLight)),
                        topLeft = Offset(archRect.left, archRect.top),
                        size = Size(doorW, doorH)
                    )
                    for (i in 1..4) {
                        drawLine(Color.Black.copy(alpha = 0.3f), Offset(archRect.left + i * (doorW / 5), archRect.top), Offset(archRect.left + i * (doorW / 5), archRect.bottom), 1f)
                    }
                    drawRect(goldColor.copy(alpha = 0.4f), Offset(archRect.left, archRect.top), Size(doorW, doorH), style = Stroke(width = 1f))
                    // Door handle
                    drawCircle(goldColor, 3f, Offset(archRect.left + doorW * 0.85f, centerY))
                }
                
                // Right Door
                rotate(degrees = 100f * doorRotation.value, pivot = Offset(archRect.right, centerY)) {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(woodColorLight, woodColor)),
                        topLeft = Offset(centerX, archRect.top),
                        size = Size(doorW, doorH)
                    )
                    for (i in 1..4) {
                        drawLine(Color.Black.copy(alpha = 0.3f), Offset(centerX + i * (doorW / 5), archRect.top), Offset(centerX + i * (doorW / 5), archRect.bottom), 1f)
                    }
                    drawRect(goldColor.copy(alpha = 0.4f), Offset(centerX, archRect.top), Size(doorW, doorH), style = Stroke(width = 1f))
                    // Door handle
                    drawCircle(goldColor, 3f, Offset(centerX + doorW * 0.15f, centerY))
                }
            }
        }

        // 8. LOGO TEXT (Refined Serif Style)
        if (!isSmall) {
            Spacer(modifier = Modifier.height(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { alpha = textAlpha.value }) {
                Text(
                    text = "VINOTHEQUE",
                    color = goldColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 12.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Decorative high-end branding line
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(60.dp).height(0.5.dp).background(goldColor.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.width(12.dp))
                    // Elegant grape cluster symbol (Simplified)
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val gS = size.width / 4f
                        drawCircle(goldColor, gS, Offset(size.width / 2, gS))
                        drawCircle(goldColor, gS, Offset(size.width / 2 - gS, gS * 2))
                        drawCircle(goldColor, gS, Offset(size.width / 2 + gS, gS * 2))
                        drawCircle(goldColor, gS, Offset(size.width / 2, gS * 3))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.width(60.dp).height(0.5.dp).background(goldColor.copy(alpha = 0.5f)))
                }
            }
        }
    }
}
