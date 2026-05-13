package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * `GET /api/health` returns either `200 {status:UP}` or `503 {status:DEGRADED}`.
 */
@JsonClass(generateAdapter = true)
data class HealthResponse(
    val service: String?,
    val timestamp: String?,
    val db: String?,
    val status: String,
)
