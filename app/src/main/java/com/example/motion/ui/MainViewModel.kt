package com.example.motion.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.motion.data.SettingsRepository
import com.example.motion.engine.MotionEngine
import com.example.motion.model.*
import com.example.motion.sensor.SensorDataManager
import com.example.motion.service.MotionCueService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    private val settingsRepository = SettingsRepository.getInstance(context)

    val settings: StateFlow<MotionCueSettings> = settingsRepository.settings

    val sensorManager = SensorDataManager(context)
    val motionEngine = MotionEngine()

    val rawReading: StateFlow<RawSensorReading> = sensorManager.rawReadingFlow
    val processedMotion: StateFlow<ProcessedMotion> = motionEngine.processedMotion

    private val _hasOverlayPermission = MutableStateFlow(checkOverlayPermission())
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _currentSimulationScenario = MutableStateFlow<SimulationScenario?>(null)
    val currentSimulationScenario: StateFlow<SimulationScenario?> = _currentSimulationScenario.asStateFlow()

    val hardwareStatus: SensorHardwareStatus = sensorManager.hardwareStatus

    init {
        // Start sensor listening for in-app preview
        sensorManager.startListening(highPrecision = true)

        // Process incoming sensor events for live preview & UI feedback
        viewModelScope.launch(Dispatchers.Default) {
            sensorManager.rawReadingFlow.collect { raw ->
                motionEngine.processFrame(raw, settings.value)
            }
        }

        // Re-process when settings change
        viewModelScope.launch {
            settings.collectLatest { s ->
                motionEngine.processFrame(sensorManager.rawReadingFlow.value, s)
            }
        }
    }

    fun refreshPermissions() {
        _hasOverlayPermission.value = checkOverlayPermission()
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun toggleMotionCues() {
        refreshPermissions()
        val current = settings.value

        if (!current.isEnabled) {
            settingsRepository.updateSettings { it.copy(isEnabled = true, isPaused = false) }
            if (hasOverlayPermission.value) {
                MotionCueService.startService(context)
            }
        } else {
            settingsRepository.updateSettings { it.copy(isEnabled = false, isPaused = false) }
            MotionCueService.stopService(context)
        }
    }

    fun togglePause() {
        val current = settings.value
        val newPaused = !current.isPaused
        settingsRepository.updateSettings { it.copy(isPaused = newPaused) }
    }

    fun updateSettings(transform: (MotionCueSettings) -> MotionCueSettings) {
        settingsRepository.updateSettings(transform)
    }

    fun setSimulationScenario(scenario: SimulationScenario?) {
        _currentSimulationScenario.value = scenario
        motionEngine.setSimulationScenario(scenario)
        MotionCueService.setSimulation(context, scenario)
    }

    override fun onCleared() {
        sensorManager.stopListening()
        super.onCleared()
    }
}
