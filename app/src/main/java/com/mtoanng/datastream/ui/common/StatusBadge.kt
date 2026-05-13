package com.mtoanng.datastream.ui.common

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.graphics.ColorUtils
import com.google.android.material.chip.Chip
import com.mtoanng.datastream.util.EnergySecurityHelper

/**
 * Material Chip pre-styled to render pillar/severity/status enums with the project's
 * palette (SECURE / ELEVATED / STRESSED / CRITICAL / INFO / WARNING).
 */
class StatusBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : Chip(context, attrs, defStyle) {

    init {
        isClickable = false
        isCheckable = false
    }

    fun setStatus(label: String?, status: String?) {
        text = label ?: status ?: "--"
        val color = EnergySecurityHelper.statusColor(context, status)
        chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            ColorUtils.setAlphaComponent(color, 60)
        )
        setTextColor(color)
        chipStrokeColor = android.content.res.ColorStateList.valueOf(color)
        chipStrokeWidth = 1f
        rippleColor = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
    }
}
