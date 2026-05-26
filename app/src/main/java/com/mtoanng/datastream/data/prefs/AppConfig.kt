package com.mtoanng.datastream.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Persistent app-level config: server URL, refresh interval. Backed by SharedPreferences
 * (`app_config.xml`). Kept separate from [TokenManager] so logout can clear credentials
 * without nuking the user's chosen server URL.
 */
class AppConfig private constructor(private val prefs: SharedPreferences) {

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) {
            val normalized = value.trim().trimEnd('/').ifEmpty { DEFAULT_BASE_URL }
            prefs.edit { putString(KEY_BASE_URL, "$normalized/") }
        }

    /** Auto-refresh interval for foreground screens (seconds). */
    var refreshIntervalSeconds: Int
        get() = prefs.getInt(KEY_REFRESH, DEFAULT_REFRESH_SECONDS)
        set(value) = prefs.edit { putInt(KEY_REFRESH, value.coerceIn(5, 600)) }

    fun resetToDefaults() {
        prefs.edit {
            putString(KEY_BASE_URL, DEFAULT_BASE_URL)
            putInt(KEY_REFRESH, DEFAULT_REFRESH_SECONDS)
        }
    }

    companion object {
        private const val PREFS_NAME = "app_config"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_REFRESH = "refresh_interval_seconds"

        /** Emulator's loopback to the host machine. */
        const val DEFAULT_BASE_URL = "http://172.30.184.31:8090/"
        const val DEFAULT_REFRESH_SECONDS = 30

        @Volatile private var INSTANCE: AppConfig? = null

        fun getInstance(context: Context): AppConfig =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppConfig(
                    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ).also { INSTANCE = it }
            }
    }
}
