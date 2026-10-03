package com.example.motion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.motion.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: MotionCueSettings,
    onUpdateSettings: ((MotionCueSettings) -> MotionCueSettings) -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenOemGuide: () -> Unit,
    onAddQuickSettingsTile: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
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
            // Section: Appearance
            item {
                Text(
                    text = "Appearance & Styling",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Color Selector
                        Column {
                            Text("Cue Color", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DotColorOption.values().forEach { option ->
                                    val isSelected = settings.colorOption == option
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(option.colorHex))
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                onUpdateSettings { it.copy(colorOption = option) }
                                            }
                                            .testTag("color_${option.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = option.label,
                                                tint = if (option == DotColorOption.WHITE) Color.Black else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Dot Size Selector
                        SegmentedSetting(
                            title = "Dot Size",
                            options = DotSize.values().map { it.label },
                            selectedIndex = DotSize.values().indexOf(settings.dotSize),
                            onSelect = { index ->
                                onUpdateSettings { it.copy(dotSize = DotSize.values()[index]) }
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Dot Count Selector
                        SegmentedSetting(
                            title = "Dot Count",
                            options = DotCount.values().map { it.label },
                            selectedIndex = DotCount.values().indexOf(settings.dotCount),
                            onSelect = { index ->
                                onUpdateSettings { it.copy(dotCount = DotCount.values()[index]) }
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Edge Distance
                        SegmentedSetting(
                            title = "Edge Distance",
                            options = EdgeDistance.values().map { it.label },
                            selectedIndex = EdgeDistance.values().indexOf(settings.edgeDistance),
                            onSelect = { index ->
                                onUpdateSettings { it.copy(edgeDistance = EdgeDistance.values()[index]) }
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Opacity Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Opacity", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${(settings.opacity * 100).toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = settings.opacity,
                                onValueChange = { newVal ->
                                    onUpdateSettings { it.copy(opacity = newVal) }
                                },
                                valueRange = 0.2f..1.0f,
                                steps = 7,
                                modifier = Modifier.testTag("opacity_slider")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Adaptive Contrast Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Adaptive Contrast Halo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Enhances visibility across both dark and light apps", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.adaptiveContrast,
                                onCheckedChange = { checked ->
                                    onUpdateSettings { it.copy(adaptiveContrast = checked) }
                                },
                                modifier = Modifier.testTag("adaptive_contrast_switch")
                            )
                        }
                    }
                }
            }

            // Section: Motion & Dynamics
            item {
                Text(
                    text = "Motion Dynamics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Pattern
                        SegmentedSetting(
                            title = "Motion Pattern",
                            options = MotionPattern.values().map { it.label },
                            selectedIndex = MotionPattern.values().indexOf(settings.pattern),
                            onSelect = { index ->
                                onUpdateSettings { it.copy(pattern = MotionPattern.values()[index]) }
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Sensitivity
                        SegmentedSetting(
                            title = "Sensitivity",
                            options = SensitivityLevel.values().map { it.label },
                            selectedIndex = SensitivityLevel.values().indexOf(settings.sensitivity),
                            onSelect = { index ->
                                onUpdateSettings { it.copy(sensitivity = SensitivityLevel.values()[index]) }
                            }
                        )

                        if (settings.sensitivity == SensitivityLevel.CUSTOM) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Custom Multiplier", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format("%.2fx", settings.customSensitivity), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = settings.customSensitivity,
                                    onValueChange = { newVal ->
                                        onUpdateSettings { it.copy(customSensitivity = newVal) }
                                    },
                                    valueRange = 0.4f..2.5f,
                                    modifier = Modifier.testTag("custom_sensitivity_slider")
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Animation Speed
                        SegmentedSetting(
                            title = "Animation Speed",
                            options = AnimationSpeed.values().map { it.label },
                            selectedIndex = AnimationSpeed.values().indexOf(settings.animationSpeed),
                            onSelect = { index ->
                                onUpdateSettings { it.copy(animationSpeed = AnimationSpeed.values()[index]) }
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Accessibility: Reduce Motion
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reduce Motion", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Dampens motion intensity and dot travel distances", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.reduceMotion,
                                onCheckedChange = { checked ->
                                    onUpdateSettings { it.copy(reduceMotion = checked) }
                                },
                                modifier = Modifier.testTag("reduce_motion_switch")
                            )
                        }
                    }
                }
            }

            // Section: Automation & Behavior
            item {
                Text(
                    text = "Behavior & Battery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Hide When Stopped", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Fades cues out when vehicle stops at lights or park", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.autoHideWhenStopped,
                                onCheckedChange = { checked ->
                                    onUpdateSettings { it.copy(autoHideWhenStopped = checked) }
                                },
                                modifier = Modifier.testTag("auto_hide_switch")
                            )
                        }

                        if (settings.autoHideWhenStopped) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Stop Detection Delay", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${settings.autoHideDelaySeconds}s", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = settings.autoHideDelaySeconds.toFloat(),
                                    onValueChange = { newVal ->
                                        onUpdateSettings { it.copy(autoHideDelaySeconds = newVal.toInt()) }
                                    },
                                    valueRange = 3f..15f,
                                    steps = 11,
                                    modifier = Modifier.testTag("auto_hide_delay_slider")
                                )
                            }
                        }
                    }
                }
            }

            // Section: Advanced & System
            item {
                Text(
                    text = "System & Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onAddQuickSettingsTile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_add_qs_tile_button")
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_qs_motion_cues),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Quick Settings Tile")
                        }

                        OutlinedButton(
                            onClick = onOpenDiagnostics,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_diagnostics_button")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Sensor Diagnostics")
                        }

                        OutlinedButton(
                            onClick = onOpenOemGuide,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_oem_guide_button")
                        ) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OEM & Battery Optimization Guide")
                        }
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
private fun SegmentedSetting(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                        .clickable { onSelect(index) }
                        .padding(vertical = 10.dp)
                        .testTag("option_${label.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
