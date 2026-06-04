package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExportJobResponse(
    val jobId: String,
    val status: String,
    val format: String,
    val requestedAt: String,
    val estimatedReadyInMs: Long?,
)
