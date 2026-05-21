package com.mtoanng.datastream.data.network

import com.mtoanng.datastream.data.dto.AcknowledgeRequest
import com.mtoanng.datastream.data.dto.AcknowledgeResponse
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.dto.AlertRuleDto
import com.mtoanng.datastream.data.dto.AlertRulePatchRequest
import com.mtoanng.datastream.data.dto.AlertRuleRequest
import com.mtoanng.datastream.data.dto.ExportJobResponse
import com.mtoanng.datastream.data.dto.ExportRequest
import com.mtoanng.datastream.data.dto.ExportStatusResponse
import com.mtoanng.datastream.data.dto.FuelPriceDto
import com.mtoanng.datastream.data.dto.FuelPriceHistoryDto
import com.mtoanng.datastream.data.dto.GridLoadDto
import com.mtoanng.datastream.data.dto.GridLoadHistoryDto
import com.mtoanng.datastream.data.dto.HealthResponse
import com.mtoanng.datastream.data.dto.LoginRequest
import com.mtoanng.datastream.data.dto.LoginResponse
import com.mtoanng.datastream.data.dto.LogoutRequest
import com.mtoanng.datastream.data.dto.MessageResponse
import com.mtoanng.datastream.data.dto.PagedResponse
import com.mtoanng.datastream.data.dto.Pillar1SupplySecurityDto
import com.mtoanng.datastream.data.dto.Pillar2MarketResilienceDto
import com.mtoanng.datastream.data.dto.Pillar3GridReliabilityDto
import com.mtoanng.datastream.data.dto.Pillar4EnergyTransitionDto
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.dto.RefreshTokenRequest
import com.mtoanng.datastream.data.dto.RefreshTokenResponse
import com.mtoanng.datastream.data.dto.SecurityScoreDto
import com.mtoanng.datastream.data.dto.UserDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Retrofit client for VES-Monitor backend.
 *
 * Original 13 endpoints are kept unchanged.
 * New endpoints are grouped and clearly marked below.
 */
interface ApiService {

    // =========================================================
    // Auth (original)
    // =========================================================

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/auth/me")
    suspend fun me(): Response<UserDto>

    // =========================================================
    // Auth — NEW: Refresh & Logout
    // =========================================================

