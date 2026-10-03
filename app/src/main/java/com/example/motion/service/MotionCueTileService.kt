package com.example.motion.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.example.MainActivity
import com.example.R
import com.example.motion.data.SettingsRepository

/**
 * Quick Settings Tile service that provides immediate, one-tap control to toggle
 * Vehicle Motion Cues on and off from the Android notification shade.
 */
class MotionCueTileService : TileService() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository.getInstance(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val hasPermission = Settings.canDrawOverlays(this)

        if (!hasPermission) {
            // Overlay permission missing - launch MainActivity to guide the user
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val currentSettings = settingsRepository.settings.value
        val shouldEnable = !currentSettings.isEnabled

        if (shouldEnable) {
            settingsRepository.updateSettings { it.copy(isEnabled = true, isPaused = false) }
            MotionCueService.startService(applicationContext)
        } else {
            settingsRepository.updateSettings { it.copy(isEnabled = false, isPaused = false) }
            MotionCueService.stopService(applicationContext)
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val hasPermission = Settings.canDrawOverlays(this)
        val settings = settingsRepository.settings.value

        tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_motion_cues)
        tile.label = getString(R.string.tile_motion_cues)

        when {
            !hasPermission -> {
                tile.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_state_permission_required)
                }
            }
            settings.isEnabled && !settings.isPaused -> {
                tile.state = Tile.STATE_ACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_state_active)
                }
            }
            else -> {
                tile.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.tile_state_inactive)
                }
            }
        }

        tile.updateTile()
    }

    companion object {
        private const val TAG = "MotionCueTileService"

        /**
         * Requests the system to re-query the tile state whenever settings change.
         */
        fun requestTileUpdate(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val component = ComponentName(context, MotionCueTileService::class.java)
                    requestListeningState(context, component)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to request tile update: ${e.message}")
            }
        }
    }
}
