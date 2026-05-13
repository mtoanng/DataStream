package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * One row of `GET /api/pillars/2/market-resilience` (Phase 7.1 IEA/IMF Affordability).
 */
@JsonClass(generateAdapter = true)
data class Pillar2MarketResilienceDto(
    val fuelType: String,
    val sigma30d: Double?,
    val priceGapPct: Double?,
    val betaCrude: Double?,
    val affordabilityIdx: Double?,
    val pillar2Score: Double,
    val status: String,
    val computedAt: String,
)
