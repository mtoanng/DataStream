package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * Backend Spring error envelope returned for all 4xx/5xx responses.
 */
@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val timestamp: String?,
    val status: Int,
    val error: String?,
    val message: String?,
    val path: String?,
)
