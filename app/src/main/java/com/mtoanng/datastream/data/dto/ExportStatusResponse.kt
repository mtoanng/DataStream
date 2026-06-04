package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExportStatusResponse(
    val jobId: String,
    val status: String,
    val format: String,
    val downloadUrl: String?,
    val fileSizeBytes: Long?,
    val readyAt: String?,
    val errorMessage: String?,
)
