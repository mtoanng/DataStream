package com.mtoanng.datastream.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mtoanng.datastream.data.dto.UserDto

/**
 * Stores the JWT access token + lightweight user info + expiry timestamp. Cleared on
 * logout or 401. Backed by SharedPreferences (`auth_session.xml`).
 */
class TokenManager private constructor(private val prefs: SharedPreferences) {

    fun saveSession(accessToken: String, expiresInMs: Long, user: UserDto) {
        // Nếu server trả về giây (ví dụ 3600) thay vì ms, ta nhân với 1000.
        // Ngưỡng 10^9 ms (~11 ngày) là mốc phân biệt hợp lý giữa s và ms.
        val durationMs = if (expiresInMs < 1_000_000_000L) expiresInMs * 1000 else expiresInMs

        prefs.edit {
            putString(KEY_TOKEN, accessToken)
            putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + durationMs)
            putLong(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_EMAIL, user.email)
            putString(KEY_ROLE, user.role)
            putBoolean(KEY_ENABLED, user.enabled)
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        if (token.isBlank()) return false
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)

        // Nới lỏng kiểm tra: Nếu token mock (Google) thì cho phép qua nếu expiresAt chưa quá cũ
        if (token.startsWith("mock_")) {
             return (expiresAt - System.currentTimeMillis()) > -300000 // Cho phép sai lệch 5 phút quá hạn
        }

        return System.currentTimeMillis() < expiresAt
    }

    fun getUser(): UserDto? {
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        return UserDto(
            id = prefs.getLong(KEY_USER_ID, 0L),
            username = username,
            fullName = prefs.getString(KEY_FULL_NAME, null),
            email = prefs.getString(KEY_EMAIL, null),
            role = prefs.getString(KEY_ROLE, "VIEWER") ?: "VIEWER",
            enabled = prefs.getBoolean(KEY_ENABLED, true),
        )
    }

    fun expiresAtMillis(): Long = prefs.getLong(KEY_EXPIRES_AT, 0L)

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "auth_session"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_EXPIRES_AT = "expires_at_ms"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_ENABLED = "enabled"

        @Volatile private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(
                    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ).also { INSTANCE = it }
            }
    }
}
