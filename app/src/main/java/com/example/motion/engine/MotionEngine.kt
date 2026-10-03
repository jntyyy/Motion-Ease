package com.example.motion.engine

import com.example.motion.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Advanced Motion Processing Engine responsible for:
 * 1. Noise filtering and deadband isolation
 * 2. Complementary sensor fusion (Linear acceleration + Gyroscope)
 * 3. Screen-space inertial vector translation
 * 4. Multi-stage Vehicle Motion State Machine with hysteresis
 * 5. Synthetic Simulation Mode for testing without physical vehicles
 */
class MotionEngine {

    private val _processedMotion = MutableStateFlow(ProcessedMotion())
    val processedMotion: StateFlow<ProcessedMotion> = _processedMotion.asStateFlow()

    // Filter states
    private var filteredLinearX = 0f
    private var filteredLinearY = 0f
    private var filteredLinearZ = 0f
    private var filteredGyroZ = 0f

    // Vehicle detection state machine variables
    private var currentState = VehicleDetectionState.IDLE
    private var sustainedMotionFrames = 0
    private var lowMotionFrames = 0
    private var stationaryTimestamp = 0L

    // Deadbands and threshold constants
    private val noiseThreshold = 0.16f       // m/s^2 noise floor
    private val vehicleMotionThreshold = 0.38f // m/s^2 to trigger possible vehicle motion
    private val sustainedFramesRequired = 8  // ~150ms of sustained inertial change
    private val lowMotionFramesThreshold = 180 // ~3 seconds at 60Hz

    // Simulation state
    private var isSimulating = false
    private var activeScenario: SimulationScenario? = null
    private var simulationStep = 0f

    /**
     * Process an incoming sensor frame using user configuration.
     */
    fun processFrame(raw: RawSensorReading, settings: MotionCueSettings) {
        // If simulation mode is active, generate synthetic dynamics
        if (settings.mode == MotionMode.SIMULATION || isSimulating) {
            processSimulationStep(settings)
            return
        }

        // Apply Deadband to eliminate sensor noise
        val cleanLinearX = applyDeadband(raw.linearAccelX, noiseThreshold)
        val cleanLinearY = applyDeadband(raw.linearAccelY, noiseThreshold)
        val cleanLinearZ = applyDeadband(raw.linearAccelZ, noiseThreshold)
        val cleanGyroZ = applyDeadband(raw.gyroZ, 0.04f)

        // Smoothing filter: Low-pass / Exponential Moving Average
        // Dynamic mode uses faster response, Regular mode uses stronger smoothing
        val filterFactor = when (settings.pattern) {
            MotionPattern.REGULAR -> 0.18f
            MotionPattern.DYNAMIC -> 0.32f
        }

        filteredLinearX += filterFactor * (cleanLinearX - filteredLinearX)
        filteredLinearY += filterFactor * (cleanLinearY - filteredLinearY)
        filteredLinearZ += filterFactor * (cleanLinearZ - filteredLinearZ)
        filteredGyroZ += filterFactor * (cleanGyroZ - filteredGyroZ)

        // Compute 2D and 3D kinetic magnitude
        val planarMagnitude = sqrt(filteredLinearX * filteredLinearX + filteredLinearY * filteredLinearY)
        val totalMagnitude = sqrt(planarMagnitude * planarMagnitude + filteredLinearZ * filteredLinearZ)

        // Vehicle Detection State Machine Update
        updateStateMachine(planarMagnitude, settings)

        // Compute screen displacement vectors based on inertia physics:
        // Physical inertia opposes acceleration:
        // Braking (-Y acceleration) pushes body forward (+Y relative on screen) -> dots move up
        // Acceleration (+Y) pulls body back (-Y) -> dots move down
        // Left turn (centripetal force right, inertia left) -> dots move right
        // Right turn -> dots move left
        val sensitivity = settings.effectiveSensitivity
        val maxAccelReference = 4.0f // 4 m/s^2 corresponds to strong vehicle braking/turn

        // Inverted inertial response
        var targetX = (-filteredLinearX / maxAccelReference) * sensitivity
        var targetY = (filteredLinearY / maxAccelReference) * sensitivity

        // Complementary lateral force from cornering yaw rate
        val corneringInertia = (-filteredGyroZ * 0.4f) * sensitivity
        targetX += corneringInertia

        // Clamp to [-1.0f .. 1.0f]
        targetX = targetX.coerceIn(-1.0f, 1.0f)
        targetY = targetY.coerceIn(-1.0f, 1.0f)
        val rotationBias = (-filteredGyroZ * 0.5f).coerceIn(-1.0f, 1.0f)

        val isVisible = determineVisibility(settings)

        _processedMotion.value = ProcessedMotion(
            state = currentState,
            targetDisplacementX = targetX,
            targetDisplacementY = targetY,
            rotationBias = rotationBias,
            motionMagnitude = planarMagnitude,
            confidence = (planarMagnitude / 2.0f).coerceIn(0f, 1f),
            isCueVisible = isVisible,
            sampleRateHz = 60f,
            isSimulated = false
        )
    }

