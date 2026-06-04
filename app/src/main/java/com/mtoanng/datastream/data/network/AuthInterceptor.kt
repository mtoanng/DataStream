// File: AuthInterceptor.kt
package com.mtoanng.datastream.data.network

import android.content.Context
import android.content.Intent
import com.mtoanng.datastream.data.prefs.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val context: Context // Thêm context vào constructor
) : Interceptor {

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

        val response = chain.proceed(request)

        // Bắt lỗi 401 Unauthorized
        if (response.code == 401) {
            val path = original.url.encodedPath

            // Log chi tiết để biết chính xác endpoint nào gây lỗi
            timber.log.Timber.e("Lỗi 401 tại: $path | Token: ${token?.take(15)}...")

            val isAuthPath = path.contains("/auth/login") ||
                             path.contains("/auth/social-login") ||
                             path.contains("/auth/refresh")

            // KHÔNG đẩy ra ngoài nếu:
            // 1. Đang ở đường dẫn login/refresh/social-login
            // 2. Token là "mock_" (đang trong chế độ giả lập để test giao diện)
            if (!isAuthPath && token?.contains("mock") != true) {
                val intent = Intent("com.mtoanng.datastream.ACTION_TOKEN_EXPIRED")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
            }
        }

        return response
    }
}
