package com.example.motion.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
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
        val currentSettings = settingsRepository.settings.value
        val isCurrentlyActive = currentSettings.isEnabled && !currentSettings.isPaused

        if (!isCurrentlyActive) {
            // Enable motion cues
            settingsRepository.updateSettings { it.copy(isEnabled = true, isPaused = false) }

            if (Settings.canDrawOverlays(this)) {
                MotionCueService.startService(applicationContext)
            } else {
                // If overlay permission is not granted, directly launch permission settings
                val permIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        permIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(permIntent)
                }
            }
        } else {
            // Disable / cancel motion cues
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
