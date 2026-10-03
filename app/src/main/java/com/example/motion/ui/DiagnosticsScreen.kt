package com.example.motion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motion.model.ProcessedMotion
import com.example.motion.model.RawSensorReading
import com.example.motion.model.SensorHardwareStatus
import com.example.ui.theme.*

/**
 * Redesigned DiagnosticsScreen:
 * Flat dark gray rounded cards on #1C1C1E background, displaying live vehicle state machine,
 * high-precision sensor telemetry, and hardware integrity.
 */
@Composable
fun DiagnosticsScreen(
    hardwareStatus: SensorHardwareStatus,
    rawReading: RawSensorReading,
    processedMotion: ProcessedMotion,
    hasOverlayPermission: Boolean,
    isServiceActive: Boolean,
    onBack: () -> Unit
) {
    val currentState = if (!isServiceActive) "IDLE" else processedMotion.state.name

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlatDarkBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg)
        ) {
            // Header
            item {
                FlatDarkHeader(
                    title = "Diagnostics",
                    onBack = onBack,
                    backContentDescription = "Back",
                    backTestTag = "diagnostics_back_button"
                )
            }

            // Card 1: State Machine Flow Visualization
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vehicle State Machine",
                                    style = Typography.titleMedium,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(RadiusTokens.pill))
                                        .background(StatusActiveGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(StatusActiveGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentState,
                                        style = Typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusActiveGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Steps visualizer
                            val states = listOf("IDLE", "POSSIBLE", "VEHICLE", "ACTIVE", "LOW", "STOPPED")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(RadiusTokens.md))
                                    .background(FlatDarkBackground)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                states.forEach { s ->
                                    val isMatch = currentState.startsWith(s) || (s == "ACTIVE" && currentState == "ACTIVE")
                                    Text(
                                        text = s,
                                        fontSize = 10.sp,
                                        fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isMatch) IosAccentBlue else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Live Sensor Telemetry
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                text = "Sensor Telemetry",
                                style = Typography.titleMedium,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            TelemetryRow(
                                label = "Linear Acceleration (m/s²)",
                                value = "X: ${String.format("%+.2f", rawReading.linearAccelX)}  Y: ${String.format("%+.2f", rawReading.linearAccelY)}  Z: ${String.format("%+.2f", rawReading.linearAccelZ)}"
                            )
                            FlatCardDivider()
                            TelemetryRow(
                                label = "Gyroscope Angular Rate (rad/s)",
                                value = "X: ${String.format("%+.2f", rawReading.gyroX)}  Y: ${String.format("%+.2f", rawReading.gyroY)}  Z: ${String.format("%+.2f", rawReading.gyroZ)}"
                            )
                            FlatCardDivider()
                            TelemetryRow(
                                label = "Target Cue Displacement",
                                value = "ΔX: ${String.format("%+.2f", processedMotion.targetDisplacementX)}  ΔY: ${String.format("%+.2f", processedMotion.targetDisplacementY)}"
                            )
                        }
                    }
                }
            }

            // Card 3: Hardware Sensor Integrity
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        FlatListRow(
                            title = "Accelerometer",
                            subtitle = if (hardwareStatus.hasAccelerometer) "Hardware detected (High Precision)" else "Hardware missing",
                            trailingContent = {
                                StatusBadge(
                                    text = if (hardwareStatus.hasAccelerometer) "OK" else "MISSING",
                                    isSuccess = hardwareStatus.hasAccelerometer
                                )
                            }
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "Gyroscope",
                            subtitle = if (hardwareStatus.hasGyroscope) "Hardware detected (High Precision)" else "Hardware missing (Fallback active)",
                            trailingContent = {
                                StatusBadge(
                                    text = if (hardwareStatus.hasGyroscope) "OK" else "FALLBACK",
                                    isSuccess = hardwareStatus.hasGyroscope
                                )
                            }
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "Overlay System Window",
                            subtitle = if (hasOverlayPermission) "Permission granted" else "Permission required",
                            trailingContent = {
                                StatusBadge(
                                    text = if (hasOverlayPermission) "GRANTED" else "REQUIRED",
                                    isSuccess = hasOverlayPermission
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(text = label, style = Typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun StatusBadge(text: String, isSuccess: Boolean) {
    val color = if (isSuccess) StatusActiveGreen else StatusWarningAmber
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(RadiusTokens.pill))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
