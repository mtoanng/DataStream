package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SocialLoginRequest(
    val provider: String,
    val idToken: String,
)
