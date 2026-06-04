package com.mtoanng.datastream

import android.app.Application
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkModule
import com.mtoanng.datastream.data.prefs.AppConfig
import com.mtoanng.datastream.data.prefs.TokenManager
import com.mtoanng.datastream.data.repository.AlertRepository
import com.mtoanng.datastream.data.repository.AlertRuleRepository
import com.mtoanng.datastream.data.repository.AuthRepository
import com.mtoanng.datastream.data.repository.ExportRepository
import com.mtoanng.datastream.data.repository.HistoryRepository
import com.mtoanng.datastream.data.repository.PagedRepository
import com.mtoanng.datastream.data.repository.PillarRepository
import com.mtoanng.datastream.data.repository.RecommendationRepository
import com.mtoanng.datastream.data.repository.SecurityRepository
import timber.log.Timber
import com.mtoanng.datastream.data.repository.ChatRepository  // NEW
import com.mtoanng.datastream.ui.chat.ChatViewModel            // NEW (không cần, Factory tạo)

class DataStreamApp : Application() {

    val tokenManager: TokenManager by lazy { TokenManager.getInstance(this) }
    val appConfig: AppConfig by lazy { AppConfig.getInstance(this) }

    // Mới: Truyền chữ 'this' (chính là DataStreamApp context) vào đầu tiên
    fun apiService(): ApiService = NetworkModule.apiService(this, tokenManager, appConfig)
    fun rebuildApi(): ApiService = NetworkModule.recreate(this, tokenManager, appConfig)

    // ── Gộp toàn bộ nghiệp vụ Auth vào đây ─────────────────────────────────────
    fun authRepository(): AuthRepository =
        AuthRepository(apiService(), tokenManager)
    /** GitHub Models Chatbot repository. Token đọc từ BuildConfig. */
    fun chatRepository() = ChatRepository(BuildConfig.GITHUB_TOKEN)


    fun securityRepository(): SecurityRepository = SecurityRepository(apiService())
    fun pillarRepository(): PillarRepository = PillarRepository(apiService())
    fun alertRepository(): AlertRepository = AlertRepository(apiService())
    fun recommendationRepository(): RecommendationRepository = RecommendationRepository(apiService())

    // ── Các nghiệp vụ mới tinh ────────────────────────────────────────────────
    fun alertRuleRepository(): AlertRuleRepository = AlertRuleRepository(apiService())
    fun historyRepository(): HistoryRepository = HistoryRepository(apiService())
    fun pagedRepository(): PagedRepository = PagedRepository(apiService())
    fun exportRepository(): ExportRepository = ExportRepository(apiService(), applicationContext)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
