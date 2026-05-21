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
            // Gửi một broadcast toàn cục để thông báo token đã chết
            val intent = Intent("com.mtoanng.datastream.ACTION_TOKEN_EXPIRED")
            intent.setPackage(context.packageName) // Bảo mật: chỉ gửi trong nội bộ app
            context.sendBroadcast(intent)
        }

        return response
    }
}