    private fun updateStateMachine(magnitude: Float, settings: MotionCueSettings) {
        val now = System.currentTimeMillis()

        when (currentState) {
            VehicleDetectionState.IDLE -> {
                if (magnitude > vehicleMotionThreshold) {
                    sustainedMotionFrames++
                    if (sustainedMotionFrames >= 3) {
                        currentState = VehicleDetectionState.POSSIBLE_MOTION
                    }
                } else {
                    sustainedMotionFrames = 0
                }
            }

            VehicleDetectionState.POSSIBLE_MOTION -> {
                if (magnitude > vehicleMotionThreshold) {
                    sustainedMotionFrames++
                    if (sustainedMotionFrames >= sustainedFramesRequired) {
                        currentState = VehicleDetectionState.VEHICLE_MOTION
                    }
                } else {
                    sustainedMotionFrames = max(0, sustainedMotionFrames - 1)
                    if (sustainedMotionFrames == 0) {
                        currentState = VehicleDetectionState.IDLE
                    }
                }
            }

            VehicleDetectionState.VEHICLE_MOTION -> {
                currentState = VehicleDetectionState.ACTIVE
                lowMotionFrames = 0
            }

            VehicleDetectionState.ACTIVE -> {
                if (magnitude < vehicleMotionThreshold * 0.6f) {
                    lowMotionFrames++
                    if (lowMotionFrames >= lowMotionFramesThreshold) {
                        currentState = VehicleDetectionState.LOW_MOTION
                        stationaryTimestamp = now
                    }
                } else {
                    lowMotionFrames = 0
                }
            }

            VehicleDetectionState.LOW_MOTION -> {
                if (magnitude > vehicleMotionThreshold) {
                    currentState = VehicleDetectionState.ACTIVE
                    lowMotionFrames = 0
                } else {
                    val stoppedDurationSeconds = (now - stationaryTimestamp) / 1000L
                    if (stoppedDurationSeconds >= settings.autoHideDelaySeconds) {
                        currentState = VehicleDetectionState.STOPPED
                    }
                }
            }

            VehicleDetectionState.STOPPED -> {
                if (magnitude > vehicleMotionThreshold) {
                    currentState = VehicleDetectionState.ACTIVE
                    lowMotionFrames = 0
                    sustainedMotionFrames = 4
                }
            }
        }
    }

