package com.example.motion.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.example.motion.model.*
import kotlin.math.sin

/**
 * High-performance, pure Canvas renderer for Vehicle Motion Cues.
 * Shared between the system overlay window and the in-app interactive preview.
 */
class MotionCueRenderer(private val context: Context) {

    // Paints
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val contrastHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(140, 0, 0, 0) // Soft dark halo for contrast against white backgrounds
    }

    private val outerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Physical screen density conversion
    private val density: Float = context.resources.displayMetrics.density

    // Smooth state interpolation
    private var currentDispX = 0f
    private var currentDispY = 0f
    private var currentAlpha = 0f
    private var wavePhase = 0f
    private var lastRenderTimeNs = 0L

    // Insets / Safe bounds
    var safeInsets = Rect(0, 0, 0, 0)

    /**
     * Renders a frame of peripheral motion cue dots onto the provided canvas.
     * Returns true if animation is still ongoing (needs subsequent frame invalidation).
     */
    fun render(
        canvas: Canvas,
        width: Int,
        height: Int,
        motion: ProcessedMotion,
        settings: MotionCueSettings
    ): Boolean {
        val nowNs = System.nanoTime()
        val dt = if (lastRenderTimeNs == 0L) 0.016f else ((nowNs - lastRenderTimeNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastRenderTimeNs = nowNs

        // Calculate target alpha
        val targetAlpha = if (motion.isCueVisible) settings.opacity else 0f

        // Smooth alpha transition (fade in/out)
        val alphaSpeed = if (targetAlpha > currentAlpha) 6.0f else 3.5f
        currentAlpha += (targetAlpha - currentAlpha) * (alphaSpeed * dt).coerceAtMost(1f)

        // If completely invisible, no drawing needed
        if (currentAlpha <= 0.005f) {
            return false
        }

        // Smooth displacement interpolation (lerp)
        val lerpSpeed = when (settings.animationSpeed) {
            AnimationSpeed.SLOW -> 6.0f
            AnimationSpeed.NORMAL -> 10.0f
            AnimationSpeed.FAST -> 15.0f
        } * (if (settings.reduceMotion) 0.6f else 1.0f)

        currentDispX += (motion.targetDisplacementX - currentDispX) * (lerpSpeed * dt).coerceAtMost(1f)
        currentDispY += (motion.targetDisplacementY - currentDispY) * (lerpSpeed * dt).coerceAtMost(1f)

        // Update traveling wave for dynamic mode
        if (settings.pattern == MotionPattern.DYNAMIC) {
            wavePhase += dt * 3.5f
        }

        // Determine base visual parameters
        val dotRadiusPx = settings.dotSize.radiusDp * density * (if (settings.reduceMotion) 0.85f else 1.0f)
        val edgeDistancePx = settings.edgeDistance.marginDp * density
        val maxTravelPx = 36.0f * density * (if (settings.reduceMotion) 0.5f else 1.0f)

        // Color setup
        val baseColorLong = settings.colorOption.colorHex
        val colorInt = baseColorLong.toInt()
        val alpha255 = (currentAlpha * 255f).toInt().coerceIn(0, 255)

        val red = Color.red(colorInt)
        val green = Color.green(colorInt)
        val blue = Color.blue(colorInt)

        dotPaint.color = Color.argb(alpha255, red, green, blue)
        contrastHaloPaint.alpha = (currentAlpha * 140f).toInt().coerceIn(0, 255)
        outerGlowPaint.color = Color.argb((alpha255 * 0.35f).toInt(), red, green, blue)

        // Safe boundaries with insets avoidance
        val left = safeInsets.left + edgeDistancePx
        val right = width - safeInsets.right - edgeDistancePx
        val top = safeInsets.top + edgeDistancePx
        val bottom = height - safeInsets.bottom - edgeDistancePx

        val usableWidth = right - left
        val usableHeight = bottom - top

        if (usableWidth <= 0 || usableHeight <= 0) return true

        val count = settings.dotCount.countPerSide

        // 1. Render Left Edge Dots
        drawEdgeDots(
            canvas = canvas,
            isVertical = true,
            fixedCoord = left,
            startCoord = top + usableHeight * 0.12f,
            endCoord = bottom - usableHeight * 0.12f,
            count = count,
            displacementNormal = currentDispX * maxTravelPx,
            displacementParallel = currentDispY * maxTravelPx * 0.5f,
            radius = dotRadiusPx,
            settings = settings
        )

        // 2. Render Right Edge Dots
        drawEdgeDots(
            canvas = canvas,
            isVertical = true,
            fixedCoord = right,
            startCoord = top + usableHeight * 0.12f,
            endCoord = bottom - usableHeight * 0.12f,
            count = count,
            displacementNormal = currentDispX * maxTravelPx,
            displacementParallel = currentDispY * maxTravelPx * 0.5f,
            radius = dotRadiusPx,
            settings = settings
        )

        // 3. Render Top Edge Dots
        drawEdgeDots(
            canvas = canvas,
            isVertical = false,
            fixedCoord = top,
            startCoord = left + usableWidth * 0.15f,
            endCoord = right - usableWidth * 0.15f,
            count = count,
            displacementNormal = currentDispY * maxTravelPx,
            displacementParallel = currentDispX * maxTravelPx * 0.5f,
            radius = dotRadiusPx,
            settings = settings
        )

        // 4. Render Bottom Edge Dots
        drawEdgeDots(
            canvas = canvas,
            isVertical = false,
            fixedCoord = bottom,
            startCoord = left + usableWidth * 0.15f,
            endCoord = right - usableWidth * 0.15f,
            count = count,
            displacementNormal = currentDispY * maxTravelPx,
            displacementParallel = currentDispX * maxTravelPx * 0.5f,
            radius = dotRadiusPx,
            settings = settings
        )

        // Return true if still in transition or moving
        val isStillMoving = Math.abs(motion.targetDisplacementX - currentDispX) > 0.005f ||
                Math.abs(motion.targetDisplacementY - currentDispY) > 0.005f ||
                Math.abs(targetAlpha - currentAlpha) > 0.005f ||
                (motion.isCueVisible && settings.pattern == MotionPattern.DYNAMIC)

        return isStillMoving || motion.isCueVisible
    }

    private fun drawEdgeDots(
        canvas: Canvas,
        isVertical: Boolean,
        fixedCoord: Float,
        startCoord: Float,
        endCoord: Float,
        count: Int,
        displacementNormal: Float,
        displacementParallel: Float,
        radius: Float,
        settings: MotionCueSettings
    ) {
        val span = endCoord - startCoord
        val step = if (count > 1) span / (count - 1) else span

        for (i in 0 until count) {
            val baseCoord = startCoord + i * step

            // In dynamic mode, add slight traveling sine wave offset to emphasize kinetic stream
            val dynamicWave = if (settings.pattern == MotionPattern.DYNAMIC && !settings.reduceMotion) {
                sin(wavePhase + i * 0.6f) * (4.0f * density)
            } else {
                0f
            }

            val normalShift = displacementNormal + dynamicWave
            val parallelShift = displacementParallel

            val cx = if (isVertical) fixedCoord + normalShift else baseCoord + parallelShift
            val cy = if (isVertical) baseCoord + parallelShift else fixedCoord + normalShift

            // Dual-layer rendering: adaptive contrast halo behind the dot
            if (settings.adaptiveContrast) {
                canvas.drawCircle(cx, cy, radius * 1.55f, contrastHaloPaint)
                canvas.drawCircle(cx, cy, radius * 1.25f, outerGlowPaint)
            }

            // Core dot
            canvas.drawCircle(cx, cy, radius, dotPaint)
        }
    }
}
