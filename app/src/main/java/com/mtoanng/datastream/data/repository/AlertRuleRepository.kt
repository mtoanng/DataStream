package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.AlertRuleDto
import com.mtoanng.datastream.data.dto.AlertRulePatchRequest
import com.mtoanng.datastream.data.dto.AlertRuleRequest
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult

class AlertRuleRepository(private val api: ApiService) {

    /** Fetch all rules (active and inactive). */
    suspend fun getRules(): NetworkResult<List<AlertRuleDto>> =
        safeApiCall { api.getAlertRules() }

    /** Fetch a single rule by [id]. */
    suspend fun getRule(id: Long): NetworkResult<AlertRuleDto> =
        safeApiCall { api.getAlertRule(id) }

    /**
     * Create a new rule.
     * @return The persisted rule including the server-assigned [AlertRuleDto.id].
     */
    suspend fun createRule(request: AlertRuleRequest): NetworkResult<AlertRuleDto> =
        safeApiCall { api.createAlertRule(request) }

    /**
     * Fully replace an existing rule. All required fields must be provided.
     */
    suspend fun updateRule(id: Long, request: AlertRuleRequest): NetworkResult<AlertRuleDto> =
        safeApiCall { api.updateAlertRule(id, request) }

    /**
     * Partially update a rule. Only the non-null fields in [patch] are sent.
     * Useful for toggling [AlertRulePatchRequest.enabled] without re-sending
     * the full rule body.
     */
    suspend fun patchRule(id: Long, patch: AlertRulePatchRequest): NetworkResult<AlertRuleDto> =
        safeApiCall { api.patchAlertRule(id, patch) }

    /**
     * Convenience: toggle the `enabled` flag of a rule in one call.
     */
    suspend fun toggleEnabled(id: Long, enabled: Boolean): NetworkResult<AlertRuleDto> =
        patchRule(id, AlertRulePatchRequest(enabled = enabled))

    /**
     * Permanently delete a rule.
     * [NetworkResult.Success] wraps [Unit] on HTTP 204.
     */
    suspend fun deleteRule(id: Long): NetworkResult<Unit> =
        safeApiCall { api.deleteAlertRule(id) }
}


