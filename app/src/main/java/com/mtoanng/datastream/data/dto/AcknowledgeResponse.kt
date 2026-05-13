package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * Response of `POST /api/recommendations/{id}/acknowledge`.
 * Phase 7.6 shape: {id, newStatus, acknowledgedBy} — no `acknowledgedAt`, no `note`.
 */
@JsonClass(generateAdapter = true)
data class AcknowledgeResponse(
    val id: Long,
    val newStatus: String,
    val acknowledgedBy: Long,
)
