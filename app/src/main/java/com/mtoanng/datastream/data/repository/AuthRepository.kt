package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.LoginRequest
import com.mtoanng.datastream.data.dto.LoginResponse
import com.mtoanng.datastream.data.dto.UserDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.prefs.TokenManager

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager,
) {
    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> {
        val result = safeApiCall { api.login(LoginRequest(username, password)) }
        if (result is NetworkResult.Success) {
            tokenManager.saveSession(
                accessToken = result.data.accessToken,
                expiresInMs = result.data.expiresInMs,
                user = result.data.user,
            )
        }
        return result
    }

    suspend fun me(): NetworkResult<UserDto> = safeApiCall { api.me() }

    fun cachedUser(): UserDto? = tokenManager.getUser()

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun logout() = tokenManager.clear()
}
