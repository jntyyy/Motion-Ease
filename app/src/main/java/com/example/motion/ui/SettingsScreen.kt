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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.motion.model.*

// MINI-UI: Minimal iOS-inspired styling tokens
private val BackgroundCenter = Color(0xFF14141A)
private val BackgroundEdge = Color(0xFF08080C)
private val AccentColor = Color(0xFF7DD3FC)
private val CardSurface = Color.White.copy(alpha = 0.05f)
private val TextPrimary = Color.White.copy(alpha = 0.90f)
private val TextSecondary = Color.White.copy(alpha = 0.45f)
private val TextMuted = Color.White.copy(alpha = 0.30f)

@Composable
fun SettingsScreen(
    settings: MotionCueSettings,
    onUpdateSettings: ((MotionCueSettings) -> MotionCueSettings) -> Unit,
    onSelectMode: (MotionMode) -> Unit, // MINI-UI: moved from Dashboard to General settings
    onAddQuickSettingsTile: () -> Unit, // MINI-UI: moved from Dashboard to General settings
    onOpenDiagnostics: () -> Unit,
    onOpenOemGuide: () -> Unit,
    onBack: () -> Unit
) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: iOS-like back button and title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable(onClick = onBack)
                            .testTag("settings_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.action_open_settings),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            // ==========================================
            // 1. GENERAL (MINI-UI: moved from Dashboard)
            // ==========================================
            item {
                SectionHeader(stringResource(R.string.settings_section_general))
            }

            item {
                SettingsCard {
                    // Motion Mode Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_motion_mode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MotionMode.values().forEach { mode ->
                                val isSelected = settings.mode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectMode(mode) },
                                    label = { Text(mode.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentColor.copy(alpha = 0.15f),
                                        selectedLabelColor = AccentColor,
                                        containerColor = Color.White.copy(alpha = 0.04f),
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Color.White.copy(alpha = 0.08f),
                                        selectedBorderColor = AccentColor
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mode_${mode.name.lowercase()}")
                                )
                            }
                        }
                        Text(
                            text = settings.mode.description,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Quick Settings Tile Shortcut
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.tile_motion_cues),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.desc_add_qs_tile),
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = onAddQuickSettingsTile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.10f),
                                contentColor = AccentColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("settings_add_qs_tile_button")
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_qs_motion_cues),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Tile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ==========================================
            // 2. DOTS APPEARANCE & PATTERN
            // ==========================================
            item {
                SectionHeader("Dots")
            }

            item {
                SettingsCard {
                    // Dot Size
                    OptionRow(
                        title = "Dot Size",
                        subtitle = "Diameter: ${settings.dotSize.radiusDp}dp",
                        options = DotSize.values().map { it.label },
                        selectedIndex = DotSize.values().indexOf(settings.dotSize),
                        onSelect = { index ->
                            onUpdateSettings { it.copy(dotSize = DotSize.values()[index]) }
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Dot Count
                    OptionRow(
                        title = "Dot Count",
                        subtitle = "${settings.dotCount.countPerSide} cues on each screen border",
                        options = DotCount.values().map { it.label },
                        selectedIndex = DotCount.values().indexOf(settings.dotCount),
                        onSelect = { index ->
                            onUpdateSettings { it.copy(dotCount = DotCount.values()[index]) }
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Motion Pattern
                    OptionRow(
                        title = "Motion Pattern",
                        subtitle = settings.pattern.description,
                        options = MotionPattern.values().map { it.label },
                        selectedIndex = MotionPattern.values().indexOf(settings.pattern),
                        onSelect = { index ->
                            onUpdateSettings { it.copy(pattern = MotionPattern.values()[index]) }
                        }
                    )
                }
            }

            // ==========================================
            // 3. POSITIONING
            // ==========================================
            item {
                SectionHeader("Position")
            }

            item {
                SettingsCard {
                    OptionRow(
                        title = "Edge Distance",
                        subtitle = "Spacing from display margins: ${settings.edgeDistance.marginDp}dp",
                        options = EdgeDistance.values().map { it.label },
                        selectedIndex = EdgeDistance.values().indexOf(settings.edgeDistance),
                        onSelect = { index ->
                            onUpdateSettings { it.copy(edgeDistance = EdgeDistance.values()[index]) }
                        }
                    )
                }
            }

            // ==========================================
            // 4. APPEARANCE & CONTRAST
            // ==========================================
            item {
                SectionHeader("Appearance")
            }

            item {
                SettingsCard {
                    // Color Palette
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Cue Tint",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DotColorOption.values().forEach { option ->
                                val isSelected = settings.colorOption == option
                                val swatchColor = Color(option.colorHex)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) AccentColor else Color.White.copy(alpha = 0.20f),
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
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (option == DotColorOption.WHITE) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Opacity Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Cue Opacity", fontSize = 14.sp, color = TextPrimary)
                            Text(
                                text = "${(settings.opacity * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentColor
                            )
                        }
                        Slider(
                            value = settings.opacity,
                            onValueChange = { newOp ->
                                onUpdateSettings { it.copy(opacity = newOp) }
                            },
                            valueRange = 0.20f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentColor,
                                activeTrackColor = AccentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Adaptive Contrast Halo Toggle
                    ToggleRow(
                        title = "Adaptive Contrast Halo",
                        subtitle = "Dark ring around white cues for visibility on light web pages",
                        checked = settings.adaptiveContrast,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(adaptiveContrast = checked) }
                        }
                    )
                }
            }

            // ==========================================
            // 5. BEHAVIOR & SENSITIVITY
            // ==========================================
            item {
                SectionHeader("Behavior")
            }

            item {
                SettingsCard {
                    // Sensitivity Option
                    OptionRow(
                        title = "Sensitivity Preset",
                        subtitle = "Inertia response multiplier",
                        options = SensitivityLevel.values().filter { it != SensitivityLevel.CUSTOM }.map { it.label },
                        selectedIndex = SensitivityLevel.values().filter { it != SensitivityLevel.CUSTOM }.indexOf(settings.sensitivity).coerceAtLeast(0),
                        onSelect = { index ->
                            val s = SensitivityLevel.values().filter { it != SensitivityLevel.CUSTOM }[index]
                            onUpdateSettings { it.copy(sensitivity = s, customSensitivity = s.multiplier) }
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Custom Multiplier Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Fine Multiplier", fontSize = 14.sp, color = TextPrimary)
                            Text(
                                text = String.format("%.1fx", settings.customSensitivity),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentColor
                            )
                        }
                        Slider(
                            value = settings.customSensitivity,
                            onValueChange = { value ->
                                onUpdateSettings { it.copy(customSensitivity = value) }
                            },
                            valueRange = 0.5f..3.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentColor,
                                activeTrackColor = AccentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Animation Speed
                    OptionRow(
                        title = "Animation Speed",
                        subtitle = "Inertia lerp and spring response",
                        options = AnimationSpeed.values().map { it.label },
                        selectedIndex = AnimationSpeed.values().indexOf(settings.animationSpeed),
                        onSelect = { index ->
                            onUpdateSettings { it.copy(animationSpeed = AnimationSpeed.values()[index]) }
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Reduce Motion Toggle (Accessibility)
                    ToggleRow(
                        title = "Reduce Motion",
                        subtitle = "Dampen cue displacements for sensitive vestibular systems",
                        checked = settings.reduceMotion,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(reduceMotion = checked) }
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Auto-hide When Stopped Toggle
                    ToggleRow(
                        title = "Auto-hide When Stopped",
                        subtitle = "Fade cues out smoothly when vehicle is idle",
                        checked = settings.autoHideWhenStopped,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(autoHideWhenStopped = checked) }
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Haptic Feedback Toggle
                    ToggleRow(
                        title = "Haptic Cues",
                        subtitle = "Light tactile feedback when vehicle accelerates or turns",
                        checked = settings.enableHapticFeedback,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(enableHapticFeedback = checked) }
                        }
                    )
                }
            }

            // ==========================================
            // 6. SYSTEM TOOLS & GUIDANCE
            // ==========================================
            item {
                SectionHeader("System & Tools")
            }

            item {
                SettingsCard {
                    OutlinedButton(
                        onClick = onOpenDiagnostics,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_diagnostics_button")
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sensor Diagnostics", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenOemGuide,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_oem_guide_button")
                    ) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OEM & Battery Guide", fontSize = 13.sp)
                    }
                }
            }

            // Passenger Safety Disclaimer
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.safety_disclaimer_title),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.safety_disclaimer_body),
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentColor,
                uncheckedThumbColor = Color.White.copy(alpha = 0.60f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun OptionRow(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) AccentColor.copy(alpha = 0.15f) else Color.Transparent
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
                        color = if (isSelected) AccentColor else TextSecondary
                    )
                }
            }
        }
    }
}
