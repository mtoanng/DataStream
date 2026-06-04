package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlertRuleRequest(
    val name: String,
    val metricType: String,
    val fuelType: String? = null,
    val region: String? = null,
    val operator: String,
    val threshold: Double,
    val severity: String,
    val enabled: Boolean = true,
)
