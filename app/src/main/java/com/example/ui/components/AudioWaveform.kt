package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun AudioWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave_anim")
    val barCount = 18

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            // Unique speed and delay per bar for natural frequency waves
            val delay = (i * 90) % 400
            val duration = 350 + (i * 45) % 250
            
            val heightFactor by if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 0.15f,
                    targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, delayMillis = delay, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ), label = "wave_height_$i"
                )
            } else {
                remember { mutableStateOf(0.12f) }
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(heightFactor)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            )
        }
    }
}
