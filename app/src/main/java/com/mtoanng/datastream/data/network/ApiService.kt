package com.mtoanng.datastream.data.network

import com.mtoanng.datastream.data.dto.AcknowledgeRequest
import com.mtoanng.datastream.data.dto.AcknowledgeResponse
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.dto.FuelPriceDto
import com.mtoanng.datastream.data.dto.GridLoadDto
import com.mtoanng.datastream.data.dto.HealthResponse
import com.mtoanng.datastream.data.dto.LoginRequest
import com.mtoanng.datastream.data.dto.LoginResponse
import com.mtoanng.datastream.data.dto.Pillar1SupplySecurityDto
import com.mtoanng.datastream.data.dto.Pillar2MarketResilienceDto
import com.mtoanng.datastream.data.dto.Pillar3GridReliabilityDto
import com.mtoanng.datastream.data.dto.Pillar4EnergyTransitionDto
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.dto.SecurityScoreDto
import com.mtoanng.datastream.data.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit client for VES-Monitor backend. Covers the 13 documented REST endpoints
 * (canonical paths only). Returns [Response] so repositories can map non-2xx codes
 * (401, 403, 5xx) into [NetworkResult.Error] messages.
 */
interface ApiService {

    // ---- Auth ----------------------------------------------------------------
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/auth/me")
    suspend fun me(): Response<UserDto>

    // ---- Security score ------------------------------------------------------
    @GET("api/security/score")
    suspend fun securityScore(): Response<SecurityScoreDto>

    /** Endpoint deprecated server-side: always returns []. Kept for parity. */
    @GET("api/security/cascade-risks")
    suspend fun cascadeRisks(): Response<List<Map<String, Any?>>>

    // ---- Pillars -------------------------------------------------------------
    @GET("api/pillars/1/supply-security")
    suspend fun pillar1(): Response<List<Pillar1SupplySecurityDto>>

    @GET("api/pillars/2/market-resilience")
    suspend fun pillar2(): Response<List<Pillar2MarketResilienceDto>>

    @GET("api/pillars/3/grid-reliability")
    suspend fun pillar3(): Response<List<Pillar3GridReliabilityDto>>

    @GET("api/pillars/4/energy-transition")
    suspend fun pillar4(): Response<List<Pillar4EnergyTransitionDto>>

    // ---- Alerts --------------------------------------------------------------
    @GET("api/alerts/active")
    suspend fun alerts(@Query("limit") limit: Int = 20): Response<List<AlertDto>>

    // ---- Recommendations -----------------------------------------------------
    @GET("api/recommendations")
    suspend fun recommendations(@Query("limit") limit: Int = 50): Response<List<RecommendationDto>>

    @POST("api/recommendations/{id}/acknowledge")
    suspend fun acknowledge(
        @Path("id") id: Long,
        @Body body: AcknowledgeRequest,
    ): Response<AcknowledgeResponse>

    // ---- Raw streams ---------------------------------------------------------
    @GET("api/fuel-prices/latest")
    suspend fun fuelPrices(
        @Query("fuel_type") fuelType: String? = null,
        @Query("limit") limit: Int = 20,
    ): Response<List<FuelPriceDto>>

    @GET("api/grid-load/latest")
    suspend fun gridLoad(): Response<List<GridLoadDto>>

    // ---- Health (public) -----------------------------------------------------
    @GET("api/health")
    suspend fun health(): Response<HealthResponse>
}
