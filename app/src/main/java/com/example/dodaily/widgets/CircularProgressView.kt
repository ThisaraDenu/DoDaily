package com.example.dodaily.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var progress = 0f
    private var maxProgress = 100f
    private var strokeWidth = 8f
    private var progressColor = Color.parseColor("#5F8E5C")
    private var backgroundColor = Color.parseColor("#E0E0E0")

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
    }

    fun setProgress(progress: Int) {
        this.progress = progress.toFloat()
        invalidate()
    }

    fun setMaxProgress(max: Int) {
        this.maxProgress = max.toFloat()
        invalidate()
    }

    fun setProgressColor(color: Int) {
        this.progressColor = color
        invalidate()
    }

    fun setProgressBackgroundColor(color: Int) {
        this.backgroundColor = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        this.strokeWidth = width
        paint.strokeWidth = width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) - strokeWidth) / 2f
        
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        // Draw background circle
        paint.color = backgroundColor
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Draw progress arc
        if (progress > 0) {
            paint.color = progressColor
            val sweepAngle = (progress / maxProgress) * 360f
            canvas.drawArc(rectF, -90f, sweepAngle, false, paint)
        }
    }
}