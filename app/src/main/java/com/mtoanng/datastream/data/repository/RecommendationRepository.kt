package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.AcknowledgeRequest
import com.mtoanng.datastream.data.dto.AcknowledgeResponse
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult

class RecommendationRepository(private val api: ApiService) {

    suspend fun list(limit: Int = 100): NetworkResult<List<RecommendationDto>> =
        safeApiCall { api.recommendations(limit) }

    suspend fun acknowledge(
        id: Long,
        status: String = "ACKNOWLEDGED",
        note: String? = null,
    ): NetworkResult<AcknowledgeResponse> = safeApiCall {
        api.acknowledge(id, AcknowledgeRequest(status = status, note = note))
    }
}
