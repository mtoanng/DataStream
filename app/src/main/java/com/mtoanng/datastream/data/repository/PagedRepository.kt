package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.dto.PagedResponse
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult

class PagedRepository(private val api: ApiService) {

    /**
     * Fetch a page of alerts with optional filters.
     *
     * @param page       0-based page index
     * @param size       Page size (max 100)
     * @param severity   "INFO" | "WARNING" | "CRITICAL" — null = all
     * @param metricType "FUEL_PRICE" | "GRID_LOAD" | … — null = all
     * @param region     Region code — null = all
     * @param from       ISO-8601 lower bound on alertTimestamp
     * @param to         ISO-8601 upper bound on alertTimestamp
     * @param sort       Sort expression, e.g. "alertTimestamp,desc"
     */
    suspend fun getAlertsPaged(
        page: Int = 0,
        size: Int = 20,
        severity: String? = null,
        metricType: String? = null,
        region: String? = null,
        from: String? = null,
        to: String? = null,
        sort: String? = "alertTimestamp,desc",
    ): NetworkResult<PagedResponse<AlertDto>> =
        safeApiCall { api.alertsPaged(page, size, severity, metricType, region, from, to, sort) }

    /**
     * Fetch a page of recommendations with optional filters.
     *
     * @param page     0-based page index
     * @param size     Page size (max 100)
     * @param pillar   1–4 — null = all
     * @param severity "INFO" | "WARNING" | "CRITICAL" — null = all
     * @param status   "PENDING" | "ACKNOWLEDGED" | "EXPIRED" — null = all
     * @param sort     Sort expression, e.g. "suggestedAt,desc"
     */
    suspend fun getRecommendationsPaged(
        page: Int = 0,
        size: Int = 20,
        pillar: Int? = null,
        severity: String? = null,
        status: String? = null,
        sort: String? = "suggestedAt,desc",
    ): NetworkResult<PagedResponse<RecommendationDto>> =
        safeApiCall { api.recommendationsPaged(page, size, pillar, severity, status, sort) }
}


