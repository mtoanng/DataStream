package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * One row of `GET /api/recommendations` (PENDING & not expired only).
 * `suggestedData` is JSONB pass-through — kept as Map so we can render raw key/value.
 */
@JsonClass(generateAdapter = true)
data class RecommendationDto(
    val id: Long,
    val pillar: Int,
    val actionType: String,
    val severity: String,
    val title: String,
    val message: String?,
    val suggestedData: Map<String, Any?>?,
    val suggestedAt: String?,
    val ageSeconds: Int,
    val expiresAt: String?,
    val expired: Boolean,
)
