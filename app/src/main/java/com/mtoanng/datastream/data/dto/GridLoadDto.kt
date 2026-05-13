package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * One row of `GET /api/grid-load/latest`.
 * Note: backend renames `isPeakHour` -> `peakHour` (Lombok) and adds `regionName` + `status`.
 */
@JsonClass(generateAdapter = true)
data class GridLoadDto(
    val regionCode: String,
    val regionName: String?,
    val loadMw: Double?,
    val capacityMw: Double?,
    val loadPct: Double?,
    val peakHour: Boolean,
    val status: String?,
    val eventTime: String?,
)
