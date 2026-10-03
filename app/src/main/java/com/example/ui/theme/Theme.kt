package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Standard Spacing Scale
object SpacingTokens {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// Corner Radius Scale
object RadiusTokens {
    val sm = 8.dp
    val md = 14.dp
    val card = 26.dp // Authoritative card radius (24-28dp)
    val sheet = 28.dp
    val pill = 999.dp
    val circularHeader = 40.dp
}

private val DarkColorScheme = darkColorScheme(
    primary = IosAccentBlue,
    onPrimary = Color.White,
    primaryContainer = FlatCardSurface,
    onPrimaryContainer = IosAccentBlue,
    secondary = TextSecondary,
    onSecondary = Color.White,
    tertiary = StatusActiveGreen,
    background = FlatDarkBackground,
    surface = FlatCardSurface,
    surfaceVariant = FlatCardDivider,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = FlatCardDivider,
    outlineVariant = TextMuted
)

private val LightColorScheme = DarkColorScheme // Flat dark theme is enforced across all environments

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always enforce flat dark design language
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
