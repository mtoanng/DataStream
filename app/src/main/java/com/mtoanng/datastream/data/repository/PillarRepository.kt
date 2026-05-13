package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.Pillar1SupplySecurityDto
import com.mtoanng.datastream.data.dto.Pillar2MarketResilienceDto
import com.mtoanng.datastream.data.dto.Pillar3GridReliabilityDto
import com.mtoanng.datastream.data.dto.Pillar4EnergyTransitionDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult

class PillarRepository(private val api: ApiService) {
    suspend fun pillar1(): NetworkResult<List<Pillar1SupplySecurityDto>> = safeApiCall { api.pillar1() }
    suspend fun pillar2(): NetworkResult<List<Pillar2MarketResilienceDto>> = safeApiCall { api.pillar2() }
    suspend fun pillar3(): NetworkResult<List<Pillar3GridReliabilityDto>> = safeApiCall { api.pillar3() }
    suspend fun pillar4(): NetworkResult<List<Pillar4EnergyTransitionDto>> = safeApiCall { api.pillar4() }
}
