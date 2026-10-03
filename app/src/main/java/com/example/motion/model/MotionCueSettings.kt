package com.example.motion.model

import androidx.compose.ui.graphics.Color

enum class MotionMode(val label: String, val description: String) {
    MANUAL("Manual", "Cues activate on demand and respond to movement"),
    AUTOMATIC("Automatic", "Activates automatically when vehicle motion patterns are detected"),
    SIMULATION("Test Simulator", "Simulate driving maneuvers to test cues without moving")
}

enum class MotionPattern(val label: String, val description: String) {
    REGULAR("Regular", "Calm, steady, and cohesive motion cues"),
    DYNAMIC("Dynamic", "Fluid and highly responsive kinetic flow")
}

enum class DotSize(val label: String, val radiusDp: Float) {
    SMALL("Small", 4.5f),
    MEDIUM("Medium", 7.0f),
    LARGE("Large", 9.5f)
}

enum class DotCount(val label: String, val countPerSide: Int) {
    FEW("Few", 4),
    NORMAL("Normal", 7),
    MANY("Many", 11)
}

enum class SensitivityLevel(val label: String, val multiplier: Float) {
    LOW("Low", 0.65f),
    MEDIUM("Medium", 1.0f),
    HIGH("High", 1.55f),
    CUSTOM("Custom", 1.0f)
}

enum class AnimationSpeed(val label: String, val speedFactor: Float) {
    SLOW("Slow", 0.65f),
    NORMAL("Normal", 1.0f),
    FAST("Fast", 1.45f)
}

enum class EdgeDistance(val label: String, val marginDp: Float) {
    NEAR("Near", 6.0f),
    NORMAL("Normal", 14.0f),
    FAR("Far", 24.0f)
}

enum class DotColorOption(val label: String, val colorHex: Long) {
    SYSTEM("System Accent", 0xFF38BDF8),
    WHITE("Crisp White", 0xFFFFFFFF),
    CYAN("Cyber Cyan", 0xFF06B6D4),
    BLUE("Electric Blue", 0xFF3B82F6),
    PURPLE("Vivid Violet", 0xFF8B5CF6),
    AMBER("Warm Amber", 0xFFF59E0B)
}

enum class SimulationScenario(val label: String, val desc: String) {
    STATIONARY("Stationary", "Vehicle is parked or idle with zero inertial push"),
    GENTLE_CRUISE("Gentle Cruise", "Steady highway cruising with subtle road undulations"),
    BRAKING("Hard Braking", "Inertia shifts forward (dots surge upward)"),
    ACCELERATION("Rapid Acceleration", "Inertia pulls back (dots shift downward)"),
    LEFT_TURN("Sharp Left Turn", "Centrifugal inertia pulls rightward (dots slide right)"),
    RIGHT_TURN("Sharp Right Turn", "Centrifugal inertia pulls leftward (dots slide left)"),
    CURVY_ROAD("Curvy Mountain Road", "Alternating lateral slalom sways with braking")
}

data class MotionCueSettings(
    val isEnabled: Boolean = false,
    val isPaused: Boolean = false,
    val mode: MotionMode = MotionMode.MANUAL,
    val pattern: MotionPattern = MotionPattern.REGULAR,
    val dotSize: DotSize = DotSize.MEDIUM,
    val dotCount: DotCount = DotCount.NORMAL,
    val opacity: Float = 0.85f,
    val sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM,
    val customSensitivity: Float = 1.0f,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val colorOption: DotColorOption = DotColorOption.SYSTEM,
    val edgeDistance: EdgeDistance = EdgeDistance.NORMAL,
    val adaptiveContrast: Boolean = true,
    val autoHideWhenStopped: Boolean = true,
    val autoHideDelaySeconds: Int = 6,
    val reduceMotion: Boolean = false,
    val enableHapticFeedback: Boolean = true
) {
    val effectiveSensitivity: Float
        get() = if (sensitivity == SensitivityLevel.CUSTOM) customSensitivity else sensitivity.multiplier
}
