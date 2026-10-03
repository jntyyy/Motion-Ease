package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.motion.data.SettingsRepository
import com.example.motion.engine.MotionEngine
import com.example.motion.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kinetic Cues", appName)
    }

    @Test
    fun `settings repository persists preferences correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SettingsRepository.getInstance(context)

        repo.updateSettings {
            it.copy(
                pattern = MotionPattern.DYNAMIC,
                dotSize = DotSize.LARGE,
                colorOption = DotColorOption.CYAN
            )
        }

        val updated = repo.settings.value
        assertEquals(MotionPattern.DYNAMIC, updated.pattern)
        assertEquals(DotSize.LARGE, updated.dotSize)
        assertEquals(DotColorOption.CYAN, updated.colorOption)
    }

    @Test
    fun `motion engine simulation generates active motion`() {
        val engine = MotionEngine()
        val settings = MotionCueSettings(isEnabled = true, isPaused = false)

        engine.setSimulationScenario(SimulationScenario.BRAKING)
        val raw = RawSensorReading()
        engine.processFrame(raw, settings)

        val motion = engine.processedMotion.value
        assertTrue("Cues should be visible during braking simulation", motion.isCueVisible)
        assertTrue("Displacement Y should reflect forward deceleration", motion.targetDisplacementY > 0f)
    }

    @Test
    fun `motion engine deadband suppresses small sensor noise`() {
        val engine = MotionEngine()
        val settings = MotionCueSettings(isEnabled = true, isPaused = false)

        // Micro-tremor sensor noise below 0.16 m/s^2
        val noisyReading = RawSensorReading(linearAccelX = 0.05f, linearAccelY = -0.04f)
        engine.processFrame(noisyReading, settings)

        val motion = engine.processedMotion.value
        assertEquals(0f, motion.targetDisplacementX, 0.01f)
        assertEquals(0f, motion.targetDisplacementY, 0.01f)
    }
}
