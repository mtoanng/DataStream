package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * Body of `GET /api/security/score` (Energy Security Index, IEA weights).
 * Status enum: SECURE (>=80), ELEVATED (60-79), STRESSED (40-59), CRITICAL (<40).
 */
@JsonClass(generateAdapter = true)
data class SecurityScoreDto(
    val pillar1Score: Double,
    val pillar2Score: Double,
    val pillar3Score: Double,
    val pillar4Score: Double,
    val overallScore: Double,
    val status: String,
    val computedAt: String,
)
