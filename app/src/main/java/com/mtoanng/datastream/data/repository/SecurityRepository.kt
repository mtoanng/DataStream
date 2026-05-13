package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.HealthResponse
import com.mtoanng.datastream.data.dto.SecurityScoreDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult

class SecurityRepository(private val api: ApiService) {
    suspend fun getScore(): NetworkResult<SecurityScoreDto> = safeApiCall { api.securityScore() }
    suspend fun getCascadeRisks(): NetworkResult<List<Map<String, Any?>>> = safeApiCall { api.cascadeRisks() }
    suspend fun getHealth(): NetworkResult<HealthResponse> = safeApiCall { api.health() }
}
