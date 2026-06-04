package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubChatRequest(
    val messages: List<GithubMessage>,
    val model: String = "gpt-4o",
    val temperature: Float = 0.7f,
    val max_tokens: Int = 1024
)

@JsonClass(generateAdapter = true)
data class GithubMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class GithubChatResponse(
    val choices: List<GithubChoice>
)

@JsonClass(generateAdapter = true)
data class GithubChoice(
    val message: GithubMessage
)
