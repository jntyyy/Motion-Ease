package com.example.motion.data

import android.content.Context
import android.content.SharedPreferences
import com.example.motion.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<MotionCueSettings> = _settings.asStateFlow()

    private fun loadSettings(): MotionCueSettings {
        return MotionCueSettings(
            isEnabled = prefs.getBoolean(KEY_IS_ENABLED, false),
            isPaused = prefs.getBoolean(KEY_IS_PAUSED, false),
            mode = try {
                MotionMode.valueOf(prefs.getString(KEY_MODE, MotionMode.MANUAL.name) ?: MotionMode.MANUAL.name)
            } catch (e: Exception) {
                MotionMode.MANUAL
            },
            pattern = try {
                MotionPattern.valueOf(prefs.getString(KEY_PATTERN, MotionPattern.REGULAR.name) ?: MotionPattern.REGULAR.name)
            } catch (e: Exception) {
                MotionPattern.REGULAR
            },
            dotSize = try {
                DotSize.valueOf(prefs.getString(KEY_DOT_SIZE, DotSize.MEDIUM.name) ?: DotSize.MEDIUM.name)
            } catch (e: Exception) {
                DotSize.MEDIUM
            },
            dotCount = try {
                DotCount.valueOf(prefs.getString(KEY_DOT_COUNT, DotCount.NORMAL.name) ?: DotCount.NORMAL.name)
            } catch (e: Exception) {
                DotCount.NORMAL
            },
            opacity = prefs.getFloat(KEY_OPACITY, 0.85f),
            sensitivity = try {
                SensitivityLevel.valueOf(prefs.getString(KEY_SENSITIVITY, SensitivityLevel.MEDIUM.name) ?: SensitivityLevel.MEDIUM.name)
            } catch (e: Exception) {
                SensitivityLevel.MEDIUM
            },
            customSensitivity = prefs.getFloat(KEY_CUSTOM_SENSITIVITY, 1.0f),
            animationSpeed = try {
                AnimationSpeed.valueOf(prefs.getString(KEY_ANIM_SPEED, AnimationSpeed.NORMAL.name) ?: AnimationSpeed.NORMAL.name)
            } catch (e: Exception) {
                AnimationSpeed.NORMAL
            },
            colorOption = try {
                DotColorOption.valueOf(prefs.getString(KEY_COLOR_OPTION, DotColorOption.SYSTEM.name) ?: DotColorOption.SYSTEM.name)
            } catch (e: Exception) {
                DotColorOption.SYSTEM
            },
            edgeDistance = try {
                EdgeDistance.valueOf(prefs.getString(KEY_EDGE_DISTANCE, EdgeDistance.NORMAL.name) ?: EdgeDistance.NORMAL.name)
            } catch (e: Exception) {
                EdgeDistance.NORMAL
            },
            adaptiveContrast = prefs.getBoolean(KEY_ADAPTIVE_CONTRAST, true),
            autoHideWhenStopped = prefs.getBoolean(KEY_AUTO_HIDE, true),
            autoHideDelaySeconds = prefs.getInt(KEY_AUTO_HIDE_DELAY, 6),
            reduceMotion = prefs.getBoolean(KEY_REDUCE_MOTION, false),
            enableHapticFeedback = prefs.getBoolean(KEY_HAPTIC, true)
        )
    }

    fun updateSettings(transform: (MotionCueSettings) -> MotionCueSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        saveSettings(updated)
        com.example.motion.service.MotionCueTileService.requestTileUpdate(appContext)
    }

    private fun saveSettings(s: MotionCueSettings) {
        prefs.edit().apply {
            putBoolean(KEY_IS_ENABLED, s.isEnabled)
            putBoolean(KEY_IS_PAUSED, s.isPaused)
            putString(KEY_MODE, s.mode.name)
            putString(KEY_PATTERN, s.pattern.name)
            putString(KEY_DOT_SIZE, s.dotSize.name)
            putString(KEY_DOT_COUNT, s.dotCount.name)
            putFloat(KEY_OPACITY, s.opacity)
            putString(KEY_SENSITIVITY, s.sensitivity.name)
            putFloat(KEY_CUSTOM_SENSITIVITY, s.customSensitivity)
            putString(KEY_ANIM_SPEED, s.animationSpeed.name)
            putString(KEY_COLOR_OPTION, s.colorOption.name)
            putString(KEY_EDGE_DISTANCE, s.edgeDistance.name)
            putBoolean(KEY_ADAPTIVE_CONTRAST, s.adaptiveContrast)
            putBoolean(KEY_AUTO_HIDE, s.autoHideWhenStopped)
            putInt(KEY_AUTO_HIDE_DELAY, s.autoHideDelaySeconds)
            putBoolean(KEY_REDUCE_MOTION, s.reduceMotion)
            putBoolean(KEY_HAPTIC, s.enableHapticFeedback)
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "kinetic_cues_preferences"
        private const val KEY_IS_ENABLED = "pref_is_enabled"
        private const val KEY_IS_PAUSED = "pref_is_paused"
        private const val KEY_MODE = "pref_mode"
        private const val KEY_PATTERN = "pref_pattern"
        private const val KEY_DOT_SIZE = "pref_dot_size"
        private const val KEY_DOT_COUNT = "pref_dot_count"
        private const val KEY_OPACITY = "pref_opacity"
        private const val KEY_SENSITIVITY = "pref_sensitivity"
        private const val KEY_CUSTOM_SENSITIVITY = "pref_custom_sensitivity"
        private const val KEY_ANIM_SPEED = "pref_anim_speed"
        private const val KEY_COLOR_OPTION = "pref_color_option"
        private const val KEY_EDGE_DISTANCE = "pref_edge_distance"
        private const val KEY_ADAPTIVE_CONTRAST = "pref_adaptive_contrast"
        private const val KEY_AUTO_HIDE = "pref_auto_hide"
        private const val KEY_AUTO_HIDE_DELAY = "pref_auto_hide_delay"
        private const val KEY_REDUCE_MOTION = "pref_reduce_motion"
        private const val KEY_HAPTIC = "pref_haptic"

        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
        }
    }
}
