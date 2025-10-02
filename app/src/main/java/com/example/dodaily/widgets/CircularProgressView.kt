package com.example.dodaily.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Simple circular progress view for displaying daily goals progress
 */
class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var progress = 0f
    private var maxProgress = 100f
    private var progressText = "25%"
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // Background circle
        backgroundPaint.color = Color.WHITE
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = 8f
        
        // Progress arc
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.strokeCap = Paint.Cap.ROUND
        
        // Text
        textPaint.color = Color.WHITE
        textPaint.textSize = 16f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }
    
    fun setProgress(progress: Float, text: String) {
        this.progress = progress.coerceIn(0f, maxProgress)
        this.progressText = text
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) - 16f
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)
        
        // Draw progress arc
        val sweepAngle = (progress / maxProgress) * 360f
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        canvas.drawArc(rect, -90f, sweepAngle, false, paint)
        
        // Draw progress text
        val textY = centerY + (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(progressText, centerX, textY, textPaint)
    }
}
