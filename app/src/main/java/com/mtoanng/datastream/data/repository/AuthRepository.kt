package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.LoginRequest
import com.mtoanng.datastream.data.dto.LoginResponse
import com.mtoanng.datastream.data.dto.LogoutRequest
import com.mtoanng.datastream.data.dto.MessageResponse
import com.mtoanng.datastream.data.dto.RefreshTokenRequest
import com.mtoanng.datastream.data.dto.RefreshTokenResponse
import com.mtoanng.datastream.data.dto.UserDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.prefs.TokenManager
import timber.log.Timber

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

    // --- LOGIC MỚI ĐƯỢC THÊM VÀO ---
    suspend fun refreshToken(): NetworkResult<RefreshTokenResponse> {
        val currentToken = tokenManager.getToken()
            ?: return NetworkResult.Error(message = "No active session to refresh")

        val result = safeApiCall { api.refreshToken(RefreshTokenRequest(currentToken)) }

        if (result is NetworkResult.Success) {
            val user = tokenManager.getUser()
                ?: return NetworkResult.Error(message = "Cached user missing after refresh")
            tokenManager.saveSession(
                accessToken = result.data.accessToken,
                expiresInMs = result.data.expiresInMs,
                user = user,
            )
            Timber.d("Token refreshed, new expiry in ${result.data.expiresInMs / 1000}s")
        }
        return result
    }

    suspend fun logoutServer(): NetworkResult<MessageResponse> {
        val token = tokenManager.getToken()
        tokenManager.clear() // Xóa local trước cho an toàn

        if (token == null) {
            return NetworkResult.Success(MessageResponse("Session cleared locally"))
        }

        val result = safeApiCall { api.logout(LogoutRequest(token)) }
        if (result is NetworkResult.Error) {
            Timber.w("Server logout failed, local session cleared anyway")
        }
        return result
    }

    // --- CÁC HÀM TIỆN ÍCH ---
    fun cachedUser(): UserDto? = tokenManager.getUser()
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    fun logout() = tokenManager.clear()
}
