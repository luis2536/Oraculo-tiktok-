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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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

    // Breathing effect for the whole oracle (subtle scale pulse)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "breathing"
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
    
    // Subtle head tilt when talking
    val headTilt by if (isOracleTalking) {
        infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ), label = "headTilt"
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

        // 3. Sacred Geometry / 4D Tesseract Projections (Rotating deeply in the background)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = (size.minDimension / 2) * 0.75f
            
            // Draw complex rotating geometric polygons (simulating 4D projection)
            val polyCount = 3
            for (p in 0 until polyCount) {
                val pScale = 1f - (p * 0.15f) + (sin(magicPhase * 0.5f) * 0.05f)
                val pRadius = baseRadius * pScale
                val pRotation = (magicPhase * (1.5f + p * 0.5f)) * (if (p % 2 == 0) 1 else -1)
                
                val path = Path()
                val vertices = 8
                for (v in 0 until vertices) {
                    val angle = (v * (360f / vertices)) * (Math.PI / 180f) + pRotation
                    // Add slight Z-depth distortion
                    val zDistortion = 1f + 0.1f * sin(angle + magicPhase * 2f).toFloat()
                    val px = center.x + (pRadius * zDistortion * cos(angle)).toFloat()
                    val py = center.y + (pRadius * zDistortion * sin(angle) * 0.6f).toFloat() // Squish Y for 3D perspective
                    if (v == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                
                drawPath(
                    path = path,
                    color = tertiaryColor.copy(alpha = 0.15f + (0.15f * p)),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), magicPhase * 5f)
                    )
                )
                
                // Connect opposing vertices for hypercube/tesseract aesthetic
                for (v in 0 until vertices / 2) {
                    val a1 = (v * (360f / vertices)) * (Math.PI / 180f) + pRotation
                    val a2 = ((v + vertices / 2) * (360f / vertices)) * (Math.PI / 180f) + pRotation
                    
                    val px1 = center.x + (pRadius * cos(a1)).toFloat()
                    val py1 = center.y + (pRadius * sin(a1) * 0.6f).toFloat()
                    val px2 = center.x + (pRadius * cos(a2)).toFloat()
                    val py2 = center.y + (pRadius * sin(a2) * 0.6f).toFloat()
                    
                    drawLine(
                        color = secondaryColor.copy(alpha = 0.1f),
                        start = Offset(px1, py1),
                        end = Offset(px2, py2),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }
        }

        // 4. Floating Tarot Cards in Orbit around her
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val orbitRadius = (size.minDimension / 2) * 0.9f
            
            // Render 5 Oracle Cards that hover and rotate mystically in 3D-like orbits
            val cardCount = 5
            for (i in 0 until cardCount) {
                // Symmetrically space cards, with a dynamic offset over time
                val cardBaseAngle = (i * (360f / cardCount)) * (Math.PI / 180)
                val dynamicOffset = if (isOracleTalking) {
                    sin(magicPhase.toDouble() + i * 1.5).toFloat() * 0.2f
                } else {
                    sin(magicPhase.toDouble() / 2 + i * 1.2).toFloat() * 0.08f
                }
                
                // Simulate 3D Depth by scaling and varying orbit radius
                val zDepth = sin(cardBaseAngle + magicPhase).toFloat()
                val currentRadius = orbitRadius * (1f + 0.1f * zDepth)
                val finalAngle = cardBaseAngle + dynamicOffset + (magicPhase * 0.5)
                
                val cx = center.x + currentRadius * cos(finalAngle).toFloat()
                val cy = center.y + currentRadius * sin(finalAngle).toFloat() * 0.5f // Squished Y to simulate 3D tilt
                
                val scaleFactor = 0.8f + (0.4f * zDepth)
                
                // 3D Card Spin: Scale X based on sine of angle to simulate flipping
                val cardSpin = sin(magicPhase * 2f + i).toFloat()
                
                // Animate size & rotation
                val cardWidth = 32.dp.toPx() * scaleFactor * Math.abs(cardSpin).coerceAtLeast(0.1f)
                val cardHeight = 48.dp.toPx() * scaleFactor
                val cardRotation = (finalAngle * (180 / Math.PI)).toFloat() + 90f + (sin(magicPhase + i) * 15f)

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
                        radius = 6.dp.toPx() * Math.abs(cardSpin).coerceAtLeast(0.1f),
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
                        start = Offset((-6.dp.toPx()) * Math.abs(cardSpin), 0f),
                        end = Offset((6.dp.toPx()) * Math.abs(cardSpin), 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // 5. Main Character Portrait
        Box(
            modifier = Modifier
                .fillMaxHeight(0.72f)
                .aspectRatio(1f)
                .offset(y = offsetY.dp)
                .rotate(headTilt)
                .scale(scale * breathingScale)
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
                        .fillMaxWidth(0.32f)
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = (-28).dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw glowing background sound orbs (simulated 3D voice field)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondaryColor.copy(alpha = 0.45f * speechAmplitude), Color.Transparent),
                            center = Offset(w / 2, h / 2),
                            radius = (w / 2) * (0.6f + 0.4f * speechAmplitude)
                        ),
                        radius = (w / 2) * (0.6f + 0.4f * speechAmplitude),
                        center = Offset(w / 2, h / 2)
                    )

                    // 2. Draw rotating cyber-rings around her mouth that contract and expand dynamically
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.35f * speechAmplitude),
                        radius = (14.dp.toPx()) * (0.8f + 0.4f * speechAmplitude),
                        center = Offset(w / 2, h / 2),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )

                    // 3. Draw the active mouth opening (vocal vortex) - a stylized ellipse that scales with speech
                    val mouthHeight = (8.dp.toPx()) * speechAmplitude
                    val mouthWidth = (16.dp.toPx()) * (1f + 0.2f * speechAmplitude)
                    drawOval(
                        color = Color.White.copy(alpha = 0.95f),
                        topLeft = Offset(w / 2 - mouthWidth / 2, h / 2 - mouthHeight / 2),
                        size = Size(mouthWidth, mouthHeight),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawOval(
                        color = secondaryColor.copy(alpha = 0.5f),
                        topLeft = Offset(w / 2 - mouthWidth / 2 - 2.dp.toPx(), h / 2 - mouthHeight / 2 - 2.dp.toPx()),
                        size = Size(mouthWidth + 4.dp.toPx(), mouthHeight + 4.dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // 4. Draw overlapping horizontal vocal spectrum waves (Multi-band depth)
                    val pathsCount = 3
                    val colors = listOf(Color.White, secondaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.5f))
                    val strokeWidths = listOf(2.2.dp.toPx(), 1.5.dp.toPx(), 1.dp.toPx())
                    
                    for (pIdx in 0 until pathsCount) {
                        val path = Path()
                        path.moveTo(0f, h / 2)
                        val points = 35
                        for (i in 0..points) {
                            val x = (i.toFloat() / points) * w
                            // Different frequencies and phase offsets per path to create depth
                            val phaseOffset = pIdx * (Math.PI / 3).toFloat()
                            val speedMultiplier = 3.5f + pIdx * 1.5f
                            val wave = sin((i * 0.45f) + (magicPhase * speedMultiplier) + phaseOffset) * 
                                         cos((i * 0.25f) - (magicPhase * 2f) + phaseOffset)
                            
                            // Scale height by speechAmplitude, and taper off near edges (fade window)
                            val edgeFade = sin((i.toFloat() / points) * Math.PI).toFloat()
                            val y = (h / 2) + wave * (h * 0.55f) * speechAmplitude * edgeFade
                            path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = colors[pIdx],
                            style = Stroke(width = strokeWidths[pIdx])
                        )
                    }

                    // 5. Draw active voice sparks (floating particle emanations flowing outward)
                    for (i in 0..5) {
                        val particleAngle = (magicPhase * 3f + i * (2 * Math.PI / 6)).toFloat()
                        val distance = (12.dp.toPx() + 18.dp.toPx() * speechAmplitude) * (0.5f + 0.5f * sin(magicPhase * 2f + i))
                        val px = w / 2 + distance * cos(particleAngle)
                        val py = h / 2 + distance * sin(particleAngle)
                        drawCircle(
                            color = tertiaryColor.copy(alpha = 0.75f * speechAmplitude),
                            radius = 2.dp.toPx() * (1f + 0.4f * sin(magicPhase + i)),
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        // 6. Mystic Cybernetic Channeling Hands (Floating Left and Right in 3D Arcs)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            
            // Hand motion offset (simulating complex breathing 8-figure movement)
            val baseLeftY = h * 0.72f
            val baseRightY = h * 0.72f
            val animMultiplier = if (isOracleTalking) 1.8f else 0.8f
            
            val leftHandY = baseLeftY + sin(magicPhase.toDouble() * 1.5).toFloat() * 25.dp.toPx() * animMultiplier
            val rightHandY = baseRightY + cos(magicPhase.toDouble() * 1.5).toFloat() * 25.dp.toPx() * animMultiplier
            
            val leftHandX = w * 0.12f + cos(magicPhase.toDouble() * 0.8).toFloat() * 15.dp.toPx() * animMultiplier
            val rightHandX = w * 0.88f + sin(magicPhase.toDouble() * 0.8).toFloat() * 15.dp.toPx() * animMultiplier
            
            // Draw Glowing Left Cybernetic Channeling Hand
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
    val scaleFactor = if (isTalking) 1.5f else 1.0f
    val r = 18.dp.toPx() * scaleFactor
    
    // Draw magic energy core
    drawCircle(
        color = Color.White,
        radius = 5.dp.toPx() * scaleFactor,
        center = center
    )
    
    // Core radial glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = if (isTalking) 0.9f else 0.5f), Color.Transparent),
            center = center,
            radius = 20.dp.toPx() * scaleFactor
        ),
        radius = 20.dp.toPx() * scaleFactor,
        center = center
    )

    // Inner geometric hand bracket path (Futuristic cyber-mystic palm)
    val path = Path()
    val direction = if (isLeft) 1f else -1f
    
    // Cyber-mystic fingers/spokes
    val fingerCount = 4
    for (i in 0 until fingerCount) {
        val fAngle = (if (isLeft) Math.PI else 0.0) - direction * (Math.PI / 4) + direction * (i * Math.PI / 6) + sin(phase * 1.5f) * 0.15f
        val fRadius = r * (1.2f + 0.3f * cos(phase + i))
        
        path.moveTo(center.x, center.y)
        path.quadraticTo(
            center.x + (r * 0.5f * cos(fAngle)).toFloat(), 
            center.y + (r * 0.5f * sin(fAngle)).toFloat(),
            center.x + (fRadius * cos(fAngle)).toFloat(), 
            center.y + (fRadius * sin(fAngle)).toFloat()
        )
    }
    
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    )

    // Outer rotating energy binding ring
    drawCircle(
        color = glowColor.copy(alpha = 0.6f),
        radius = r * 1.2f,
        center = center,
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), phase * 15f * direction)
        )
    )
    
    // Energy stream connecting hand to the central body (Oracle)
    val bodyCenter = Offset(size.width / 2, size.height * 0.72f) // Pointing near her core
    val streamPath = Path()
    streamPath.moveTo(center.x, center.y)
    streamPath.quadraticTo(
        center.x + (bodyCenter.x - center.x) / 2f, center.y - 40.dp.toPx() * scaleFactor,
        bodyCenter.x, bodyCenter.y
    )
    
    drawPath(
        path = streamPath,
        color = glowColor.copy(alpha = if (isTalking) 0.4f else 0.15f),
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), -phase * 30f)
        )
    )

    // Floating micro-sigils around channeling hands
    val particleCount = 6
    for (i in 0 until particleCount) {
        val angle = (phase * (if (isLeft) -1f else 1f) + i * (2 * Math.PI / particleCount)).toFloat()
        val pRadius = r * (1.4f + 0.2f * sin(phase * 3f + i))
        val px = center.x + pRadius * cos(angle)
        val py = center.y + pRadius * sin(angle)
        drawCircle(
            color = glowColor.copy(alpha = 0.7f),
            radius = 2.dp.toPx() * (if (isTalking) 1.5f else 1.0f),
            center = Offset(px, py)
        )
    }
}
