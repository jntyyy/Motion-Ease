package com.example.motion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwipeDown
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
import com.example.ui.theme.*

/**
 * Redesigned QuickSettingsGuideDialog:
 * Flat dark card sheet (radius 28dp, fill #333336) matching authoritative design spec.
 */
@Composable
fun QuickSettingsGuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RadiusTokens.sheet))
                .background(FlatCardSurface)
                .testTag("qs_guide_dialog")
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
                            Icons.Default.SwipeDown,
                            contentDescription = null,
                            tint = IosAccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Notification Bar Tile",
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
                            .testTag("close_qs_guide"),
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RadiusTokens.md))
                        .background(FlatDarkBackground)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Swipe down twice from the top of your screen to open full Quick Settings.",
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "2. Tap the Pencil / Edit icon to customize available tiles.",
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "3. Find 'Kinetic Cues' and drag it into your active quick tiles.",
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "4. Tap the tile anytime while riding in a car to instantly toggle cues.",
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IosAccentBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(RadiusTokens.pill)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
