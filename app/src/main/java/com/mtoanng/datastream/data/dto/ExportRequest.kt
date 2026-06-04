package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExportRequest(
    val dataType: String,
    val format: String,
    val from: String? = null,
    val to: String? = null,
    val filters: Map<String, String>? = null,
)
