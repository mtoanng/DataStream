package com.mtoanng.datastream.data.network

import com.mtoanng.datastream.data.prefs.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds `Authorization: Bearer <token>` to every outgoing request when a token is
 * available. Public endpoints (`/api/auth/login`, `/api/health`) tolerate the header,
 * so we don't bother filtering by path.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenManager.getToken()
        val request = if (!token.isNullOrBlank() && original.header("Authorization") == null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
