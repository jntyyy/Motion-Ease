package com.example.motion.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import com.example.motion.model.RawSensorReading
import com.example.motion.model.SensorHardwareStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Robust sensor data manager that accesses Android Sensor Framework,
 * provides hardware detection with graceful fallbacks, and performs
 * display-rotation coordinate transformation.
 */
class SensorDataManager(private val context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    // Hardware Sensors
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val gravitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val linearAccelSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Hardware status inspection
    val hardwareStatus: SensorHardwareStatus = SensorHardwareStatus(
        hasAccelerometer = accelerometer != null,
        hasGyroscope = gyroscope != null,
        hasGravity = gravitySensor != null,
        hasLinearAcceleration = linearAccelSensor != null,
        hasRotationVector = rotationVectorSensor != null,
        activeSensorCount = listOfNotNull(
            accelerometer, gyroscope, gravitySensor, linearAccelSensor, rotationVectorSensor
        ).size
    )

    private val _rawReadingFlow = MutableStateFlow(RawSensorReading())
    val rawReadingFlow: StateFlow<RawSensorReading> = _rawReadingFlow.asStateFlow()

    private var isListening = false

    // Fallback filters for devices lacking dedicated hardware sensors
    private val estimatedGravity = FloatArray(3) { 0f }
    private val estimatedLinearAccel = FloatArray(3) { 0f }
    private val alpha = 0.8f // Low-pass filter coefficient for gravity separation

    // Rate calculations
    private var lastTimestampNs: Long = 0L
    private var sampleCount: Int = 0
    private var lastFpsCalculationTime: Long = 0L
    private var currentSampleRateHz: Float = 0f

    // Current sensor values storage
    private var curAccelX = 0f
    private var curAccelY = 0f
    private var curAccelZ = 0f

    private var curGyroX = 0f
    private var curGyroY = 0f
    private var curGyroZ = 0f

    private var curLinearX = 0f
    private var curLinearY = 0f
    private var curLinearZ = 0f

    private var curGravX = 0f
    private var curGravY = 0f
    private var curGravZ = 0f

    private var curRoll = 0f
    private var curPitch = 0f
    private var curYaw = 0f

    /**
     * Start registering sensor listeners with battery-friendly configuration.
     */
    @Synchronized
    fun startListening(highPrecision: Boolean = true) {
        if (isListening || sensorManager == null) return

        val delay = if (highPrecision) {
            SensorManager.SENSOR_DELAY_GAME // ~50Hz, ideal for 60fps cues without overheating
        } else {
            SensorManager.SENSOR_DELAY_UI   // ~16-20Hz, low battery idle
        }

        accelerometer?.let { sensorManager.registerListener(this, it, delay) }
        gyroscope?.let { sensorManager.registerListener(this, it, delay) }
        gravitySensor?.let { sensorManager.registerListener(this, it, delay) }
        linearAccelSensor?.let { sensorManager.registerListener(this, it, delay) }
        rotationVectorSensor?.let { sensorManager.registerListener(this, it, delay) }

        isListening = true
    }

    /**
     * Stop listening to sensors completely to eliminate battery draw.
     */
    @Synchronized
    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = System.currentTimeMillis()
        sampleCount++
        if (now - lastFpsCalculationTime >= 1000L) {
            currentSampleRateHz = sampleCount.toFloat()
            sampleCount = 0
            lastFpsCalculationTime = now
        }

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                curAccelX = event.values[0]
                curAccelY = event.values[1]
                curAccelZ = event.values[2]

                // Fallback: If hardware linear acceleration is missing, compute it via filter
                if (linearAccelSensor == null) {
                    estimatedGravity[0] = alpha * estimatedGravity[0] + (1 - alpha) * curAccelX
                    estimatedGravity[1] = alpha * estimatedGravity[1] + (1 - alpha) * curAccelY
                    estimatedGravity[2] = alpha * estimatedGravity[2] + (1 - alpha) * curAccelZ

                    curLinearX = curAccelX - estimatedGravity[0]
                    curLinearY = curAccelY - estimatedGravity[1]
                    curLinearZ = curAccelZ - estimatedGravity[2]

                    if (gravitySensor == null) {
                        curGravX = estimatedGravity[0]
                        curGravY = estimatedGravity[1]
                        curGravZ = estimatedGravity[2]
                    }
                }
            }

            Sensor.TYPE_LINEAR_ACCELERATION -> {
                curLinearX = event.values[0]
                curLinearY = event.values[1]
                curLinearZ = event.values[2]
            }

            Sensor.TYPE_GRAVITY -> {
                curGravX = event.values[0]
                curGravY = event.values[1]
                curGravZ = event.values[2]
            }

            Sensor.TYPE_GYROSCOPE -> {
                curGyroX = event.values[0]
                curGyroY = event.values[1]
                curGyroZ = event.values[2]
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                curYaw = Math.toDegrees(orientation[0].toDouble()).toFloat()
                curPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                curRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()
            }
        }

        // Get display rotation to map device coordinates to screen coordinates
        val displayRotation = getDisplayRotation()

        // Remap coordinates based on display orientation
        val (screenLinearX, screenLinearY) = remapToScreen(curLinearX, curLinearY, displayRotation)
        val (screenAccelX, screenAccelY) = remapToScreen(curAccelX, curAccelY, displayRotation)
        val (screenGyroX, screenGyroY) = remapToScreen(curGyroX, curGyroY, displayRotation)

        _rawReadingFlow.value = RawSensorReading(
            timestampNs = event.timestamp,
            accelX = screenAccelX,
            accelY = screenAccelY,
            accelZ = curAccelZ,
            gyroX = screenGyroX,
            gyroY = screenGyroY,
            gyroZ = curGyroZ,
            linearAccelX = screenLinearX,
            linearAccelY = screenLinearY,
            linearAccelZ = curLinearZ,
            gravityX = curGravX,
            gravityY = curGravY,
            gravityZ = curGravZ,
            rotationRoll = curRoll,
            rotationPitch = curPitch,
            rotationYaw = curYaw,
            displayRotationDegrees = displayRotation
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action required for standard motion cues
    }

    private fun getDisplayRotation(): Int {
        return try {
            val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.rotation ?: Surface.ROTATION_0
            } else {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }
            when (rotation) {
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Remaps device X and Y axes to match the user's current screen orientation.
     */
    private fun remapToScreen(x: Float, y: Float, degrees: Int): Pair<Float, Float> {
        return when (degrees) {
            90 -> Pair(y, -x)      // Landscape standard
            180 -> Pair(-x, -y)    // Reverse portrait
            270 -> Pair(-y, x)     // Reverse landscape
            else -> Pair(x, y)     // Portrait standard
        }
    }

    fun getSampleRateHz(): Float = currentSampleRateHz
}
