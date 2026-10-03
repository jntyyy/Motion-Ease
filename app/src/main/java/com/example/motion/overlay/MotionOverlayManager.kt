package com.example.motion.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.example.motion.model.MotionCueSettings
import com.example.motion.model.ProcessedMotion
import com.example.motion.render.MotionCueOverlayView

/**
 * Manages the Android System Overlay Window (`TYPE_APPLICATION_OVERLAY`).
 * Ensures strict touch-through behavior, non-interception of gestures,
 * and zero interference with underlying applications.
 */
class MotionOverlayManager(private val context: Context) {

    private val windowManager: WindowManager? =
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    private var overlayView: MotionCueOverlayView? = null
    var isOverlayAttached: Boolean = false
        private set

    /**
     * Check if SYSTEM_ALERT_WINDOW permission is granted.
     */
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Attach the overlay window to WindowManager with touch-through flags.
     */
    @Synchronized
    fun showOverlay(settings: MotionCueSettings) {
        if (!hasOverlayPermission() || windowManager == null) {
            Log.w(TAG, "Cannot show overlay: permission missing or WindowManager unavailable")
            return
        }

        if (isOverlayAttached && overlayView != null) {
            overlayView?.updateSettings(settings)
            return
        }

        try {
            val view = MotionCueOverlayView(context).apply {
                updateSettings(settings)
            }

            val layoutParams = WindowManager.LayoutParams().apply {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                format = PixelFormat.TRANSLUCENT

                // Critical touch-through and non-obtrusive flags:
                // FLAG_NOT_FOCUSABLE: Never steals keyboard or input focus
                // FLAG_NOT_TOUCHABLE: Completely ignores and passes through all touches to apps below
                // FLAG_LAYOUT_IN_SCREEN: Extends within screen bounds
                // FLAG_LAYOUT_NO_LIMITS: Allows edge-to-edge drawing past system bars
                // FLAG_HARDWARE_ACCELERATED: 60fps smooth rendering
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP or Gravity.START
                title = "VehicleMotionCuesOverlay"
            }

            windowManager.addView(view, layoutParams)
            overlayView = view
            isOverlayAttached = true
            Log.i(TAG, "Overlay successfully attached to WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach overlay window: ${e.message}", e)
            isOverlayAttached = false
            overlayView = null
        }
    }

    /**
     * Remove the overlay window and free resources.
     */
    @Synchronized
    fun hideOverlay() {
        if (!isOverlayAttached || overlayView == null || windowManager == null) return

        try {
            windowManager.removeViewImmediate(overlayView)
            Log.i(TAG, "Overlay successfully detached from WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay view: ${e.message}", e)
        } finally {
            overlayView = null
            isOverlayAttached = false
        }
    }

    /**
     * Dispatch motion state update to the overlay canvas.
     */
    fun updateMotion(motion: ProcessedMotion) {
        overlayView?.updateMotion(motion)
    }

    /**
     * Dispatch settings update to the overlay canvas.
     */
    fun updateSettings(settings: MotionCueSettings) {
        overlayView?.updateSettings(settings)
    }

    companion object {
        private const val TAG = "MotionOverlayManager"
    }
}
