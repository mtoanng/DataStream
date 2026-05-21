package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    val accessToken: String,
    val expiresInMs: Long,
)
