package com.example.motion.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.motion.model.*
import com.example.motion.render.MotionCueRenderer

// MINI-UI: Minimal iOS-inspired dark aesthetic color constants
private val BackgroundCenter = Color(0xFF14141A)
private val BackgroundEdge = Color(0xFF08080C)
private val AccentCyan = Color(0xFF7DD3FC)
private val StatusReady = Color.White.copy(alpha = 0.45f)
private val StatusActive = Color(0xFF34D399)
private val StatusPaused = Color(0xFF60A5FA)
private val StatusPermission = Color(0xFFFBBF24)
private val StatusUnavailable = Color(0xFFF87171)

/**
 * Rebuilt DashboardScreen: Ultra-clean, minimal luxury dark dashboard with iOS-like simplicity.
 * Renders live peripheral motion cues on screen edges when active and ensures 100% responsiveness.
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
    onSelectMode: (MotionMode) -> Unit, // MINI-UI: moved to SettingsScreen General section
    onAddQuickSettingsTile: () -> Unit // MINI-UI: moved to SettingsScreen General section
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val renderer = remember { MotionCueRenderer(context) }

    // Active state responds immediately to toggle
    val isActive = settings.isEnabled && !settings.isPaused

    // Single subtle breathing pulse for active state (60fps, zero lag)
    val infiniteTransition = rememberInfiniteTransition(label = "power_glow_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Interaction sources for smooth spring press animations
    val powerInteractionSource = remember { MutableInteractionSource() }
    val isPowerPressed by powerInteractionSource.collectIsPressedAsState()
    val powerScale by animateFloatAsState(
        targetValue = if (isPowerPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "power_press_scale"
    )

    val settingsInteractionSource = remember { MutableInteractionSource() }
    val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()
    val settingsScale by animateFloatAsState(
        targetValue = if (isSettingsPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "settings_press_scale"
    )

    // Determine current status state and colors
    val (statusText, statusColor) = when {
        !isSensorsAvailable -> Pair(stringResource(R.string.status_sensors_unavailable), StatusUnavailable)
        !settings.isEnabled -> Pair(stringResource(R.string.status_ready), StatusReady)
        settings.isPaused -> Pair(stringResource(R.string.status_paused), StatusPaused)
        else -> Pair(stringResource(R.string.status_active), StatusActive)
    }

    // MINI-UI: Root Box with smooth dark radial gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(BackgroundCenter, BackgroundEdge),
                    radius = 1400f
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Live Peripheral Motion Cues on Screen Margins (Touch pass-through)
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

        // ① Header: App title + Subtitle on start, circular iOS-like Settings button on end
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.90f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.app_subtitle),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.40f)
                )
            }

            // MINI-UI: Circular 42dp settings button with 0.92 spring scale
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer {
                        scaleX = settingsScale
                        scaleY = settingsScale
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable(
                        interactionSource = settingsInteractionSource,
                        indication = ripple(bounded = true, radius = 24.dp, color = Color.White),
                        onClick = onOpenSettings
                    )
                    .testTag("dashboard_settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.action_open_settings),
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ② Central Column: Star Power Button + Animated Status Pill + Secondary Subtitle
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big Circular Toggle Button (112dp touch area > 96dp circle)
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .graphicsLayer {
                        scaleX = powerScale
                        scaleY = powerScale
                    }
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = powerInteractionSource,
                        indication = ripple(bounded = true, radius = 56.dp, color = AccentCyan),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleActive()
                        }
                    )
                    .testTag("main_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                // Glow Pulse Ring (active state only)
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha
                            }
                            .clip(CircleShape)
                            .border(2.5.dp, AccentCyan, CircleShape)
                    )
                }

                // Inner 96dp Circle
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(
                            width = 2.5.dp,
                            color = if (isActive) AccentCyan else Color.White.copy(alpha = 0.22f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = if (isActive) stringResource(R.string.action_disable) else stringResource(R.string.action_enable),
                        tint = if (isActive) AccentCyan else Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status Pill with smooth crossfade animation
            AnimatedContent(
                targetState = Pair(statusText, statusColor),
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "status_crossfade"
            ) { (text, color) ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(color.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                        .testTag("system_status_pill"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Secondary subtle guidance text
            Text(
                text = when {
                    !settings.isEnabled -> stringResource(R.string.action_tap_to_start)
                    settings.isPaused -> stringResource(R.string.action_tap_to_resume)
                    else -> stringResource(R.string.action_tap_to_stop)
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center
            )
        }

        // ③ Slim Permission Banner: Displayed only when !hasOverlayPermission
        AnimatedVisibility(
            visible = !hasOverlayPermission,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp)
                .padding(bottom = 60.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = StatusUnavailable.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusUnavailable.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.status_permission_needed),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusUnavailable
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.permission_banner_desc),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.60f),
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TextButton(
                        onClick = onRequestOverlayPermission,
                        modifier = Modifier.testTag("grant_permission_button")
                    ) {
                        Text(
                            text = stringResource(R.string.action_grant),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = StatusUnavailable
                        )
                    }
                }
            }
        }

        // ④ Bottom Row: Simulator & Diagnostics subtle text buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onOpenSimulator,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_simulator),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.40f)
                )
            }

            Text(
                text = "•",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.20f)
            )

            TextButton(
                onClick = onOpenDiagnostics,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_diagnostics),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.40f)
                )
            }
        }
    }
}
