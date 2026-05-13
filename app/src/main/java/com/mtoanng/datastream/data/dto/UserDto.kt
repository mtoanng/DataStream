package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val email: String?,
    val role: String,
    val enabled: Boolean,
)
