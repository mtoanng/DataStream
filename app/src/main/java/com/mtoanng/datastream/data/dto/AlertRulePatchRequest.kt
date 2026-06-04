package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlertRulePatchRequest(
    val name: String? = null,
    val threshold: Double? = null,
    val severity: String? = null,
    val enabled: Boolean? = null,
)
