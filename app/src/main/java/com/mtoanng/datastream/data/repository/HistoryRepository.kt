package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.FuelPriceHistoryDto
import com.mtoanng.datastream.data.dto.GridLoadHistoryDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult

class HistoryRepository(private val api: ApiService) {

    /**
     * Retrieve historical fuel price rows for charting.
     *
     * @param fuelType  e.g. "COAL", "GAS", "OIL", "LNG" — null = all
     * @param region    Region code filter — null = all
     * @param from      ISO-8601 lower bound (inclusive)
     * @param to        ISO-8601 upper bound (inclusive) — null = server default (now)
     * @param limit     Max rows (default 200, hard-capped server-side at 1000)
     */
    suspend fun getFuelPriceHistory(
        fuelType: String? = null,
        region: String? = null,
        from: String? = null,
        to: String? = null,
        limit: Int = 200,
    ): NetworkResult<List<FuelPriceHistoryDto>> =
        safeApiCall { api.fuelPriceHistory(fuelType, region, from, to, limit) }

    /**
     * Retrieve historical grid load rows for charting.
     *
     * @param regionCode  Region code filter — null = all
     * @param from        ISO-8601 lower bound (inclusive)
     * @param to          ISO-8601 upper bound (inclusive) — null = server default
     * @param limit       Max rows (default 200)
     */
    suspend fun getGridLoadHistory(
        regionCode: String? = null,
        from: String? = null,
        to: String? = null,
        limit: Int = 200,
    ): NetworkResult<List<GridLoadHistoryDto>> =
        safeApiCall { api.gridLoadHistory(regionCode, from, to, limit) }
}

