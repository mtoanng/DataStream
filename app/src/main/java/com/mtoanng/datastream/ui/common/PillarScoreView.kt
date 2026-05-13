package com.mtoanng.datastream.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mtoanng.datastream.R
import com.mtoanng.datastream.util.EnergySecurityHelper
import com.mtoanng.datastream.util.dpToPx

/**
 * Circular gauge: draws a thick arc from 270deg sweeping clockwise based on score
 * (0..100) and prints the score in the centre with an optional status label below.
 */
class PillarScoreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private var score: Float = 0f
    private var status: String = "--"
    private var arcColor: Int = ContextCompat.getColor(context, R.color.status_unknown)

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 18.dpToPx().toFloat()
        strokeCap = Paint.Cap.ROUND
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 18.dpToPx().toFloat()
        strokeCap = Paint.Cap.ROUND
    }

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = ContextCompat.getColor(context, R.color.text_primary)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, R.color.text_secondary)
    }

    private val arcRect = RectF()

    fun setScore(score: Double, status: String?) {
        this.score = score.toFloat().coerceIn(0f, 100f)
        this.status = status ?: EnergySecurityHelper.statusFromScore(score)
        this.arcColor = EnergySecurityHelper.statusColor(context, this.status)
        arcPaint.color = this.arcColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val side = minOf(width, height).toFloat()
        val pad = 18.dpToPx().toFloat()
        arcRect.set(pad, pad, side - pad, side - pad)

        // Track behind the arc
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)

        // Score arc
        val sweep = 360f * (score / 100f)
        canvas.drawArc(arcRect, 270f, sweep, false, arcPaint)

        // Score label centre
        scorePaint.textSize = side * 0.28f
        val centerX = side / 2f
        val baseline = side / 2f - (scorePaint.descent() + scorePaint.ascent()) / 2f
        canvas.drawText(String.format("%.0f", score), centerX, baseline, scorePaint)

        // Status text below
        labelPaint.textSize = side * 0.08f
        canvas.drawText(status, centerX, baseline + scorePaint.textSize * 0.7f, labelPaint)
    }
}
