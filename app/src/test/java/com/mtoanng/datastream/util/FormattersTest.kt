package com.mtoanng.datastream.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `percent formats null value as dashes`() {
        assertEquals("--", Formatters.percent(null))
    }

    @Test
    fun `percent multiplies fraction by 100`() {
        assertEquals("84.2%", Formatters.percent(0.842))
        assertEquals("0.0%", Formatters.percent(0.0))
    }

    @Test
    fun `percentRaw passes through already-percentage values`() {
        assertEquals("12.5%", Formatters.percentRaw(12.5))
        assertEquals("--", Formatters.percentRaw(null))
    }

    @Test
    fun `score handles missing values`() {
        assertEquals("--", Formatters.score(null))
        assertEquals("64.8", Formatters.score(64.83))
    }

    @Test
    fun `ago renders seconds-minutes-hours-days suffix`() {
        assertEquals("12s ago", Formatters.ago(12))
        assertEquals("5m ago", Formatters.ago(60 * 5))
        assertEquals("2h ago", Formatters.ago(60 * 60 * 2))
        assertEquals("3d ago", Formatters.ago(86_400 * 3))
    }

    @Test
    fun `ago supports future timestamps`() {
        assertEquals("30s from now", Formatters.ago(-30))
    }

    @Test
    fun `isoToLocal returns dashes when input is empty`() {
        assertEquals("--", Formatters.isoToLocal(null))
        assertEquals("--", Formatters.isoToLocal(""))
    }

    @Test
    fun `isoToLocal returns the original string when not parseable`() {
        // We don't assert the localized format because it depends on JVM TZ;
        // just confirm graceful fallback.
        val raw = "not-a-date"
        assertEquals(raw, Formatters.isoToLocal(raw))
    }
}
