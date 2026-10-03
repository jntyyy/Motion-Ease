package com.example.motion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.motion.model.*
import com.example.ui.theme.*

/**
 * Redesigned SettingsScreen:
 * Authoritative flat dark design language (flat #1C1C1E background, flat #333336 rounded cards,
 * hairline inner dividers, iOS blue #0A84FF accent, and custom iOS toggle pills).
 */
@Composable
fun SettingsScreen(
    settings: MotionCueSettings,
    onUpdateSettings: ((MotionCueSettings) -> MotionCueSettings) -> Unit,
    onSelectMode: (MotionMode) -> Unit,
    onAddQuickSettingsTile: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenOemGuide: () -> Unit,
    onOpenHelpAndSupport: () -> Unit,
    onBack: () -> Unit
) {
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
                    title = "Settings",
                    onBack = onBack,
                    backContentDescription = "Back",
                    backTestTag = "settings_back_button"
                )
            }

            // ==========================================
            // 1. GENERAL CARD
            // ==========================================
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        // Motion Mode
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                text = "Motion Mode",
                                style = Typography.titleMedium,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SmoothSlidingSegmentedControl(
                                options = MotionMode.values().map { it.label },
                                selectedIndex = MotionMode.values().indexOf(settings.mode),
                                onSelect = { idx -> onSelectMode(MotionMode.values()[idx]) },
                                testTagPrefix = "mode_"
                            )
                        }

                        FlatCardDivider()

                        // Fade-Out Delay Slider (Default 6s)
                        Column(modifier = Modifier.padding(vertical = 14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fade-out Delay",
                                    style = Typography.titleMedium,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(RadiusTokens.pill))
                                        .background(Color(0xFF222225))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${settings.autoHideDelaySeconds}s",
                                        style = Typography.labelMedium,
                                        fontSize = 14.sp,
                                        color = IosAccentBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            SmoothSleekSlider(
                                value = settings.autoHideDelaySeconds.toFloat(),
                                onValueChange = { newDelay ->
                                    onUpdateSettings { it.copy(autoHideDelaySeconds = newDelay.toInt()) }
                                },
                                valueRange = 2f..15f,
                                steps = 12
                            )
                        }

                        FlatCardDivider()

                        // Quick Settings Tile Row
                        FlatListRow(
                            title = "Quick Settings Tile",
                            subtitle = "Toggle cues directly from notification shade",
                            trailingContent = {
                                Button(
                                    onClick = onAddQuickSettingsTile,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = IosAccentBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(RadiusTokens.pill),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("settings_add_qs_tile_button")
                                ) {
                                    Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }

            // ==========================================
            // 2. CUES APPEARANCE CARD
            // ==========================================
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        // Dot Size Row
                        FlatSegmentedRow(
                            title = "Dot Size",
                            options = DotSize.values().map { it.label },
                            selectedIndex = DotSize.values().indexOf(settings.dotSize),
                            onSelect = { idx ->
                                onUpdateSettings { it.copy(dotSize = DotSize.values()[idx]) }
                            }
                        )

                        FlatCardDivider()

                        // Dot Count Row
                        FlatSegmentedRow(
                            title = "Dot Count",
                            options = DotCount.values().map { it.label },
                            selectedIndex = DotCount.values().indexOf(settings.dotCount),
                            onSelect = { idx ->
                                onUpdateSettings { it.copy(dotCount = DotCount.values()[idx]) }
                            }
                        )

                        FlatCardDivider()

                        // Motion Pattern
                        FlatSegmentedRow(
                            title = "Motion Pattern",
                            options = MotionPattern.values().map { it.label },
                            selectedIndex = MotionPattern.values().indexOf(settings.pattern),
                            onSelect = { idx ->
                                onUpdateSettings { it.copy(pattern = MotionPattern.values()[idx]) }
                            }
                        )

                        FlatCardDivider()

                        // Cue Opacity
                        Column(modifier = Modifier.padding(vertical = 14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cue Intensity (Opacity)",
                                    style = Typography.titleMedium,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(RadiusTokens.pill))
                                        .background(Color(0xFF222225))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${(settings.opacity * 100).toInt()}%",
                                        style = Typography.labelMedium,
                                        fontSize = 14.sp,
                                        color = IosAccentBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            SmoothSleekSlider(
                                value = settings.opacity,
                                onValueChange = { newOp ->
                                    onUpdateSettings { it.copy(opacity = newOp) }
                                },
                                valueRange = 0.20f..1.0f
                            )
                        }

                        FlatCardDivider()

                        // Adaptive Contrast Halo Toggle
                        FlatListRow(
                            title = "Adaptive Contrast Halo",
                            subtitle = "Dark ring around dots for visibility on white content",
                            trailingContent = {
                                FlatToggle(
                                    checked = settings.adaptiveContrast,
                                    onCheckedChange = { checked ->
                                        onUpdateSettings { it.copy(adaptiveContrast = checked) }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // ==========================================
            // 3. BEHAVIOR CARD
            // ==========================================
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        // Motion Sensitivity Multiplier
                        Column(modifier = Modifier.padding(vertical = 14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Motion Sensitivity",
                                    style = Typography.titleMedium,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(RadiusTokens.pill))
                                        .background(Color(0xFF222225))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1fx", settings.customSensitivity),
                                        style = Typography.labelMedium,
                                        fontSize = 14.sp,
                                        color = IosAccentBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            SmoothSleekSlider(
                                value = settings.customSensitivity,
                                onValueChange = { newSens ->
                                    onUpdateSettings { it.copy(customSensitivity = newSens) }
                                },
                                valueRange = 0.5f..3.0f
                            )
                        }

                        FlatCardDivider()

                        // Reduce Motion Toggle
                        FlatListRow(
                            title = "Reduce Motion",
                            subtitle = "Dampen cue movement for sensitive vestibular systems",
                            trailingContent = {
                                FlatToggle(
                                    checked = settings.reduceMotion,
                                    onCheckedChange = { checked ->
                                        onUpdateSettings { it.copy(reduceMotion = checked) }
                                    }
                                )
                            }
                        )

                        FlatCardDivider()

                        // Auto-hide When Stopped Toggle
                        FlatListRow(
                            title = "Auto-hide When Stopped",
                            subtitle = "Fade cues out smoothly when vehicle is idle",
                            trailingContent = {
                                FlatToggle(
                                    checked = settings.autoHideWhenStopped,
                                    onCheckedChange = { checked ->
                                        onUpdateSettings { it.copy(autoHideWhenStopped = checked) }
                                    }
                                )
                            }
                        )

                        FlatCardDivider()

                        // Haptic Feedback Toggle
                        FlatListRow(
                            title = "Haptic Inertia Cues",
                            subtitle = "Tactile tap on acceleration and brake impulses",
                            trailingContent = {
                                FlatToggle(
                                    checked = settings.enableHapticFeedback,
                                    onCheckedChange = { checked ->
                                        onUpdateSettings { it.copy(enableHapticFeedback = checked) }
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // ==========================================
            // 4. SUPPORT & ABOUT CARD
            // ==========================================
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        FlatListRow(
                            title = "Help & Support",
                            subtitle = "FAQ, contact support, and issue reporting",
                            trailingChevron = true,
                            onClick = onOpenHelpAndSupport
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "Sensor Diagnostics",
                            subtitle = "Real-time accelerometer, gyroscope & state machine",
                            trailingChevron = true,
                            onClick = onOpenDiagnostics,
                            modifier = Modifier.testTag("open_diagnostics_button")
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "OEM & Battery Optimization",
                            subtitle = "Keep cues running in background on Samsung, Xiaomi, etc.",
                            trailingChevron = true,
                            onClick = onOpenOemGuide,
                            modifier = Modifier.testTag("open_oem_guide_button")
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "Version",
                            trailingValue = "1.0.0 (Build 1)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlatSegmentedRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 14.dp)) {
        Text(
            text = title,
            style = Typography.titleMedium,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        SmoothSlidingSegmentedControl(
            options = options,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            testTagPrefix = "option_"
        )
    }
}
