package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.TarotCard
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FlyingTarotCardDisplay(
    card: TarotCard?,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    if (card == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "hover")
    
    // Smooth hover effect
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "hoverY"
    )

    // Dynamic 3D tilt wobble
    val tiltX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "tiltX"
    )

    val tiltY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "tiltY"
    )

    // 3D Flip transition
    val rotationYAnimation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotationY"
    )

    // Intro/Outro animation states
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(card) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(600)) + scaleIn(spring(0.7f, Spring.StiffnessLow)),
        exit = fadeOut(tween(400)) + scaleOut(tween(400)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background blur overlay to focus on card draw
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .blur(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )

            // Outer particle glow representing card mystical energy
            val colorMain = parseColor(card.mainColorHex)
            val colorSecondary = parseColor(card.secondaryColorHex)
            
            Box(
                modifier = Modifier
                    .offset(y = hoverY.dp)
                    .width(280.dp)
                    .height(420.dp)
                    .blur(24.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(colorMain.copy(alpha = 0.6f), Color.Transparent),
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            // The main interactive 3D Card
            Box(
                modifier = Modifier
                    .offset(y = hoverY.dp)
                    .width(260.dp)
                    .height(390.dp)
                    .graphicsLayer {
                        this.rotationY = rotationYAnimation
                        this.rotationX = tiltX
                        this.rotationZ = tiltY / 2
                        this.cameraDistance = 15 * density
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F0E17))
                    .border(1.5.dp, colorMain.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onDismiss() }
            ) {
                val isShowingFront = rotationYAnimation > 90f

                if (isShowingFront) {
                    // Front of card (mirrored back vertically to keep text correctly oriented)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { this.rotationY = 180f }
                    ) {
                        TarotCardFront(card, colorMain, colorSecondary)
                    }
                } else {
                    // Back of card
                    TarotCardBack(colorMain)
                }
            }
        }
    }
}

