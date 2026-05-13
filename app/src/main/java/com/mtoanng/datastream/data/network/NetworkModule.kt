package com.mtoanng.datastream.data.network

import com.mtoanng.datastream.BuildConfig
import com.mtoanng.datastream.data.prefs.AppConfig
import com.mtoanng.datastream.data.prefs.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Hand-rolled DI: builds a single [ApiService] for the current `baseUrl`. We expose
 * [recreate] so the Settings screen can swap base URL at runtime without restarting
 * the app.
 */
object NetworkModule {

    private const val TIMEOUT_SECONDS = 20L

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var currentBaseUrl: String = ""

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Synchronized
    fun apiService(tokenManager: TokenManager, appConfig: AppConfig): ApiService {
        val baseUrl = appConfig.baseUrl
        if (retrofit == null || currentBaseUrl != baseUrl) {
            retrofit = build(baseUrl, tokenManager)
            currentBaseUrl = baseUrl
        }
        return retrofit!!.create(ApiService::class.java)
    }

    /** Force a rebuild — call after the user changes baseUrl in Settings. */
    @Synchronized
    fun recreate(tokenManager: TokenManager, appConfig: AppConfig): ApiService {
        retrofit = null
        return apiService(tokenManager, appConfig)
    }

    private fun build(baseUrl: String, tokenManager: TokenManager): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
    }
}
