package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * One row of `GET /api/pillars/4/energy-transition` (Phase 7.1 IPCC Acceptability).
 */
@JsonClass(generateAdapter = true)
data class Pillar4EnergyTransitionDto(
    val regionCode: String,
    val renewablePct: Double?,
    val co2Intensity: Double?,
    val curtailmentRate: Double?,
    val netzeroProgress: Double?,
    val pillar4Score: Double,
    val status: String,
    val computedAt: String,
)
