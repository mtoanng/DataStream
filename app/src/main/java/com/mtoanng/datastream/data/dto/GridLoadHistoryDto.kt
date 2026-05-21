package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GridLoadHistoryDto(
    val regionCode: String,
    val regionName: String?,
    val loadMw: Double?,
    val capacityMw: Double?,
    val loadPct: Double?,
    val peakHour: Boolean,
    val status: String?,
    val eventTime: String,
)
