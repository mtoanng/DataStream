package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult

class AlertRepository(private val api: ApiService) {
    suspend fun getActive(limit: Int = 50): NetworkResult<List<AlertDto>> =
        safeApiCall { api.alerts(limit) }
}
