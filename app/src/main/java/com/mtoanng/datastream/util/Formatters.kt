package com.mtoanng.datastream.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Pure formatters — no Android dependencies, fully unit-testable.
 */
object Formatters {

    private val percentFormat = DecimalFormat("#0.0")
    private val scoreFormat = DecimalFormat("#0.0")
    private val numberFormat = DecimalFormat("#,##0.##")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    /** Display a 0..1 ratio as `12.3%`, or `--` when value missing. */
    fun percent(value: Double?): String =
        if (value == null) "--" else "${percentFormat.format(value * 100.0)}%"

    /** Display an already-percentage value (0..100). */
    fun percentRaw(value: Double?): String =
        if (value == null) "--" else "${percentFormat.format(value)}%"

    fun score(value: Double?): String =
        if (value == null) "--" else scoreFormat.format(value)

    fun number(value: Double?): String =
        if (value == null) "--" else numberFormat.format(value)

    fun number(value: Long?): String =
        if (value == null) "--" else numberFormat.format(value)

    /** Convert "2026-05-13T10:30:00Z" -> "2026-05-13 17:30" (local zone). */
    fun isoToLocal(iso: String?): String {
        if (iso.isNullOrBlank()) return "--"
        return try {
            val instant = OffsetDateTime.parse(iso).toInstant()
            dateFormat.format(Date.from(instant))
        } catch (_: DateTimeParseException) {
            iso
        } catch (_: Exception) {
            iso
        }
    }

    /**
     * Render seconds as "12s ago", "5m ago", "2h ago", "3d ago". Negative -> future.
     * Algorithm only — no Android `Locale` lookups, so tests stay deterministic.
     */
    fun ago(seconds: Int): String {
        val s = abs(seconds)
        val suffix = if (seconds < 0) "from now" else "ago"
        return when {
            s < 60      -> "${s}s $suffix"
            s < 3600    -> "${s / 60}m $suffix"
            s < 86_400  -> "${s / 3600}h $suffix"
            else        -> "${s / 86_400}d $suffix"
        }
    }
}
