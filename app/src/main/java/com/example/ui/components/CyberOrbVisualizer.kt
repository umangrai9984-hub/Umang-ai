package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CyberOrbVisualizer(
    isListening: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_rotation")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val mainColor = when {
        isListening -> CyanAccent
        isProcessing -> PurpleAccent
        isSpeaking -> PinkAccent
        else -> IndigoPrimary
    }

    val glowGradient = Brush.radialGradient(
        colors = listOf(
            mainColor.copy(alpha = 0.6f),
            IndigoPrimary.copy(alpha = 0.25f),
            Color.Transparent
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(175.dp)
            .clip(CircleShape)
            .background(Color(0x1AFFFFFF), CircleShape)
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("cyber_orb_mic_button")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.7f) * if (isListening) (1f + audioLevel * 0.35f) else pulseScale

            // Outer ambient glow
            drawCircle(
                brush = glowGradient,
                radius = baseRadius * 1.35f,
                center = center
            )

            // Frosted glass inner orb fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        mainColor.copy(alpha = 0.45f),
                        IndigoPrimary.copy(alpha = 0.2f),
                        Color(0x10000000)
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // Outer wave ring 1
            val path1 = Path()
            val points = 60
            for (i in 0..points) {
                val angle = (i.toFloat() / points) * 2 * PI.toFloat()
                val waveOffset = sin(angle * 5 + phase) * (5f + audioLevel * 16f)
                val r = baseRadius + waveOffset
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) path1.moveTo(x, y) else path1.lineTo(x, y)
            }
            path1.close()

            drawPath(
                path = path1,
                color = mainColor.copy(alpha = 0.85f),
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner wave ring 2
            val path2 = Path()
            for (i in 0..points) {
                val angle = (i.toFloat() / points) * 2 * PI.toFloat()
                val waveOffset = cos(angle * 7 - phase * 1.4f) * (3f + audioLevel * 10f)
                val r = (baseRadius * 0.82f) + waveOffset
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) path2.moveTo(x, y) else path2.lineTo(x, y)
            }
            path2.close()

            drawPath(
                path = path2,
                color = Color.White.copy(alpha = 0.5f),
                style = Stroke(width = 1.5f.dp.toPx())
            )

            // Cute glowing eyes inside orb
            val eyeOffset = 16.dp.toPx()
            val eyeY = center.y - 6.dp.toPx()
            val eyeRadius = 3.5f.dp.toPx() * (if (isSpeaking) 1.25f else 1f)

            drawCircle(
                color = Color.White,
                radius = eyeRadius,
                center = Offset(center.x - eyeOffset, eyeY)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius,
                center = Offset(center.x + eyeOffset, eyeY)
            )

            // Cute smiling curve
            val smilePath = Path().apply {
                moveTo(center.x - 10.dp.toPx(), center.y + 10.dp.toPx())
                quadraticTo(
                    center.x, center.y + (if (isSpeaking) 18.dp.toPx() else 15.dp.toPx()),
                    center.x + 10.dp.toPx(), center.y + 10.dp.toPx()
                )
            }
            drawPath(
                path = smilePath,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center Action Icon overlay in frosted badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0x33FFFFFF), CircleShape)
                .border(1.dp, Color(0x40FFFFFF), CircleShape)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            val icon = when {
                isListening -> Icons.Default.GraphicEq
                isProcessing -> Icons.Default.AutoAwesome
                else -> Icons.Default.Mic
            }
            Icon(
                imageVector = icon,
                contentDescription = "Voice Orb Mic",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

