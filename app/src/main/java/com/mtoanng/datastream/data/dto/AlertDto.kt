package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * One row of `GET /api/alerts/active`.
 * `triggeredPrice` keeps the legacy "price" name but actually carries
 * loadPct / intensity / inventoryDays depending on `metricType`.
 */
@JsonClass(generateAdapter = true)
data class AlertDto(
    val id: Long,
    val ruleId: Long?,
    val ruleName: String?,
    val metricType: String,
    val fuelType: String?,
    val location: String?,
    val region: String?,
    val triggeredPrice: Double?,
    val threshold: Double?,
    val operator: String?,
    val severity: String,
    val message: String?,
    val eventTimestamp: String?,
    val alertTimestamp: String?,
    val ageSeconds: Int,
)
