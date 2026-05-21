package com.mtoanng.datastream.data.network

import android.content.Context
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
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.util.concurrent.TimeUnit

/**
 * Hand-rolled DI: builds a single [ApiService] for the current `baseUrl`. We expose
 * [recreate] so the Settings screen can swap base URL at runtime without restarting
 * the app.
 *
 * The exposed [ApiService] is a **delegating proxy** that always forwards each call
 * to whatever Retrofit instance is currently bound to [retrofit]. Repositories
 * captured during ViewModel construction therefore keep working seamlessly after
 * [recreate] swaps the baseUrl — the underlying transport is rebuilt, but
 * downstream consumers see no reference change.
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

    /**
     * Single cached `java.lang.reflect.Proxy` that re-resolves the underlying Retrofit
     * implementation on every method call. Built lazily on first access.
     */
    private val apiServiceProxy: ApiService by lazy {
        @Suppress("UNCHECKED_CAST")
        Proxy.newProxyInstance(
            ApiService::class.java.classLoader,
            arrayOf(ApiService::class.java),
        ) { _, method, args ->
            val current = retrofit
                ?: error(
                    "NetworkModule.apiService(...) must be called at least once before " +
                        "invoking ApiService methods on the proxy.",
                )
            val impl = current.create(ApiService::class.java)
            try {
                if (args == null) method.invoke(impl) else method.invoke(impl, *args)
            } catch (ite: InvocationTargetException) {
                throw ite.cause ?: ite
            }
        } as ApiService
    }

    @Synchronized
    fun apiService(context: Context, tokenManager: TokenManager, appConfig: AppConfig): ApiService {
        val baseUrl = appConfig.baseUrl
        if (retrofit == null || currentBaseUrl != baseUrl) {
            // Đã sửa: Truyền context vào hàm build
            retrofit = build(context, baseUrl, tokenManager)
            currentBaseUrl = baseUrl
        }
        return apiServiceProxy
    }

    /** Force a rebuild — call after the user changes baseUrl in Settings. */
    @Synchronized
    fun recreate(context: Context, tokenManager: TokenManager, appConfig: AppConfig): ApiService {
        retrofit = null
        // Đã sửa: Truyền context vào apiService
        return apiService(context, tokenManager, appConfig)
    }

    /**
     * Test-only hook: swap the bound Retrofit instance directly without going through
     * [AppConfig]/[TokenManager]. Returns the same [apiServiceProxy] reference, so
     * callers can verify that previously-captured proxy references redirect to the
     * new instance after a swap.
     */
    @androidx.annotation.VisibleForTesting
    @Synchronized
    internal fun setRetrofitForTest(replacement: Retrofit): ApiService {
        retrofit = replacement
        currentBaseUrl = replacement.baseUrl().toString()
        return apiServiceProxy
    }

    /** Test-only hook: clear cached state between tests. */
    @androidx.annotation.VisibleForTesting
    @Synchronized
    internal fun resetForTest() {
        retrofit = null
        currentBaseUrl = ""
    }

    // Đã sửa: Thêm tham số context: Context vào đây
    private fun build(context: Context, baseUrl: String, tokenManager: TokenManager): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Đã sửa: Xuống dòng chuẩn syntax và truyền context
            .addInterceptor(AuthInterceptor(tokenManager, context))
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
