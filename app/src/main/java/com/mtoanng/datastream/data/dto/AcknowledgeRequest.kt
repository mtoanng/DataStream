package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * Body of `POST /api/recommendations/{id}/acknowledge`.
 * `status` defaults to ACKNOWLEDGED on the server when not provided; we still send it
 * explicitly for clarity. `note` is optional free text persisted server-side only.
 */
@JsonClass(generateAdapter = true)
data class AcknowledgeRequest(
    val status: String = "ACKNOWLEDGED",
    val note: String? = null,
)
