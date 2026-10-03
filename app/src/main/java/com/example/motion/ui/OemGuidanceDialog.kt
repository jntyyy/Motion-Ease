package com.example.motion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun OemGuidanceDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp))
                .testTag("oem_guidance_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Background & OEM Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_oem_guide")) {
                        Icon(Icons.Default.Close, contentDescription = "Close guide")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        OemGuideCard(
                            brand = "Xiaomi / HyperOS / MIUI",
                            instructions = listOf(
                                "Go to Settings > Apps > Manage Apps > Kinetic Cues.",
                                "Enable 'Autostart' to allow service transitions.",
                                "In 'Battery Saver', choose 'No restrictions' so sensors aren't throttled when screen is active."
                            )
                        )
                    }

                    item {
                        OemGuideCard(
                            brand = "Samsung (One UI)",
                            instructions = listOf(
                                "Go to Settings > Battery and device care > Battery.",
                                "Tap 'Background usage limits' > 'Never sleeping apps'.",
                                "Add Kinetic Cues to prevent OS from killing overlay while using maps or videos."
                            )
                        )
                    }

                    item {
                        OemGuideCard(
                            brand = "Google Pixel & Clean Android",
                            instructions = listOf(
                                "Settings > Apps > Kinetic Cues > App battery usage.",
                                "Select 'Unrestricted' for continuous 60fps sensor processing."
                            )
                        )
                    }

                    item {
                        OemGuideCard(
                            brand = "OnePlus / OPPO / Realme (ColorOS / OxygenOS)",
                            instructions = listOf(
                                "Settings > Battery > More battery settings > App battery management.",
                                "Find Kinetic Cues and allow 'Background activity' and 'Auto-launch'."
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_oem_button")
                ) {
                    Text("Got It")
                }
            }
        }
    }
}

@Composable
private fun OemGuideCard(brand: String, instructions: List<String>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = brand,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            instructions.forEachIndexed { index, inst ->
                Text(
                    text = "${index + 1}. $inst",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
