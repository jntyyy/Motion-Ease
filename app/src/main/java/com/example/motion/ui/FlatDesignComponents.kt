package com.example.motion.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Standard Header Component matching authoritative screenshots:
 * Circular dark button (40dp, white @ 8% bg) on left with back/close icon, centered bold title.
 */
@Composable
fun FlatDarkHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    backContentDescription: String = "Back",
    backTestTag: String = "header_back_button",
    trailingContent: @Composable (RowScope.() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        if (onBack != null) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .graphicsLayer {
                        alpha = if (isPressed) 0.85f else 1.0f
                    }
                    .clip(CircleShape)
                    .background(FlatHeaderButtonBg)
                    .border(0.5.dp, FlatHeaderButtonBorder, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = backIcon,
                    contentDescription = backContentDescription,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Centered bold title (20sp)
        Text(
            text = title,
            style = Typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.align(Alignment.Center)
        )

        if (trailingContent != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                content = trailingContent
            )
        }
    }
}

/**
 * RoundedCard: radius 26dp, fill #333336 (flat, non-blur, non-gradient), hairline inner dividers
 */
@Composable
fun FlatRoundedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusTokens.card))
            .background(FlatCardSurface)
            .padding(horizontal = 20.dp, vertical = 4.dp),
        content = content
    )
}

/**
 * Hairline Divider for inside RoundedCards
 */
@Composable
fun FlatCardDivider() {
    HorizontalDivider(
        color = FlatCardDivider,
        thickness = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Standard ListRow: icon + label left (18sp white), trailing chevron / external-link / toggle / value right.
 * Minimum touch target 48dp, padding 20dp.
 */
@Composable
fun FlatListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = TextPrimary,
    trailingChevron: Boolean = false,
    trailingExternalLink: Boolean = false,
    trailingValue: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .graphicsLayer { alpha = if (isPressed) 0.85f else 1.0f }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 18.dp)
    } else {
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(vertical = 18.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = leadingIconTint,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 14.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = Typography.titleMedium,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (!trailingValue.isNullOrBlank()) {
                Text(
                    text = trailingValue,
                    style = Typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            if (trailingContent != null) {
                trailingContent()
            } else if (trailingExternalLink) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "External link",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            } else if (trailingChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Navigate",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * iOS-style Toggle Pill:
 * Gray #636366 track with dark knob when OFF,
 * #0A84FF track with white knob when ON.
 */
@Composable
fun FlatToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 50.dp
    val trackHeight = 30.dp
    val knobSize = 26.dp
    val padding = 2.dp

    val trackColor by animateColorAsState(
        targetValue = if (checked) ToggleTrackOn else ToggleTrackOff,
        animationSpec = tween(durationMillis = 200),
        label = "toggle_track_color"
    )

    val knobColor by animateColorAsState(
        targetValue = if (checked) ToggleKnobOn else ToggleKnobOff,
        animationSpec = tween(durationMillis = 200),
        label = "toggle_knob_color"
    )

    val knobOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - knobSize - padding else padding,
        animationSpec = tween(durationMillis = 200),
        label = "toggle_knob_offset"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = knobOffset)
                .size(knobSize)
                .clip(CircleShape)
                .background(knobColor)
        )
    }
}

/**
 * Ultra-smooth iOS-style Sliding Segmented Control:
 * An animated indicator pill slides smoothly under options with haptic feedback.
 */
@Composable
fun SmoothSlidingSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "option_"
) {
    val haptic = LocalHapticFeedback.current
    val containerPadding = 4.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Color(0xFF222225))
            .padding(containerPadding)
    ) {
        val totalWidth = maxWidth
        val count = options.size.coerceAtLeast(1)
        val segmentWidth = totalWidth / count

        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 450f
            ),
            label = "segmented_indicator_offset"
        )

        // Smooth sliding indicator pill (Fully circular)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .defaultMinSize(minHeight = 40.dp)
                .clip(CircleShape)
                .background(IosAccentBlue)
        )

        // Option items on top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else TextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "segmented_text_color"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 40.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.RadioButton,
                            onClick = {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelect(index)
                                }
                            }
                        )
                        .padding(vertical = 8.dp)
                        .testTag("$testTagPrefix${option.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = Typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Ultra-smooth tactile Slider with custom track, floating thumb, and haptic feedback.
 */
@Composable
fun SmoothSleekSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "slider_thumb_scale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(valueRange, steps) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    var newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                    if (steps > 0) {
                        val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
                        newValue = (kotlin.math.round((newValue - valueRange.start) / stepSize) * stepSize + valueRange.start)
                            .coerceIn(valueRange.start, valueRange.endInclusive)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onValueChange(newValue)
                }
            }
            .pointerInput(valueRange, steps) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        var newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        if (steps > 0) {
                            val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
                            newValue = (kotlin.math.round((newValue - valueRange.start) / stepSize) * stepSize + valueRange.start)
                                .coerceIn(valueRange.start, valueRange.endInclusive)
                        }
                        onValueChange(newValue)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val width = maxWidth
        val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val thumbRadius = 11.dp
        val activeWidth = width * fraction

        // Inactive background track (sleek dark pill)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF222225))
        )

        // Active progress track (vibrant iOS blue)
        Box(
            modifier = Modifier
                .width(activeWidth.coerceAtLeast(8.dp))
                .height(8.dp)
                .clip(CircleShape)
                .background(IosAccentBlue)
        )

        // Floating thumb with subtle shadow and expansion
        val thumbOffset = ((width - (thumbRadius * 2)) * fraction)
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbRadius * 2)
                .graphicsLayer {
                    scaleX = thumbScale
                    scaleY = thumbScale
                    shadowElevation = 8.dp.toPx()
                    shape = CircleShape
                    clip = true
                }
                .background(Color.White)
        )
    }
}

