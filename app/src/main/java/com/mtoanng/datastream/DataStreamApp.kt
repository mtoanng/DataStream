package com.mtoanng.datastream

import android.app.Application
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkModule
import com.mtoanng.datastream.data.prefs.AppConfig
import com.mtoanng.datastream.data.prefs.TokenManager
import com.mtoanng.datastream.data.repository.AlertRepository
import com.mtoanng.datastream.data.repository.AuthRepository
import com.mtoanng.datastream.data.repository.PillarRepository
import com.mtoanng.datastream.data.repository.RecommendationRepository
import com.mtoanng.datastream.data.repository.SecurityRepository
import timber.log.Timber

/**
 * Manual DI container — exposes lazily-built repositories to the UI layer. We keep
 * `apiService` short-lived (rebuilt when baseUrl changes) but everything else stays
 * for the process's lifetime.
 */
class DataStreamApp : Application() {

    val tokenManager: TokenManager by lazy { TokenManager.getInstance(this) }
    val appConfig: AppConfig by lazy { AppConfig.getInstance(this) }

    fun apiService(): ApiService = NetworkModule.apiService(tokenManager, appConfig)

    fun rebuildApi(): ApiService = NetworkModule.recreate(tokenManager, appConfig)

    fun authRepository(): AuthRepository = AuthRepository(apiService(), tokenManager)
    fun securityRepository(): SecurityRepository = SecurityRepository(apiService())
    fun pillarRepository(): PillarRepository = PillarRepository(apiService())
    fun alertRepository(): AlertRepository = AlertRepository(apiService())
    fun recommendationRepository(): RecommendationRepository = RecommendationRepository(apiService())

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
