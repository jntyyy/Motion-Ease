package com.example.motion.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

/**
 * Pixel-faithful Help & Support Screen matching authoritative reference screenshots:
 * Flat dark gray rounded cards on near-black background, iOS blue inline links,
 * and custom iOS-style toggle switches.
 */
@Composable
fun HelpAndSupportScreen(
    onBack: () -> Unit,
    onOpenSafetyDisclaimer: () -> Unit
) {
    val context = LocalContext.current
    var shakeToReportEnabled by remember { mutableStateOf(false) }
    var showReportSentDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlatDarkBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg)
        ) {
            // Header
            item {
                FlatDarkHeader(
                    title = "Help & Support",
                    onBack = onBack,
                    backContentDescription = "Back to Settings",
                    backTestTag = "help_back_button"
                )
            }

            // Card 1: Help Centre & Contact
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        FlatListRow(
                            title = "Kinetic Cues Help Centre",
                            subtitle = "Articles, guides, and science behind motion cues",
                            trailingExternalLink = true,
                            onClick = {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://support.google.com/android")
                                )
                                context.startActivity(browserIntent)
                            }
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "Contact customer support",
                            subtitle = "Average response time: under 2 hours",
                            trailingChevron = true,
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@kineticcues.app")
                                    putExtra(Intent.EXTRA_SUBJECT, "Kinetic Cues Support Request")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    // Fallback handled
                                }
                            }
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "Report an issue",
                            subtitle = "Submit sensor logs and bug feedback",
                            trailingChevron = true,
                            onClick = {
                                showReportSentDialog = true
                            }
                        )
                    }
                }
            }

            // Caption Link (Reference style: gray sentence with blue inline link)
            item {
                val captionText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextSecondary, fontSize = 14.sp)) {
                        append("Need support with passenger motion discomfort? ")
                    }
                    pushStringAnnotation(tag = "contact", annotation = "contact")
                    withStyle(
                        style = SpanStyle(
                            color = IosAccentBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    ) {
                        append("Contact us")
                    }
                    pop()
                }

                Text(
                    text = captionText,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clickable {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@kineticcues.app")
                                putExtra(Intent.EXTRA_SUBJECT, "Passenger Support Question")
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                )
            }

            // Card 2: Interactive Troubleshooting
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        FlatListRow(
                            title = "Shake phone to report an issue",
                            subtitle = "Quickly trigger bug report dialog while testing in vehicle",
                            trailingContent = {
                                FlatToggle(
                                    checked = shakeToReportEnabled,
                                    onCheckedChange = { shakeToReportEnabled = it }
                                )
                            }
                        )
                    }
                }
            }

            // Card 3: Safety & Privacy
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FlatRoundedCard {
                        FlatListRow(
                            title = "Passenger Safety Notice",
                            subtitle = "Important guidelines for passengers in moving vehicles",
                            trailingChevron = true,
                            onClick = onOpenSafetyDisclaimer
                        )
                        FlatCardDivider()
                        FlatListRow(
                            title = "Privacy & Zero Sensor Tracking",
                            subtitle = "Inertia readings are processed locally in real-time",
                            trailingChevron = true,
                            onClick = {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://policies.google.com/privacy")
                                )
                                context.startActivity(browserIntent)
                            }
                        )
                    }
                }
            }
        }

        // Report dialog
        if (showReportSentDialog) {
            AlertDialog(
                onDismissRequest = { showReportSentDialog = false },
                containerColor = FlatCardSurface,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary,
                title = { Text("Report Issue", fontWeight = FontWeight.Bold) },
                text = { Text("Would you like to send real-time sensor diagnostics to customer support?") },
                confirmButton = {
                    TextButton(
                        onClick = { showReportSentDialog = false }
                    ) {
                        Text("Send Logs", color = IosAccentBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportSentDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
