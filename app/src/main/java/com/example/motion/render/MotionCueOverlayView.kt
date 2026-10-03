package com.example.motion.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import com.example.motion.model.MotionCueSettings
import com.example.motion.model.ProcessedMotion

/**
 * Custom hardware-accelerated View that draws peripheral motion cues over the entire screen.
 * Driven by Choreographer / postInvalidateOnAnimation only while active.
 */
class MotionCueOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val renderer = MotionCueRenderer(context)
    private var currentMotion = ProcessedMotion()
    private var currentSettings = MotionCueSettings()
    private var isRenderingActive = false

    init {
        // Completely transparent background
        setBackgroundColor(0x00000000)
        // Hardware acceleration enabled
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun updateMotion(motion: ProcessedMotion) {
        this.currentMotion = motion
        if (motion.isCueVisible && !isRenderingActive) {
            isRenderingActive = true
            postInvalidateOnAnimation()
        } else if (!motion.isCueVisible && isRenderingActive) {
            postInvalidateOnAnimation()
        }
    }

    fun updateSettings(settings: MotionCueSettings) {
        this.currentSettings = settings
        postInvalidateOnAnimation()
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val statusBars = insets.getInsets(WindowInsets.Type.statusBars())
            val navBars = insets.getInsets(WindowInsets.Type.navigationBars())
            val cutouts = insets.displayCutout

            val safeTop = Math.max(statusBars.top, cutouts?.safeInsetTop ?: 0)
            val safeBottom = Math.max(navBars.bottom, cutouts?.safeInsetBottom ?: 0)
            val safeLeft = Math.max(navBars.left, cutouts?.safeInsetLeft ?: 0)
            val safeRight = Math.max(navBars.right, cutouts?.safeInsetRight ?: 0)

            renderer.safeInsets = Rect(safeLeft, safeTop, safeRight, safeBottom)
        } else {
            @Suppress("DEPRECATION")
            renderer.safeInsets = Rect(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
        }
        postInvalidateOnAnimation()
        return super.onApplyWindowInsets(insets)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val needsNextFrame = renderer.render(
            canvas = canvas,
            width = width,
            height = height,
            motion = currentMotion,
            settings = currentSettings
        )

        isRenderingActive = needsNextFrame
        if (needsNextFrame) {
            postInvalidateOnAnimation()
        }
    }
}
