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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oracle_idle")

    val magicPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isOracleTalking) 2000 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "magic_phase"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        // OPTIMIZED BACKGROUND AURA (No blur, using radial gradient instead for performance)
        val auraScale = if (isOracleTalking) 1.2f else 0.9f
        Box(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .aspectRatio(1f)
                .scale(scale * auraScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // 1. Single Canvas for Rings, Polygons, Cards, and Hands (Vastly improves performance)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val minRadius = size.minDimension / 2f

            // A. Rings
            val ringRadius = minRadius * 0.9f * scale
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            val ring2Radius = minRadius * 1.05f * scale
            drawCircle(
                color = secondaryColor.copy(alpha = 0.2f),
                radius = ring2Radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )

            // B. Simple Sacred Geometry (Reduced complexity)
            val pRadius = minRadius * 0.7f * scale
            val pRotation = magicPhase * 1.5f
            val path = Path()
            val vertices = 6 // Hexagon instead of complex tesseract
            for (v in 0 until vertices) {
                val angle = (v * (360f / vertices)) * (Math.PI / 180f) + pRotation
                val px = cx + (pRadius * cos(angle)).toFloat()
                val py = cy + (pRadius * sin(angle) * 0.7f).toFloat() // 3D tilt
                if (v == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(
                path = path,
                color = tertiaryColor.copy(alpha = 0.3f),
                style = Stroke(width = 1.dp.toPx())
            )

            // C. Floating Tarot Cards
            val cardCount = 4 // Reduced from 5
            val orbitRadius = minRadius * 0.85f * scale
            for (i in 0 until cardCount) {
                val cardBaseAngle = (i * (360f / cardCount)) * (Math.PI / 180)
                val zDepth = sin(cardBaseAngle + magicPhase).toFloat()
                val currentRadius = orbitRadius * (1f + 0.1f * zDepth)
                val finalAngle = cardBaseAngle + (magicPhase * 0.8)
                
                val cardX = cx + currentRadius * cos(finalAngle).toFloat()
                val cardY = cy + currentRadius * sin(finalAngle).toFloat() * 0.5f
                
                val scaleFactor = 0.7f + (0.3f * zDepth)
                val cardSpin = sin(magicPhase * 2f + i).toFloat()
                
                val cWidth = 28.dp.toPx() * scaleFactor * Math.abs(cardSpin).coerceAtLeast(0.1f)
                val cHeight = 42.dp.toPx() * scaleFactor
                val cRot = (finalAngle * (180 / Math.PI)).toFloat() + 90f + (sin(magicPhase + i) * 15f)

                withTransform({
                    translate(cardX, cardY)
                    rotate(cRot)
                }) {
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.3f),
                        topLeft = Offset(-cWidth/2, -cHeight/2),
                        size = Size(cWidth, cHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(-cWidth/2 + 2f, -cHeight/2 + 2f),
                        size = Size(cWidth - 4f, cHeight - 4f),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            
            // D. Mystic Hands (Simplified)
            val handY = size.height * 0.72f
            val handOffsetMultiplier = if (isOracleTalking) 1.5f else 0.8f
            
            val leftHandY = handY + sin(magicPhase * 1.5).toFloat() * 20.dp.toPx() * handOffsetMultiplier
            val rightHandY = handY + cos(magicPhase * 1.5).toFloat() * 20.dp.toPx() * handOffsetMultiplier
            
            val leftHandX = size.width * 0.15f + cos(magicPhase * 0.8).toFloat() * 10.dp.toPx() * handOffsetMultiplier
            val rightHandX = size.width * 0.85f + sin(magicPhase * 0.8).toFloat() * 10.dp.toPx() * handOffsetMultiplier
            
            // Draw Hands
            val handR = 14.dp.toPx() * scale * if(isOracleTalking) 1.2f else 1.0f
            
            // Left Hand
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(primaryColor.copy(alpha = 0.8f), Color.Transparent), center = Offset(leftHandX, leftHandY), radius = handR * 2),
                radius = handR * 2, center = Offset(leftHandX, leftHandY)
            )
            drawCircle(color = Color.White.copy(alpha=0.6f), radius = handR*0.3f, center = Offset(leftHandX, leftHandY))
            
            // Right Hand
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(secondaryColor.copy(alpha = 0.8f), Color.Transparent), center = Offset(rightHandX, rightHandY), radius = handR * 2),
                radius = handR * 2, center = Offset(rightHandX, rightHandY)
            )
            drawCircle(color = Color.White.copy(alpha=0.6f), radius = handR*0.3f, center = Offset(rightHandX, rightHandY))
            
            // Energy streams to center
            val streamPathLeft = Path().apply { moveTo(leftHandX, leftHandY); quadraticTo(cx - 30.dp.toPx(), cy + 60.dp.toPx(), cx, cy + 80.dp.toPx()) }
            val streamPathRight = Path().apply { moveTo(rightHandX, rightHandY); quadraticTo(cx + 30.dp.toPx(), cy + 60.dp.toPx(), cx, cy + 80.dp.toPx()) }
            
            drawPath(path = streamPathLeft, color = primaryColor.copy(alpha=0.4f), style = Stroke(width = 2.dp.toPx()))
            drawPath(path = streamPathRight, color = secondaryColor.copy(alpha=0.4f), style = Stroke(width = 2.dp.toPx()))
        }

        // 2. Main Character Portrait
        Box(
            modifier = Modifier
                .fillMaxHeight(0.65f) // Slightly smaller for better fit
                .aspectRatio(1f)
                .scale(scale)
        ) {
            val levitationOffset = sin(magicPhase * 2f).toFloat() * 12f
            Image(
                painter = painterResource(id = R.drawable.img_oracle_character), // Assuming an image exists
                contentDescription = "Cyber Oracle",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = levitationOffset.dp)
                    .clip(CircleShape)
            )
        }
    }
}
