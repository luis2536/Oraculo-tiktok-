package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MysticOracleVisualizer(
    isOracleTalking: Boolean,
    scale: Float,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mystic_oracle_anim")
    
    // Smooth hands & cards wave animation
    val magicPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "magicPhase"
    )

    // Speech amplitude for mouth simulation
    val speechAmplitude by if (isOracleTalking) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(150, easing = EaseInOutBounce),
                repeatMode = RepeatMode.Reverse
            ), label = "speechAmplitude"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Glowing background aura matching speech state
        val backgroundGlowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = if (isOracleTalking) 0.85f else 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (isOracleTalking) 300 else 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "bg_glow"
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .aspectRatio(1f)
                .scale(scale * 1.05f)
                .blur(32.dp)
                .background(primaryColor.copy(alpha = backgroundGlowAlpha), CircleShape)
        )

        // 2. Holographic Ring HUD System
        HolographicRings(
            isOracleTalking = isOracleTalking,
            scale = scale,
            modifier = Modifier
                .fillMaxHeight(1.15f)
                .aspectRatio(1f)
        )

        // 3. Floating Tarot Cards in Orbit around her
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val orbitRadius = (size.minDimension / 2) * 0.82f
            
            // Render 3 Oracle Cards that hover and rotate mystically
            val cardCount = 3
            for (i in 0 until cardCount) {
                // Symmetrically space cards, with a dynamic offset over time
                val cardBaseAngle = (i * (360f / cardCount)) * (Math.PI / 180)
                val dynamicOffset = if (isOracleTalking) {
                    sin(magicPhase.toDouble() + i * 1.5).toFloat() * 0.15f
                } else {
                    sin(magicPhase.toDouble() / 2 + i * 1.2).toFloat() * 0.06f
                }
                
                val finalAngle = cardBaseAngle + dynamicOffset
                val cx = center.x + orbitRadius * cos(finalAngle).toFloat()
                val cy = center.y + orbitRadius * sin(finalAngle).toFloat()
                
                // Animate size & rotation
                val cardWidth = 32.dp.toPx()
                val cardHeight = 48.dp.toPx()
                val cardRotation = (finalAngle * (180 / Math.PI)).toFloat() + 90f + (sin(magicPhase + i) * 10f)

                withTransform({
                    translate(cx, cy)
                    rotate(cardRotation)
                }) {
                    // Draw outer card glow
                    val cardGlowColor = if (isOracleTalking) secondaryColor else primaryColor
                    drawRoundRect(
                        color = cardGlowColor.copy(alpha = if (isOracleTalking) 0.45f else 0.2f),
                        topLeft = Offset(-cardWidth / 2 - 3.dp.toPx(), -cardHeight / 2 - 3.dp.toPx()),
                        size = Size(cardWidth + 6.dp.toPx(), cardHeight + 6.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw card back outline
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(-cardWidth / 2, -cardHeight / 2),
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw inner cosmic runic symbol inside the card
                    drawCircle(
                        color = tertiaryColor.copy(alpha = 0.5f),
                        radius = 6.dp.toPx(),
                        center = Offset(0f, 0f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    
                    // Mystical cross within the card
                    drawLine(
                        color = tertiaryColor.copy(alpha = 0.5f),
                        start = Offset(0f, -8.dp.toPx()),
                        end = Offset(0f, 8.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = tertiaryColor.copy(alpha = 0.5f),
                        start = Offset(-6.dp.toPx(), 0f),
                        end = Offset(6.dp.toPx(), 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // 4. Main Character Portrait
        Box(
            modifier = Modifier
                .fillMaxHeight(0.72f)
                .aspectRatio(1f)
                .offset(y = offsetY.dp)
                .scale(scale)
                .clip(CircleShape)
                .border(2.5.dp, primaryColor.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.img_oracle_character),
                contentDescription = "Oracle",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 5. Real-time Talking Mouth overlay (Simulated glowing tech mouth wave at her face bottom)
            if (isOracleTalking) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.28f)
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = (-32).dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val path = Path()
                    path.moveTo(0f, h / 2)
                    
                    // Draw a glowing sine/cosine speech wave centered
                    val points = 30
                    for (i in 0..points) {
                        val x = (i.toFloat() / points) * w
                        // Harmonic talking wave calculation
                        val wave = sin((i * 0.4f) + (magicPhase * 4f)) * cos((i * 0.2f) - (magicPhase * 2f))
                        val y = (h / 2) + wave * (h * 0.45f) * speechAmplitude
                        path.lineTo(x, y)
                    }
                    
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    
                    // Outer glow behind speech path
                    drawPath(
                        path = path,
                        color = secondaryColor.copy(alpha = 0.6f),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
            }
        }

        // 6. Mystic Cybernetic Channeling Hands (Floating Left and Right)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            
            // Hand motion offset
            val leftHandY = h * 0.72f + sin(magicPhase.toDouble() * 1.5).toFloat() * 20.dp.toPx() * (if (isOracleTalking) 1.5f else 0.5f)
            val rightHandY = h * 0.72f + cos(magicPhase.toDouble() * 1.5).toFloat() * 20.dp.toPx() * (if (isOracleTalking) 1.5f else 0.5f)
            
            val leftHandX = w * 0.12f
            val rightHandX = w * 0.88f
            
            // Draw Glowing Left Cybernetic Channeling Hand (represented as stylized futuristic energy vectors)
            drawHandArc(
                center = Offset(leftHandX, leftHandY),
                glowColor = primaryColor,
                isTalking = isOracleTalking,
                phase = magicPhase,
                isLeft = true
            )

            // Draw Glowing Right Cybernetic Channeling Hand
            drawHandArc(
                center = Offset(rightHandX, rightHandY),
                glowColor = secondaryColor,
                isTalking = isOracleTalking,
                phase = magicPhase,
                isLeft = false
            )
        }
    }
}

// Custom Draw Helper for Stylized Futuristic Channeling Hands
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHandArc(
    center: Offset,
    glowColor: Color,
    isTalking: Boolean,
    phase: Float,
    isLeft: Boolean
) {
    val scaleFactor = if (isTalking) 1.3f else 1.0f
    val r = 16.dp.toPx() * scaleFactor
    
    // Draw magic energy core
    drawCircle(
        color = Color.White,
        radius = 4.dp.toPx() * scaleFactor,
        center = center
    )
    drawCircle(
        color = glowColor.copy(alpha = if (isTalking) 0.8f else 0.4f),
        radius = 10.dp.toPx() * scaleFactor,
        center = center
    )

    // Inner geometric hand bracket path (Futuristic cyber-mystic palm)
    val path = Path()
    val direction = if (isLeft) 1f else -1f
    
    path.moveTo(center.x - direction * r, center.y - r)
    path.quadraticTo(
        center.x + direction * r * 0.2f, center.y - r * 0.1f,
        center.x - direction * r, center.y + r
    )
    
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(width = 2.dp.toPx())
    )

    // Floating micro-sigils around channeling hands
    val particleCount = 4
    for (i in 0 until particleCount) {
        val angle = (phase + i * (2 * Math.PI / particleCount)).toFloat()
        val px = center.x + r * 1.3f * cos(angle)
        val py = center.y + r * 1.3f * sin(angle)
        drawCircle(
            color = glowColor.copy(alpha = 0.5f),
            radius = 2.dp.toPx(),
            center = Offset(px, py)
        )
    }
}
