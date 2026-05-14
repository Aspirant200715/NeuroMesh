package com.neuromesh.crisis.presentation.ui.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.neuromesh.crisis.R
import kotlin.math.min

class ObservationOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var isAnalyzing = false
    private var confidence = 0f
    private var pulseRadius = 0f
    private var pulseGrowing = true

    private val scanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        alpha = 128
    }

    private val confidencePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.accent_green)
        style = Paint.Style.FILL
    }

    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    fun setAnalyzing(analyzing: Boolean) {
        isAnalyzing = analyzing
        if (analyzing) startAnimation() else invalidate()
    }

    fun setConfidence(conf: Float) {
        confidence = conf
        invalidate()
    }

    private fun startAnimation() {
        val runnable = object : Runnable {
            override fun run() {
                if (!isAnalyzing) return
                if (pulseGrowing) {
                    pulseRadius += 2f
                    if (pulseRadius > min(width, height) * 0.4f) pulseGrowing = false
                } else {
                    pulseRadius -= 2f
                    if (pulseRadius < 20f) pulseGrowing = true
                }
                invalidate()
                postDelayed(this, 16)
            }
        }
        post(runnable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isAnalyzing) return

        val cx = width / 2f
        val cy = height / 2f
        val cornerLen = 40f
        val margin = 60f

        canvas.drawLine(margin, margin, margin + cornerLen, margin, cornerPaint)
        canvas.drawLine(margin, margin, margin, margin + cornerLen, cornerPaint)
        canvas.drawLine(width - margin, margin, width - margin - cornerLen, margin, cornerPaint)
        canvas.drawLine(width - margin, margin, width - margin, margin + cornerLen, cornerPaint)
        canvas.drawLine(margin, height - margin, margin + cornerLen, height - margin, cornerPaint)
        canvas.drawLine(margin, height - margin, margin, height - margin - cornerLen, cornerPaint)
        canvas.drawLine(width - margin, height - margin, width - margin - cornerLen, height - margin, cornerPaint)
        canvas.drawLine(width - margin, height - margin, width - margin, height - margin - cornerLen, cornerPaint)

        if (pulseRadius > 0) {
            scanPaint.alpha = (255 * (1f - pulseRadius / (min(width, height) * 0.4f))).toInt()
            canvas.drawCircle(cx, cy, pulseRadius, scanPaint)
        }
    }
}
