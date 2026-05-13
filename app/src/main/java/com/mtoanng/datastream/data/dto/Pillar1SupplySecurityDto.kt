package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * One row of `GET /api/pillars/1/supply-security` (Phase 7.1 IEA/APERC Availability).
 * Sub-indicators:
 *  - idr           Import Dependency Ratio (0-1, lower better)
 *  - sfri          Strategic Fuel Reserve Index = stock days (target >=90)
 *  - hhiSupply     Herfindahl-Hirschman concentration (0-10000, lower better)
 *  - n1Resilience  Days of cover if largest single source disrupted
 */
@JsonClass(generateAdapter = true)
data class Pillar1SupplySecurityDto(
    val regionCode: String,
    val fuelType: String,
    val idr: Double?,
    val sfri: Double?,
    val hhiSupply: Double?,
    val n1Resilience: Double?,
    val pillar1Score: Double,
    val status: String,
    val computedAt: String,
)
