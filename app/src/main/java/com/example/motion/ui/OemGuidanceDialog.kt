package com.example.motion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Close
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
import com.example.ui.theme.*

/**
 * Redesigned OemGuidanceDialog:
 * Flat dark card sheet (radius 28dp, fill #333336) matching authoritative design spec.
 */
@Composable
fun OemGuidanceDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RadiusTokens.sheet))
                .background(FlatCardSurface)
                .testTag("oem_guidance_dialog")
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
                            tint = IosAccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Background & OEM Guide",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FlatHeaderButtonBg)
                            .clickable(onClick = onDismiss)
                            .testTag("close_oem_guide"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OemGuideCard(
                            brand = "Samsung (One UI)",
                            instruction = "Settings → Apps → Kinetic Cues → Battery → Select 'Unrestricted'. Also disable 'Put unused apps to sleep'."
                        )
                    }
                    item {
                        OemGuideCard(
                            brand = "Xiaomi / Redmi / POCO (HyperOS / MIUI)",
                            instruction = "Settings → Apps → Manage Apps → Kinetic Cues → Enable 'Autostart' and set Battery Saver to 'No restrictions'."
                        )
                    }
                    item {
                        OemGuideCard(
                            brand = "Google Pixel & Motorola (Stock Android)",
                            instruction = "Settings → Apps → Kinetic Cues → App battery usage → Set to 'Unrestricted'."
                        )
                    }
                    item {
                        OemGuideCard(
                            brand = "OnePlus / OPPO / Realme (ColorOS)",
                            instruction = "Settings → Battery → More Settings → App Battery Management → Allow background activity."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IosAccentBlue,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(RadiusTokens.pill)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OemGuideCard(brand: String, instruction: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusTokens.md))
            .background(FlatDarkBackground)
            .padding(14.dp)
    ) {
        Text(
            text = brand,
            style = Typography.titleMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = IosAccentBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = instruction,
            style = Typography.bodySmall,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}
