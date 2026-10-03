package com.example.motion.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.motion.model.*
import com.example.motion.render.MotionCueRenderer
import com.example.ui.theme.*

/**
 * Minimal DashboardScreen:
 * - Top Header: Title "Motion Ease" on the left, Settings icon button on the right.
 * - Background: Solid #1F1F1F.
 * - Center: Single circular card button (solid #353638 without border) switching smoothly
 *   between the Play and Pause SVG icons.
 * - First press automatically requests system overlay permission if not granted.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun DashboardScreen(
    settings: MotionCueSettings,
    processedMotion: ProcessedMotion,
    hasOverlayPermission: Boolean,
    isSensorsAvailable: Boolean,
    currentScenario: SimulationScenario?,
    onToggleActive: () -> Unit,
    onTogglePause: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onSelectMode: (MotionMode) -> Unit,
    onAddQuickSettingsTile: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val renderer = remember { MotionCueRenderer(context) }

    val isActive = settings.isEnabled && !settings.isPaused

    // Gentle breathing pulse animation for the active state
    val shouldPulse = isActive && !settings.reduceMotion
    val infiniteTransition = rememberInfiniteTransition(label = "start_pulse")
    val pulseScale by if (shouldPulse) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.22f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }
    val pulseAlpha by if (shouldPulse) {
        infiniteTransition.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        remember { mutableFloatStateOf(0.0f) }
    }

    // Spring scaling on press
    val powerInteraction = remember { MutableInteractionSource() }
    val isPowerPressed by powerInteraction.collectIsPressedAsState()
    val powerScale by animateFloatAsState(
        targetValue = if (isPowerPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "power_press_scale"
    )

    val settingsInteraction = remember { MutableInteractionSource() }
    val isSettingsPressed by settingsInteraction.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Live Peripheral Motion Cues layer on screen edges (touch pass-through)
        if (isActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { composeCanvas ->
                    renderer.render(
                        composeCanvas.nativeCanvas,
                        size.width.toInt(),
                        size.height.toInt(),
                        processedMotion,
                        settings
                    )
                }
            }
        }

        // Header: Title "Motion Ease" on the left, Settings icon button on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Motion Ease",
                style = Typography.titleLarge,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer { alpha = if (isSettingsPressed) 0.80f else 1.0f }
                    .clip(CircleShape)
                    .background(FlatHeaderButtonBg)
                    .clickable(
                        interactionSource = settingsInteraction,
                        indication = null,
                        role = Role.Button,
                        onClick = onOpenSettings
                    )
                    .testTag("dashboard_settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_custom),
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Center: Circular Star/Toggle Button (Solid #353638 without border)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(116.dp)
                .graphicsLayer {
                    scaleX = powerScale
                    scaleY = powerScale
                }
                .clip(CircleShape)
                .clickable(
                    interactionSource = powerInteraction,
                    indication = null,
                    role = Role.Button,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Automatically open system overlay permission dialog if not yet granted
                        if (!hasOverlayPermission) {
                            onRequestOverlayPermission()
                        }
                        onToggleActive()
                    }
                )
                .testTag("main_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            // Soft breathing halo when active
            if (shouldPulse) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .clip(CircleShape)
                        .background(IosAccentBlue)
                )
            }

            // The Circular Card (Solid #353638 without border)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF353638)),
                contentAlignment = Alignment.Center
            ) {
                // Smooth icon transition between Play and Pause SVGs
                AnimatedContent(
                    targetState = isActive,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 60)) +
                                scaleIn(initialScale = 0.85f, animationSpec = tween(220, delayMillis = 60)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(180)) +
                                        scaleOut(targetScale = 0.85f, animationSpec = tween(180))
                            )
                    },
                    label = "play_pause_transition"
                ) { active ->
                    if (active) {
                        // Pause icon from SVG
                        Icon(
                            painter = painterResource(id = R.drawable.ic_start_pause),
                            contentDescription = "Pause / Stop",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    } else {
                        // Play icon from SVG (with 2dp optical center offset)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_start_play),
                            contentDescription = "Start / Play",
                            tint = Color.White,
                            modifier = Modifier
                                .size(38.dp)
                                .offset(x = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
