package com.example.motion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    hardwareStatus: SensorHardwareStatus,
    rawReading: RawSensorReading,
    processedMotion: ProcessedMotion,
    hasOverlayPermission: Boolean,
    isServiceActive: Boolean,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Diagnostics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("diagnostics_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Hardware Sensor Availability",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SensorStatusRow("Accelerometer", hardwareStatus.hasAccelerometer, "Primary inertial source")
                        SensorStatusRow("Gyroscope", hardwareStatus.hasGyroscope, "Angular rate & cornering")
                        SensorStatusRow("Gravity Sensor", hardwareStatus.hasGravity, "Tilt orientation separation")
                        SensorStatusRow("Linear Acceleration", hardwareStatus.hasLinearAcceleration, "Hardware dynamic force")
                        SensorStatusRow("Rotation Vector", hardwareStatus.hasRotationVector, "Full 3D orientation quaternion")
                    }
                }
            }

            item {
                Text(
                    text = "Real-Time Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricItem("Motion Magnitude", String.format("%.3f m/s²", processedMotion.motionMagnitude))
                        MetricItem("Sample Rate", String.format("%.1f Hz", processedMotion.sampleRateHz))
                        MetricItem("Detection State", processedMotion.state.displayName)
                        MetricItem("Display Rotation", "${rawReading.displayRotationDegrees}°")
                        MetricItem("Screen Linear X", String.format("%.3f m/s²", rawReading.linearAccelX))
                        MetricItem("Screen Linear Y", String.format("%.3f m/s²", rawReading.linearAccelY))
                        MetricItem("Screen Linear Z", String.format("%.3f m/s²", rawReading.linearAccelZ))
                        MetricItem("Angular Velocity Z", String.format("%.3f rad/s", rawReading.gyroZ))
                        MetricItem("Computed Target X", String.format("%.2f", processedMotion.targetDisplacementX))
                        MetricItem("Computed Target Y", String.format("%.2f", processedMotion.targetDisplacementY))
                    }
                }
            }

            item {
                Text(
                    text = "System & Overlay Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SensorStatusRow("Overlay Permission", hasOverlayPermission, "SYSTEM_ALERT_WINDOW")
                        SensorStatusRow("Foreground Service", isServiceActive, "SpecialUse service active")
                        SensorStatusRow("Cue Visibility", processedMotion.isCueVisible, "Actively drawn on screen")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SensorStatusRow(name: String, isAvailable: Boolean, details: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isAvailable) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
        ) {
            Text(
                text = if (isAvailable) "AVAILABLE" else "FALLBACK",
                color = if (isAvailable) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
