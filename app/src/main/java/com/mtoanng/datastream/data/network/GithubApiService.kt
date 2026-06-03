package com.mtoanng.datastream.data.network

import com.mtoanng.datastream.data.dto.GithubChatRequest
import com.mtoanng.datastream.data.dto.GithubChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Dedicated interface for GitHub Models API to avoid polluting the main ApiService.
 */
interface GithubApiService {
    @POST("chat/completions")
    suspend fun githubChat(
        @Header("Authorization") auth: String,
        @Body body: GithubChatRequest
    ): Response<GithubChatResponse>
}