    /**
     * Exchange a still-valid token for a new one before it expires.
     * Call proactively when [TokenManager.expiresAtMillis()] is within
     * ~5 minutes of [System.currentTimeMillis()].
     */
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): Response<RefreshTokenResponse>

    /**
     * Invalidate the token server-side. Always call before clearing
     * 'TokenManager' so the server can blocklist the JWT.
     * Fire-and-forget: even on network failure, clear local session.
     */
    @POST("api/auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<MessageResponse>

    // =========================================================
    // Security score (original)
    // =========================================================

    @GET("api/security/score")
    suspend fun securityScore(): Response<SecurityScoreDto>

    /** Endpoint deprecated server-side: always returns []. Kept for parity. */
    @GET("api/security/cascade-risks")
    suspend fun cascadeRisks(): Response<List<Map<String, Any?>>>

    // =========================================================
    // Pillars (original)
    // =========================================================

    @GET("api/pillars/1/supply-security")
    suspend fun pillar1(): Response<List<Pillar1SupplySecurityDto>>

    @GET("api/pillars/2/market-resilience")
    suspend fun pillar2(): Response<List<Pillar2MarketResilienceDto>>

    @GET("api/pillars/3/grid-reliability")
    suspend fun pillar3(): Response<List<Pillar3GridReliabilityDto>>

    @GET("api/pillars/4/energy-transition")
    suspend fun pillar4(): Response<List<Pillar4EnergyTransitionDto>>

    // =========================================================
    // Alerts (original)
    // =========================================================

    @GET("api/alerts/active")
    suspend fun alerts(@Query("limit") limit: Int = 20): Response<List<AlertDto>>

    // =========================================================
    // Alerts — NEW: filter + paginate
    // =========================================================

    /**
     * Paginated, filterable alert list.
     *
     * @param page      0-based page index (default 0)
     * @param size      Page size (default 20, max 100)
     * @param severity  Filter: INFO | WARNING | CRITICAL — null means all
     * @param metricType Filter: FUEL_PRICE | GRID_LOAD | … — null means all
     * @param region    Filter by region code — null means all
     * @param from      ISO-8601 lower bound on 'alertTimestamp' (inclusive)
     * @param to        ISO-8601 upper bound on 'alertTimestamp' (inclusive)
     * @param sort      Field to sort by, e.g. "alertTimestamp,desc"
     */
    @GET("api/alerts")
    suspend fun alertsPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("severity") severity: String? = null,
        @Query("metricType") metricType: String? = null,
        @Query("region") region: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("sort") sort: String? = "alertTimestamp,desc",
    ): Response<PagedResponse<AlertDto>>

    // =========================================================
    // Alert Rules — NEW: CRUD
    // =========================================================

    /** List all alert rules (active + inactive). */
    @GET("api/alert-rules")
    suspend fun getAlertRules(): Response<List<AlertRuleDto>>

    /** Get a single alert rule by id. */
    @GET("api/alert-rules/{id}")
    suspend fun getAlertRule(@Path("id") id: Long): Response<AlertRuleDto>

    /** Create a new alert rule. Returns the persisted entity with server-assigned id. */
    @POST("api/alert-rules")
    suspend fun createAlertRule(@Body body: AlertRuleRequest): Response<AlertRuleDto>

    /** Full replacement of an existing alert rule. */
    @PUT("api/alert-rules/{id}")
    suspend fun updateAlertRule(
        @Path("id") id: Long,
        @Body body: AlertRuleRequest,
    ): Response<AlertRuleDto>

    /**
     * Partial update — send only the fields you want to change.
     * Typical use: toggle [AlertRulePatchRequest.enabled] without
     * re-sending the entire rule body.
     */
    @PATCH("api/alert-rules/{id}")
    suspend fun patchAlertRule(
        @Path("id") id: Long,
        @Body body: AlertRulePatchRequest,
    ): Response<AlertRuleDto>

    /** Permanently delete an alert rule. Returns 204 No Content. */
    @DELETE("api/alert-rules/{id}")
    suspend fun deleteAlertRule(@Path("id") id: Long): Response<Unit>

    // =========================================================
    // Recommendations (original)
    // =========================================================

    @GET("api/recommendations")
    suspend fun recommendations(@Query("limit") limit: Int = 50): Response<List<RecommendationDto>>

    @POST("api/recommendations/{id}/acknowledge")
    suspend fun acknowledge(
        @Path("id") id: Long,
        @Body body: AcknowledgeRequest,
    ): Response<AcknowledgeResponse>

    // =========================================================
    // Recommendations — NEW: filter + paginate
    // =========================================================

    /**
     * Paginated, filterable recommendations list.
     *
     * @param pillar    Filter by pillar number 1-4 — null means all
     * @param severity  Filter: INFO | WARNING | CRITICAL — null means all
     * @param status    Filter: PENDING | ACKNOWLEDGED | EXPIRED — null means all
     * @param sort      Field + direction, e.g. "suggestedAt,desc"
     */
    @GET("api/recommendations/paged")
    suspend fun recommendationsPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("pillar") pillar: Int? = null,
        @Query("severity") severity: String? = null,
        @Query("status") status: String? = null,
        @Query("sort") sort: String? = "suggestedAt,desc",
    ): Response<PagedResponse<RecommendationDto>>

    // =========================================================
    // Raw streams (original)
    // =========================================================

    @GET("api/fuel-prices/latest")
    suspend fun fuelPrices(
        @Query("fuel_type") fuelType: String? = null,
        @Query("limit") limit: Int = 20,
    ): Response<List<FuelPriceDto>>

    @GET("api/grid-load/latest")
    suspend fun gridLoad(): Response<List<GridLoadDto>>

    // =========================================================
    // History — NEW: time-range queries
    // =========================================================

    /**
     * Historical fuel prices for charting.
     *
     * @param fuelType  Filter: COAL | GAS | OIL | LNG | … — null means all
     * @param region    Region code filter — null means all
     * @param from      ISO-8601 start (inclusive), e.g. "2025-01-01T00:00:00Z"
     * @param to        ISO-8601 end (inclusive) — null defaults to now server-side
     * @param limit     Hard cap on rows returned (default 200)
     */
    @GET("api/fuel-prices/history")
    suspend fun fuelPriceHistory(
        @Query("fuel_type") fuelType: String? = null,
        @Query("region") region: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("limit") limit: Int = 200,
    ): Response<List<FuelPriceHistoryDto>>

    /**
     * Historical grid load for charting.
     *
     * @param regionCode Region code filter — null means all regions
     * @param from       ISO-8601 start (inclusive)
     * @param to         ISO-8601 end (inclusive) — null defaults to now server-side
     * @param limit      Hard cap on rows returned (default 200)
     */
    @GET("api/grid-load/history")
    suspend fun gridLoadHistory(
        @Query("region") regionCode: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("limit") limit: Int = 200,
    ): Response<List<GridLoadHistoryDto>>

    // =========================================================
    // Export — NEW: async CSV / PDF export
    // =========================================================

    /**
     * Kick off an async export job. Returns immediately with a 'jobId'.
     * Poll [exportStatus] until `status == READY`, then call [downloadExport].
     */
    @POST("api/export/request")
    suspend fun requestExport(@Body body: ExportRequest): Response<ExportJobResponse>

    /** Poll export job progress. */
    @GET("api/export/status/{jobId}")
    suspend fun exportStatus(@Path("jobId") jobId: String): Response<ExportStatusResponse>

    /**
     * Stream the finished file. Use [Streaming] so OkHttp does **not**
     * buffer the entire response body in memory before returning —
     * important for large CSV/PDF files.
     * Write [ResponseBody.byteStream()] to a file on a background thread.
     */
    @Streaming
    @GET("api/export/download/{jobId}")
    suspend fun downloadExport(@Path("jobId") jobId: String): Response<ResponseBody>

    // =========================================================
    // Health (original)
    // =========================================================

    @GET("api/health")
    suspend fun health(): Response<HealthResponse>
}
