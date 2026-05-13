package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * One row of `GET /api/pillars/3/grid-reliability` (Phase 7.1 NERC/IEEE Accessibility).
 */
@JsonClass(generateAdapter = true)
data class Pillar3GridReliabilityDto(
    val regionCode: String,
    val reserveMarginPct: Double?,
    val peakLoadFactor: Double?,
    val sheddingProb: Double?,
    val freqStabilityIdx: Double?,
    val pillar3Score: Double,
    val status: String,
    val computedAt: String,
)
