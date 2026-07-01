package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.hypot
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var color: Color
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 45,
    colors: List<Color> = listOf(Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFFA855F7))
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 1f,
                vx = (Random.nextFloat() - 0.5f) * 0.0015f,
                vy = (Random.nextFloat() - 0.5f) * 0.0015f,
                alpha = Random.nextFloat() * 0.6f + 0.2f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val connectDistance = width * 0.15f // Distance to draw connection lines

        time.hashCode() // trigger recomposition

        particles.forEach { p ->
            p.x += p.vx
            p.y += p.vy

            if (p.x < 0) p.x = 1f
            if (p.x > 1) p.x = 0f
            if (p.y < 0) p.y = 1f
            if (p.y > 1) p.y = 0f
        }

        // Draw connections
        for (i in particles.indices) {
            for (j in i + 1 until particles.size) {
                val p1 = particles[i]
                val p2 = particles[j]
                val dx = (p1.x - p2.x) * width
                val dy = (p1.y - p2.y) * height
                val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                if (distance < connectDistance) {
                    val opacity = (1f - (distance / connectDistance)) * 0.3f
                    drawLine(
                        color = Color.White.copy(alpha = opacity),
                        start = Offset(p1.x * width, p1.y * height),
                        end = Offset(p2.x * width, p2.y * height),
                        strokeWidth = 1.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw particles with outer glow
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.alpha * 0.3f),
                radius = p.radius * 2.5f,
                center = Offset(p.x * width, p.y * height)
            )
            drawCircle(
                color = Color.White.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(p.x * width, p.y * height)
            )
        }
    }
}
