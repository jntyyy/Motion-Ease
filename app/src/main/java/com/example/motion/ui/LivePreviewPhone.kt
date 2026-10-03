package com.example.motion.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motion.model.*
import kotlin.math.sin

/**
 * Interactive Live Phone Preview showing real-time motion cue dots responding to
 * live device sensors or synthetic driving simulations.
 */
@Composable
fun LivePreviewPhone(
    motion: ProcessedMotion,
    settings: MotionCueSettings,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Smooth displacement animation
    val animDispX by animateFloatAsState(
        targetValue = motion.targetDisplacementX,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "dispX"
    )
    val animDispY by animateFloatAsState(
        targetValue = motion.targetDisplacementY,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "dispY"
    )

    val dotColor = Color(settings.colorOption.colorHex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .testTag("live_phone_preview"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Simulated Phone Canvas
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
            ) {
                // Phone Screen Content & Motion Dots Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle background grid
                    drawRoundRect(
                        color = Color(0xFF1E293B).copy(alpha = 0.4f),
                        topLeft = Offset(12f, 12f),
                        size = Size(w - 24f, h - 24f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    // Center crosshair / inertial balance circle
                    drawCircle(
                        color = Color(0xFF334155).copy(alpha = 0.5f),
                        radius = 28f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )

                    // Kinetic inertial vector bead in center
                    val centerBeadX = (w / 2f) + (animDispX * 24f)
                    val centerBeadY = (h / 2f) + (animDispY * 24f)
                    drawCircle(
                        color = dotColor.copy(alpha = 0.35f),
                        radius = 9f,
                        center = Offset(centerBeadX, centerBeadY)
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 4f,
                        center = Offset(centerBeadX, centerBeadY)
                    )

                    // Draw Edge Cues on Left, Right, Top, Bottom
                    val margin = 12f
                    val dotRadius = settings.dotSize.radiusDp * 0.7f
                    val maxTravel = 16f
                    val count = settings.dotCount.countPerSide.coerceIn(3, 8)
                    val alpha = if (settings.isEnabled || motion.isSimulated) settings.opacity else 0.25f

                    // Helper for drawing edge dots
                    fun drawEdge(isVertical: Boolean, fixed: Float, start: Float, end: Float, dispN: Float, dispP: Float) {
                        val span = end - start
                        val step = if (count > 1) span / (count - 1) else span
                        for (i in 0 until count) {
                            val base = start + i * step
                            val dynamic = if (settings.pattern == MotionPattern.DYNAMIC) {
                                sin(wavePhase + i * 0.7f) * 3f
                            } else 0f

                            val cx = if (isVertical) fixed + dispN + dynamic else base + dispP
                            val cy = if (isVertical) base + dispP else fixed + dispN + dynamic

                            // Adaptive contrast shadow
                            if (settings.adaptiveContrast) {
                                drawCircle(
                                    color = Color.Black.copy(alpha = alpha * 0.6f),
                                    radius = dotRadius * 1.5f,
                                    center = Offset(cx, cy)
                                )
                            }
                            // Vivid Dot
                            drawCircle(
                                color = dotColor.copy(alpha = alpha),
                                radius = dotRadius,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // Left edge
                    drawEdge(true, margin, 40f, h - 40f, animDispX * maxTravel, animDispY * maxTravel * 0.4f)
                    // Right edge
                    drawEdge(true, w - margin, 40f, h - 40f, animDispX * maxTravel, animDispY * maxTravel * 0.4f)
                    // Top edge
                    drawEdge(false, margin + 8f, 28f, w - 28f, animDispY * maxTravel, animDispX * maxTravel * 0.4f)
                    // Bottom edge
                    drawEdge(false, h - margin - 8f, 28f, w - 28f, animDispY * maxTravel, animDispX * maxTravel * 0.4f)
                }

                // Camera punch hole mockup
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(8.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )

                // Bottom home gesture bar mockup
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .width(36.dp)
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )
            }

            // Real-time telemetry overlay pill on the preview
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = "ACCEL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.2f m/s²", motion.motionMagnitude),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // State badge at the bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (motion.isCueVisible) dotColor else Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = motion.state.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
