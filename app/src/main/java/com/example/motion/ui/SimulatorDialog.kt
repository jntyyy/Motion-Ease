package com.example.motion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.motion.model.SimulationScenario
import com.example.ui.theme.*

/**
 * Redesigned SimulatorDialog:
 * Flat dark rounded dialog (radius 28dp, fill #333336) with iOS blue accent #0A84FF.
 */
@Composable
fun SimulatorDialog(
    currentScenario: SimulationScenario?,
    onSelectScenario: (SimulationScenario?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RadiusTokens.sheet))
                .background(FlatCardSurface)
                .testTag("simulator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header with circular close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Motion Simulator",
                            style = Typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Test cues without riding in a vehicle",
                            style = Typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FlatHeaderButtonBg)
                            .clickable(onClick = onDismiss)
                            .testTag("close_simulator_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SimulationScenario.values()) { scenario ->
                        val isSelected = currentScenario == scenario
                        val icon = when (scenario) {
                            SimulationScenario.STATIONARY -> Icons.Default.PauseCircleOutline
                            SimulationScenario.GENTLE_CRUISE -> Icons.Default.DirectionsCar
                            SimulationScenario.BRAKING -> Icons.Default.VerticalAlignTop
                            SimulationScenario.ACCELERATION -> Icons.Default.VerticalAlignBottom
                            SimulationScenario.LEFT_TURN -> Icons.Default.TurnLeft
                            SimulationScenario.RIGHT_TURN -> Icons.Default.TurnRight
                            SimulationScenario.CURVY_ROAD -> Icons.Default.AltRoute
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(RadiusTokens.md))
                                .background(if (isSelected) IosAccentBlue.copy(alpha = 0.15f) else FlatDarkBackground)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) IosAccentBlue else Color.Transparent,
                                    shape = RoundedCornerShape(RadiusTokens.md)
                                )
                                .clickable {
                                    if (isSelected) onSelectScenario(null) else onSelectScenario(scenario)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("scenario_${scenario.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) IosAccentBlue else FlatCardSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scenario.label,
                                    style = Typography.titleMedium,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) IosAccentBlue else TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = scenario.desc,
                                    style = Typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = IosAccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onSelectScenario(null)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlatCardDivider,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(RadiusTokens.pill)
                ) {
                    Text("Stop Simulation", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
