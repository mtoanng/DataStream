package com.mtoanng.datastream.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.mtoanng.datastream.R

/**
 * Maps backend status enum + numeric scores to colors/labels following the IEA/APERC
 * thresholds (SECURE >=80, ELEVATED 60-79, STRESSED 40-59, CRITICAL <40).
 */
object EnergySecurityHelper {

    fun statusFromScore(score: Double): String = when {
        score >= 80 -> "SECURE"
        score >= 60 -> "ELEVATED"
        score >= 40 -> "STRESSED"
        else        -> "CRITICAL"
    }

    fun statusColor(context: Context, status: String?): Int {
        val resId = when (status?.uppercase()) {
            "SECURE", "NORMAL", "UP" -> R.color.status_secure
            "ELEVATED"               -> R.color.status_elevated
            "STRESSED", "WARNING"    -> R.color.status_stressed
            "CRITICAL", "DEGRADED"   -> R.color.status_critical
            "INFO"                   -> R.color.status_info
            else                     -> R.color.status_unknown
        }
        return ContextCompat.getColor(context, resId)
    }

    fun severityColor(context: Context, severity: String?): Int = statusColor(context, severity)

    /** Localized status label key — strings.xml must define `status_secure` etc. */
    fun statusStringRes(status: String?): Int = when (status?.uppercase()) {
        "SECURE"    -> R.string.status_secure
        "ELEVATED"  -> R.string.status_elevated
        "STRESSED"  -> R.string.status_stressed
        "CRITICAL"  -> R.string.status_critical
        "INFO"      -> R.string.severity_info
        "WARNING"   -> R.string.severity_warning
        "NORMAL"    -> R.string.status_normal
        else        -> R.string.status_unknown
    }

    fun pillarColor(context: Context, pillar: Int): Int {
        val resId = when (pillar) {
            1 -> R.color.pillar_1
            2 -> R.color.pillar_2
            3 -> R.color.pillar_3
            4 -> R.color.pillar_4
            else -> R.color.pillar_unknown
        }
        return ContextCompat.getColor(context, resId)
    }
}
