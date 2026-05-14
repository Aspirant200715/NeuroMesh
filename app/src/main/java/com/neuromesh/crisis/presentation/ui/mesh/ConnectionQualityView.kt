package com.neuromesh.crisis.presentation.ui.mesh

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.neuromesh.crisis.R

class ConnectionQualityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var quality = 0
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setQuality(level: Int) {
        quality = level.coerceIn(0, 4)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val barWidth = width / 5f - 2f
        for (i in 0 until 4) {
            val barHeight = (height * (i + 1) / 4f)
            val left = i * (barWidth + 2f)
            val top = height - barHeight
            barPaint.color = if (i < quality) {
                context.getColor(R.color.accent_green)
            } else {
                context.getColor(R.color.mesh_isolated)
            }
            canvas.drawRect(left, top, left + barWidth, height.toFloat(), barPaint)
        }
    }
}
