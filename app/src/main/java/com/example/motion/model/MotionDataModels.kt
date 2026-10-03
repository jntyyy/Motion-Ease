package com.example.motion.model

/**
 * Hardware availability of motion sensors on the device.
 */
data class SensorHardwareStatus(
    val hasAccelerometer: Boolean = false,
    val hasGyroscope: Boolean = false,
    val hasGravity: Boolean = false,
    val hasLinearAcceleration: Boolean = false,
    val hasRotationVector: Boolean = false,
    val activeSensorCount: Int = 0
) {
    val isOperational: Boolean
        get() = hasAccelerometer // Accelerometer is minimum requirement; other sensors enhance precision
}

/**
 * Raw instantaneous sensor values extracted from the Android Sensor Framework.
 */
data class RawSensorReading(
    val timestampNs: Long = 0L,
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val linearAccelX: Float = 0f,
    val linearAccelY: Float = 0f,
    val linearAccelZ: Float = 0f,
    val gravityX: Float = 0f,
    val gravityY: Float = 0f,
    val gravityZ: Float = 0f,
    val rotationRoll: Float = 0f,
    val rotationPitch: Float = 0f,
    val rotationYaw: Float = 0f,
    val displayRotationDegrees: Int = 0
)

/**
 * Vehicle Detection States in the state machine.
 */
enum class VehicleDetectionState(val displayName: String) {
    IDLE("Stationary / Idle"),
    POSSIBLE_MOTION("Detecting Motion…"),
    VEHICLE_MOTION("Vehicle Motion Detected"),
    ACTIVE("Active & Tracking"),
    LOW_MOTION("Low Motion / Cruising"),
    STOPPED("Vehicle Stopped")
}

/**
 * Processed motion output used by the cue rendering engine.
 */
data class ProcessedMotion(
    val state: VehicleDetectionState = VehicleDetectionState.IDLE,
    val targetDisplacementX: Float = 0f, // in range [-1.0f .. 1.0f]
    val targetDisplacementY: Float = 0f, // in range [-1.0f .. 1.0f]
    val rotationBias: Float = 0f,       // in range [-1.0f .. 1.0f]
    val motionMagnitude: Float = 0f,    // in m/s^2
    val confidence: Float = 0f,         // in [0.0f .. 1.0f]
    val isCueVisible: Boolean = false,
    val sampleRateHz: Float = 0f,
    val isSimulated: Boolean = false
)