    private fun determineVisibility(settings: MotionCueSettings): Boolean {
        if (!settings.isEnabled || settings.isPaused) return false

        return when (settings.mode) {
            MotionMode.MANUAL -> {
                if (settings.autoHideWhenStopped && currentState == VehicleDetectionState.STOPPED) {
                    false
                } else {
                    true
                }
            }

            MotionMode.AUTOMATIC -> {
                // In automatic mode, cues only show when vehicle motion is confirmed
                currentState == VehicleDetectionState.ACTIVE ||
                        currentState == VehicleDetectionState.VEHICLE_MOTION ||
                        currentState == VehicleDetectionState.LOW_MOTION
            }

            MotionMode.SIMULATION -> true
        }
    }

    /**
     * Injects a simulation scenario to let users test vehicle motion cues.
     */
    fun setSimulationScenario(scenario: SimulationScenario?) {
        if (scenario == null) {
            isSimulating = false
            activeScenario = null
            simulationStep = 0f
            currentState = VehicleDetectionState.IDLE
        } else {
            isSimulating = true
            activeScenario = scenario
            simulationStep = 0f
            currentState = VehicleDetectionState.ACTIVE
        }
    }

    private fun processSimulationStep(settings: MotionCueSettings) {
        val scenario = activeScenario ?: SimulationScenario.GENTLE_CRUISE
        simulationStep += 0.05f

        val (targetX, targetY, rot, mag) = when (scenario) {
            SimulationScenario.STATIONARY -> {
                currentState = VehicleDetectionState.IDLE
                Quad(0f, 0f, 0f, 0.05f)
            }

            SimulationScenario.GENTLE_CRUISE -> {
                currentState = VehicleDetectionState.ACTIVE
                val sway = sin(simulationStep * 0.7f) * 0.15f
                val road = sin(simulationStep * 1.5f) * 0.08f
                Quad(sway, road, sway * 0.2f, 0.5f)
            }

            SimulationScenario.BRAKING -> {
                currentState = VehicleDetectionState.ACTIVE
                val surge = (sin(simulationStep * 1.2f) * 0.65f).coerceAtLeast(0f)
                Quad(0f, surge, 0f, 1.8f)
            }

            SimulationScenario.ACCELERATION -> {
                currentState = VehicleDetectionState.ACTIVE
                val pullBack = -(sin(simulationStep * 1.2f) * 0.65f).coerceAtLeast(0f)
                Quad(0f, pullBack, 0f, 1.8f)
            }

            SimulationScenario.LEFT_TURN -> {
                currentState = VehicleDetectionState.ACTIVE
                val centrifugal = (sin(simulationStep * 0.9f) * 0.75f).coerceAtLeast(0.1f)
                Quad(centrifugal, 0.1f, -0.4f, 2.2f)
            }

            SimulationScenario.RIGHT_TURN -> {
                currentState = VehicleDetectionState.ACTIVE
                val centrifugal = -(sin(simulationStep * 0.9f) * 0.75f).coerceAtLeast(0.1f)
                Quad(centrifugal, 0.1f, 0.4f, 2.2f)
            }

            SimulationScenario.CURVY_ROAD -> {
                currentState = VehicleDetectionState.ACTIVE
                val curve = sin(simulationStep * 1.1f) * 0.8f
                val braking = sin(simulationStep * 2.2f) * 0.35f
                Quad(curve, braking, curve * 0.5f, 2.4f)
            }
        }

        val sensitivity = settings.effectiveSensitivity
        _processedMotion.value = ProcessedMotion(
            state = currentState,
            targetDisplacementX = (targetX * sensitivity).coerceIn(-1.0f, 1.0f),
            targetDisplacementY = (targetY * sensitivity).coerceIn(-1.0f, 1.0f),
            rotationBias = rot,
            motionMagnitude = mag,
            confidence = 1.0f,
            isCueVisible = settings.isEnabled && !settings.isPaused,
            sampleRateHz = 60f,
            isSimulated = true
        )
    }

    private fun applyDeadband(value: Float, threshold: Float): Float {
        return if (abs(value) < threshold) 0f else value
    }

    private data class Quad(val x: Float, val y: Float, val rot: Float, val mag: Float)
}
