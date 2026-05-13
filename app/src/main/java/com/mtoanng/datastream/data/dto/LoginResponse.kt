package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

/**
 * Body of `POST /api/auth/login`.
 * `expiresInMs` is **milliseconds** (8h = 28 800 000), not seconds.
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val accessToken: String,
    val expiresInMs: Long,
    val user: UserDto,
)
