package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlertRuleDto(
    val id: Long,
    val name: String,
    val metricType: String,
    val fuelType: String?,
    val region: String?,
    val operator: String,
    val threshold: Double,
    val severity: String,
    val enabled: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
)