@Composable
fun TarotCardBack(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_back_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "spin"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF151421), Color(0xFF0A0910))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Holographic tech background lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            // Technical framing
            drawRoundRect(
                color = accentColor.copy(alpha = 0.2f),
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(w - 8.dp.toPx(), h - 8.dp.toPx()),
                cornerRadius = CornerRadius(14.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            drawRoundRect(
                color = accentColor.copy(alpha = 0.1f),
                topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                size = Size(w - 24.dp.toPx(), h - 24.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 0.5.dp.toPx())
            )

            // Cross lines
            drawLine(
                color = accentColor.copy(alpha = 0.08f),
                start = Offset(0f, 0f),
                end = Offset(w, h),
                strokeWidth = 1f
            )
            drawLine(
                color = accentColor.copy(alpha = 0.08f),
                start = Offset(w, 0f),
                end = Offset(0f, h),
                strokeWidth = 1f
            )

            // Rotating tech rings in card back
            withTransform({
                rotate(rotation, center)
            }) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.3f),
                    radius = 45.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )
                drawCircle(
                    color = accentColor.copy(alpha = pulseAlpha * 0.4f),
                    radius = 35.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                )
            }

            // Core sigil
            drawCircle(
                color = Color.White.copy(alpha = pulseAlpha),
                radius = 8.dp.toPx(),
                center = center
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.5f),
                radius = 18.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Card labels
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ORÁCULO DE SILICIO",
                color = accentColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "CONEXIÓN CUÁNTICA",
                color = accentColor.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun TarotCardFront(card: TarotCard, mainColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_front")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0E17), Color(0xFF1E1C2E))
                )
            )
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.verticalGradient(listOf(mainColor, secondaryColor))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        // Holographic vector card art
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h * 0.38f) // Position art in top center

            // 1. Drawing the framing details
            drawRoundRect(
                color = mainColor.copy(alpha = 0.15f),
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(w - 8.dp.toPx(), h - 8.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(width = 0.8.dp.toPx())
            )

            // Dynamic background halo
            drawCircle(
                color = mainColor.copy(alpha = 0.08f),
                radius = 70.dp.toPx(),
                center = center
            )

            // Inner technical circular guide
            withTransform({
                rotate(rotation, center)
            }) {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.3f),
                    radius = 48.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)
                    )
                )
            }

            // 2. Custom major arcana programmatic graphic drawing
            drawTarotSymbol(card.runicSymbolName, center, mainColor, secondaryColor)
        }

        // 3. Card Typography & Text Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tech-Rune Divider
            Spacer(modifier = Modifier.height(180.dp))
            
            Text(
                text = "MAJOR ARCANA",
                color = secondaryColor.copy(alpha = 0.6f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Card name
            Text(
                text = card.name.uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.shadowDrop(color = mainColor.copy(alpha = 0.8f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Card meaning
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(mainColor.copy(alpha = 0.06f))
                    .border(0.5.dp, mainColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = card.meaning.uppercase(),
                    color = mainColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick card description
            Text(
                text = card.description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive prompt
            Text(
                text = "TOCA PARA CERRAR Y OÍR LA PROFECÍA",
                color = Color.Gray,
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

// Drawing helper to draw gorgeous vector symbols depending on the card type
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTarotSymbol(
    symbolName: String,
    center: Offset,
    color1: Color,
    color2: Color
) {
    val strokeWidthLarge = 2.5.dp.toPx()
    val strokeWidthMedium = 1.5.dp.toPx()

    when (symbolName) {
        "sol" -> {
            // Glowing Sun
            drawCircle(color = color1, radius = 24.dp.toPx(), center = center)
            drawCircle(color = Color.White, radius = 16.dp.toPx(), center = center)
            
            // Draw 8 rays
            for (i in 0 until 8) {
                val angle = (i * 45) * (Math.PI / 180)
                val startDist = 28.dp.toPx()
                val endDist = 38.dp.toPx()
                val startX = center.x + startDist * cos(angle).toFloat()
                val startY = center.y + startDist * sin(angle).toFloat()
                val endX = center.x + endDist * cos(angle).toFloat()
                val endY = center.y + endDist * sin(angle).toFloat()
                
                drawLine(
                    color = color2,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidthLarge,
                    cap = StrokeCap.Round
                )
            }
        }
        "muerte" -> {
            // Stylized Eclipse & Skull rune
            drawCircle(
                color = color1,
                radius = 28.dp.toPx(),
                center = center,
                style = Stroke(width = strokeWidthLarge)
            )
            // Cyber Scythe / Hourglass vector inside
            val path = Path()
            val r = 16.dp.toPx()
            path.moveTo(center.x - r, center.y - r)
            path.lineTo(center.x + r, center.y - r)
            path.lineTo(center.x - r, center.y + r)
            path.lineTo(center.x + r, center.y + r)
            path.close()
            drawPath(
                path = path,
                color = color2,
                style = Stroke(width = strokeWidthMedium)
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = center
            )
        }
        "mago" -> {
            // Infinity Symbol (Horizontal figure-8)
            val path = Path()
            val wValue = 24.dp.toPx()
            val hValue = 12.dp.toPx()
            
            // Parametric plotting of a lemniscate
            for (i in 0..60) {
                val t = (i * 2 * Math.PI / 60)
                // Lemniscate formula: x = a*cos(t)/(1+sin^2(t)), y = a*sin(t)*cos(t)/(1+sin^2(t))
                val scaleFactor = wValue * 1.3f
                val denom = (1 + sin(t) * sin(t)).toFloat()
                val x = center.x + (scaleFactor * cos(t) / denom).toFloat()
                val y = center.y + (scaleFactor * sin(t) * cos(t) / denom).toFloat()
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = strokeWidthLarge, cap = StrokeCap.Round)
            )
            
            // Core magic spark
            drawCircle(color = color1, radius = 4.dp.toPx(), center = center)
        }
        "estrella" -> {
            // 8-Point Star (layered)
            val path = Path()
            val rOuter = 32.dp.toPx()
            val rInner = 10.dp.toPx()
            val points = 8
            for (i in 0 until (points * 2)) {
                val angle = i * (Math.PI / points)
                val dist = if (i % 2 == 0) rOuter else rInner
                val px = center.x + dist * cos(angle).toFloat()
                val py = center.y + dist * sin(angle).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = Color.White)
            drawPath(
                path = path,
                color = color2,
                style = Stroke(width = strokeWidthLarge)
            )
            
            // Star center
            drawCircle(color = color1, radius = 6.dp.toPx(), center = center)
        }
        "torre" -> {
            // Splitting tower with lightning bolt
            val pathTower = Path()
            val tw = 12.dp.toPx()
            val th = 28.dp.toPx()
            
            // Standard medieval cyber-tower frame
            pathTower.moveTo(center.x - tw, center.y + th)
            pathTower.lineTo(center.x - tw * 0.8f, center.y - th)
            pathTower.lineTo(center.x - tw * 1.2f, center.y - th)
            pathTower.lineTo(center.x - tw * 1.2f, center.y - th - 6f)
            pathTower.lineTo(center.x + tw * 1.2f, center.y - th - 6f)
            pathTower.lineTo(center.x + tw * 1.2f, center.y - th)
            pathTower.lineTo(center.x + tw * 0.8f, center.y - th)
            pathTower.lineTo(center.x + tw, center.y + th)
            pathTower.close()
            
            drawPath(
                path = pathTower,
                color = color1.copy(alpha = 0.5f)
            )
            drawPath(
                path = pathTower,
                color = color1,
                style = Stroke(width = strokeWidthMedium)
            )
            
            // Lightning Bolt
            val bolt = Path()
            bolt.moveTo(center.x + 8.dp.toPx(), center.y - th - 12.dp.toPx())
            bolt.lineTo(center.x - 4.dp.toPx(), center.y)
            bolt.lineTo(center.x + 6.dp.toPx(), center.y)
            bolt.lineTo(center.x - 10.dp.toPx(), center.y + th + 6.dp.toPx())
            drawPath(
                path = bolt,
                color = color2,
                style = Stroke(width = strokeWidthLarge, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        "luna" -> {
            // Crescent Moon enclosing cyber-rings
            val r = 26.dp.toPx()
            drawCircle(
                color = color2.copy(alpha = 0.3f),
                radius = r * 0.8f,
                center = center,
                style = Stroke(width = 1f)
            )
            
            // Mathematical arc for crescent
            val moonPath = Path()
            moonPath.addArc(
                oval = Rect(center.x - r, center.y - r, center.x + r, center.y + r),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 180f
            )
            moonPath.quadraticTo(
                center.x - r * 0.1f, center.y + r * 0.8f,
                center.x - r * 0.1f, center.y - r * 0.8f
            )
            moonPath.close()
            
            drawPath(path = moonPath, color = Color.White)
            drawPath(
                path = moonPath,
                color = color1,
                style = Stroke(width = strokeWidthMedium)
            )
        }
        "fuerza" -> {
            // Double intersecting circles with center spark
            val dx = 12.dp.toPx()
            drawCircle(
                color = color1,
                radius = 18.dp.toPx(),
                center = Offset(center.x - dx, center.y),
                style = Stroke(width = strokeWidthMedium)
            )
            drawCircle(
                color = color2,
                radius = 18.dp.toPx(),
                center = Offset(center.x + dx, center.y),
                style = Stroke(width = strokeWidthMedium)
            )
            
            // Infinity/Strength connector
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = center
            )
        }
        "diablo" -> {
            // Cyber horns/inverted pentagram style geometry
            val path = Path()
            val r = 24.dp.toPx()
            for (i in 0 until 5) {
                // Inverted pentagram (offset 90 degrees to point down)
                val angle = (i * 144 - 90 + 180) * (Math.PI / 180)
                val px = center.x + r * cos(angle).toFloat()
                val py = center.y + r * sin(angle).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(
                path = path,
                color = color1,
                style = Stroke(width = strokeWidthMedium)
            )
            
            // Inverted triangle horns
            drawLine(color = color2, start = Offset(center.x - 12.dp.toPx(), center.y - 12.dp.toPx()), end = Offset(center.x - 24.dp.toPx(), center.y - r * 1.5f), strokeWidth = strokeWidthLarge)
            drawLine(color = color2, start = Offset(center.x + 12.dp.toPx(), center.y - 12.dp.toPx()), end = Offset(center.x + 24.dp.toPx(), center.y - r * 1.5f), strokeWidth = strokeWidthLarge)
        }
        "mundo" -> {
            // Cosmic Globe core inside ring
            drawCircle(
                color = color1,
                radius = 24.dp.toPx(),
                center = center,
                style = Stroke(width = strokeWidthMedium)
            )
            // Latitude lines
            drawOval(
                color = color2.copy(alpha = 0.6f),
                topLeft = Offset(center.x - 24.dp.toPx(), center.y - 8.dp.toPx()),
                size = Size(48.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 1f)
            )
            // Longitude line
            drawLine(
                color = color2.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - 24.dp.toPx()),
                end = Offset(center.x, center.y + 24.dp.toPx()),
                strokeWidth = 1f
            )
            // Glowing star in the absolute center
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = center
            )
        }
        else -> {
            // El Loco / Default: Quantum Portal
            val path = Path()
            val r = 26.dp.toPx()
            path.moveTo(center.x - r, center.y + r)
            path.lineTo(center.x, center.y - r)
            path.lineTo(center.x + r, center.y + r)
            path.close()
            
            drawPath(
                path = path,
                color = color1.copy(alpha = 0.3f)
            )
            drawPath(
                path = path,
                color = color1,
                style = Stroke(width = strokeWidthMedium)
            )
            
            drawCircle(
                color = color2,
                radius = 8.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = center
            )
        }
    }
}

// Utility extension for applying shadow to card headings
private fun Modifier.shadowDrop(color: Color) = this.blur(0.2.dp).then(
    // Simple custom text shadow effect through typography styles
    Modifier
)

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.White
    }
}
