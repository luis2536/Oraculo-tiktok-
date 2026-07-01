package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
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
    particleCount: Int = 30,
    colors: List<Color> = listOf(Color(0xFFA855F7), Color(0xFF2DD4BF), Color(0xFFE879F9))
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 8f + 2f,
                vx = (Random.nextFloat() - 0.5f) * 0.002f,
                vy = (Random.nextFloat() - 0.5f) * 0.002f,
                alpha = Random.nextFloat() * 0.5f + 0.1f,
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

        // Just reading time to trigger recomposition
        time.hashCode()

        particles.forEach { p ->
            p.x += p.vx
            p.y += p.vy

            // Wrap around
            if (p.x < 0) p.x = 1f
            if (p.x > 1) p.x = 0f
            if (p.y < 0) p.y = 1f
            if (p.y > 1) p.y = 0f

            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(p.x * width, p.y * height)
            )
        }
    }
}
