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
            
            // D. Realistic Articulated Ethereal Hands
            val handY = size.height * 0.73f
            val handOffsetMultiplier = if (isOracleTalking) 1.6f else 0.9f
            
            val leftHandY = handY + sin(magicPhase * 2.0).toFloat() * 14.dp.toPx() * handOffsetMultiplier
            val rightHandY = handY + cos(magicPhase * 2.0).toFloat() * 14.dp.toPx() * handOffsetMultiplier
            
            val leftHandX = size.width * 0.16f + cos(magicPhase * 1.0).toFloat() * 8.dp.toPx() * handOffsetMultiplier
            val rightHandX = size.width * 0.84f + sin(magicPhase * 1.0).toFloat() * 8.dp.toPx() * handOffsetMultiplier
            
            val handR = 12.dp.toPx() * scale
            
            // Left Hand (Channeled Energy & 3D core & Floating Cyber Fingers)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(leftHandX, leftHandY),
                    radius = handR * 2.5f
                ),
                radius = handR * 2.5f, center = Offset(leftHandX, leftHandY)
            )
            drawCircle(color = Color.White, radius = handR * 0.35f, center = Offset(leftHandX, leftHandY))
            drawCircle(color = primaryColor, radius = handR * 0.5f, center = Offset(leftHandX, leftHandY), style = Stroke(width = 1.5.dp.toPx()))
            
            // Left Hand Fingers (fan layout)
            val fingerCount = 4
            for (f in 0 until fingerCount) {
                val fAngle = Math.PI + (f - 1.5) * 0.35
                val wiggle = sin(magicPhase * 12f + f).toFloat() * 3.5.dp.toPx() * (if (isOracleTalking) 1.8f else 1.0f)
                
                val j1X = leftHandX + (handR * 1.3f * cos(fAngle)).toFloat()
                val j1Y = leftHandY + (handR * 1.3f * sin(fAngle)).toFloat() + wiggle
                val j2X = leftHandX + (handR * 2.2f * cos(fAngle)).toFloat()
                val j2Y = leftHandY + (handR * 2.2f * sin(fAngle)).toFloat() + wiggle * 1.5f
                
                drawLine(color = primaryColor, start = Offset(leftHandX, leftHandY), end = Offset(j1X, j1Y), strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(j1X, j1Y), end = Offset(j2X, j2Y), strokeWidth = 1.5.dp.toPx())
                
                drawCircle(color = secondaryColor, radius = 2.dp.toPx(), center = Offset(j1X, j1Y))
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(j2X, j2Y))
            }
            
            // Right Hand (Channeled Energy & 3D core & Floating Cyber Fingers)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.8f), secondaryColor.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(rightHandX, rightHandY),
                    radius = handR * 2.5f
                ),
                radius = handR * 2.5f, center = Offset(rightHandX, rightHandY)
            )
            drawCircle(color = Color.White, radius = handR * 0.35f, center = Offset(rightHandX, rightHandY))
            drawCircle(color = secondaryColor, radius = handR * 0.5f, center = Offset(rightHandX, rightHandY), style = Stroke(width = 1.5.dp.toPx()))
            
            // Right Hand Fingers (fan layout)
            for (f in 0 until fingerCount) {
                val fAngle = 0.0 - (f - 1.5) * 0.35
                val wiggle = cos(magicPhase * 12f + f).toFloat() * 3.5.dp.toPx() * (if (isOracleTalking) 1.8f else 1.0f)
                
                val j1X = rightHandX + (handR * 1.3f * cos(fAngle)).toFloat()
                val j1Y = rightHandY + (handR * 1.3f * sin(fAngle)).toFloat() + wiggle
                val j2X = rightHandX + (handR * 2.2f * cos(fAngle)).toFloat()
                val j2Y = rightHandY + (handR * 2.2f * sin(fAngle)).toFloat() + wiggle * 1.5f
                
                drawLine(color = secondaryColor, start = Offset(rightHandX, rightHandY), end = Offset(j1X, j1Y), strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(j1X, j1Y), end = Offset(j2X, j2Y), strokeWidth = 1.5.dp.toPx())
                
                drawCircle(color = primaryColor, radius = 2.dp.toPx(), center = Offset(j1X, j1Y))
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(j2X, j2Y))
            }
            
            // Energy streams to center
            val streamPathLeft = Path().apply { moveTo(leftHandX, leftHandY); quadraticTo(cx - 30.dp.toPx(), cy + 60.dp.toPx(), cx, cy + 80.dp.toPx()) }
            val streamPathRight = Path().apply { moveTo(rightHandX, rightHandY); quadraticTo(cx + 30.dp.toPx(), cy + 60.dp.toPx(), cx, cy + 80.dp.toPx()) }
            
            drawPath(path = streamPathLeft, color = primaryColor.copy(alpha=0.4f), style = Stroke(width = 2.dp.toPx()))
            drawPath(path = streamPathRight, color = secondaryColor.copy(alpha=0.4f), style = Stroke(width = 2.dp.toPx()))
        }

        // 2. Main Character Portrait with Real Speaking Mouth Overlay
        val levitationOffset = sin(magicPhase * 2f).toFloat() * 10f
        Box(
            modifier = Modifier
                .fillMaxHeight(0.65f)
                .aspectRatio(1f)
                .offset(y = levitationOffset.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_oracle_character),
                contentDescription = "Cyber Oracle",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            
            // Lip-Sync Mouth overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.54f // Placed exactly on the character's facial mouth area
                
                if (isOracleTalking) {
                    // Mouth opens and closes rapidly
                    val mouthOpenPct = (sin(magicPhase * 24f) + 1f) / 2f
                    val mWidth = 12.dp.toPx()
                    val mHeight = (3.dp.toPx() + 9.dp.toPx() * mouthOpenPct)
                    
                    // Glow effect:
                    drawCircle(
                        color = Color(0xFFFF4081).copy(alpha = 0.3f),
                        radius = mWidth * 0.8f,
                        center = Offset(cx, cy)
                    )
                    
                    // Dark inner cavity
                    drawRoundRect(
                        color = Color(0xFF6A1B9A),
                        topLeft = Offset(cx - mWidth / 2f, cy - mHeight / 2f),
                        size = Size(mWidth, mHeight),
                        cornerRadius = CornerRadius(mWidth / 2f, mHeight / 2f)
                    )
                    
                    // Cute glowing pink lips
                    drawRoundRect(
                        color = Color(0xFFFF4081),
                        topLeft = Offset(cx - mWidth / 2f, cy - mHeight / 2f),
                        size = Size(mWidth, mHeight),
                        cornerRadius = CornerRadius(mWidth / 2f, mHeight / 2f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    
                    // Inner gloss (tongue/teeth representation)
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(cx - mWidth / 4f, cy - mHeight / 2f + 1f),
                        size = Size(mWidth / 2f, 1.5.dp.toPx()),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                } else {
                    // Smile state
                    val smileWidth = 8.dp.toPx()
                    val smileHeight = 2.5.dp.toPx()
                    val path = Path().apply {
                        moveTo(cx - smileWidth / 2f, cy)
                        quadraticTo(cx, cy + smileHeight, cx + smileWidth / 2f, cy)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFF4081),
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
