package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicRings(
    isOracleTalking: Boolean,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram_rotation")
    
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation1"
    )
    
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation2"
    )

    val scanningY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "scanningY"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = (size.minDimension / 2) * 0.88f
            
            // Draw a subtle background radar grid (crosshairs & concentric light lines)
            drawCircle(
                color = primaryColor.copy(alpha = 0.05f),
                radius = baseRadius * 0.5f,
                center = center,
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.08f),
                radius = baseRadius * 0.75f,
                center = center,
                style = Stroke(width = 1f)
            )
            
            // Technical crosshairs
            val lineLen = 15.dp.toPx()
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x - baseRadius - lineLen, center.y),
                end = Offset(center.x - baseRadius + lineLen, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x + baseRadius - lineLen, center.y),
                end = Offset(center.x + baseRadius + lineLen, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - baseRadius - lineLen),
                end = Offset(center.x, center.y - baseRadius + lineLen),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y + baseRadius - lineLen),
                end = Offset(center.x, center.y + baseRadius + lineLen),
                strokeWidth = 1.dp.toPx()
            )

            // Outer Rotating Ring: Dash patterned circle with active validation nodes
            withTransform({
                rotate(rotation1, center)
            }) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.4f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f)
                    )
                )

                // 4 Validation nodes on outer circle (Double Validation aesthetic)
                for (i in 0 until 4) {
                    val angle = (i * 90) * (Math.PI / 180)
                    val nodeX = center.x + baseRadius * cos(angle).toFloat()
                    val nodeY = center.y + baseRadius * sin(angle).toFloat()
                    
                    drawCircle(
                        color = secondaryColor,
                        radius = 5.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // Inner Rotating Ring: Octagon structure counter-rotating
            withTransform({
                rotate(rotation2, center)
            }) {
                val innerRadius = baseRadius * 0.85f
                
                drawCircle(
                    color = tertiaryColor.copy(alpha = 0.35f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(
                        width = 1.5f.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                    )
                )

                // High-tech Hexagon/Octagon Polygon Line drawing
                val points = 8
                val path = Path()
                for (i in 0 until points) {
                    val angle = (i * (360f / points)) * (Math.PI / 180)
                    val px = center.x + innerRadius * cos(angle).toFloat()
                    val py = center.y + innerRadius * sin(angle).toFloat()
                    if (i == 0) {
                        path.moveTo(px, py)
                    } else {
                        path.lineTo(px, py)
                    }
                }
                path.close()
                
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.15f),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Dynamic nodes on the octagon corners
                for (i in 0 until points step 2) {
                    val angle = (i * (360f / points)) * (Math.PI / 180)
                    val px = center.x + innerRadius * cos(angle).toFloat()
                    val py = center.y + innerRadius * sin(angle).toFloat()
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // Glowing Laser Scanning line (simulating AI core scanning)
            val scanYPosition = center.y - baseRadius + (baseRadius * 2 * scanningY)
            val scanWidth = baseRadius * 0.9f
            drawLine(
                color = secondaryColor.copy(alpha = 0.6f),
                start = Offset(center.x - scanWidth, scanYPosition),
                end = Offset(center.x + scanWidth, scanYPosition),
                strokeWidth = 2.dp.toPx()
            )
            // Soft laser glow
            drawLine(
                color = secondaryColor.copy(alpha = 0.15f),
                start = Offset(center.x - scanWidth, scanYPosition),
                end = Offset(center.x + scanWidth, scanYPosition),
                strokeWidth = 8.dp.toPx()
            )
        }

        // Overlay Technical text parameters in the corners (glass-tech style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Top Left Info
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "SYS_DEC: ONLINE",
                    color = primaryColor.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "P2P_SYNC: CONNECTED",
                    color = secondaryColor.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top Right Info
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "CORE_AI: GEMINI_PRO",
                    color = tertiaryColor.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isOracleTalking) "VOX_GEN: ACTIVE" else "VOX_GEN: IDLE",
                    color = (if (isOracleTalking) secondaryColor else Color.Gray).copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
