package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.dto.ChatMessage
import com.mtoanng.datastream.data.dto.FuelPriceDto
import com.mtoanng.datastream.data.dto.GithubChatRequest
import com.mtoanng.datastream.data.dto.GithubMessage
import com.mtoanng.datastream.data.dto.GridLoadDto
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.dto.SecurityScoreDto
import com.mtoanng.datastream.data.network.GithubApiService
import com.mtoanng.datastream.data.network.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Quản lý chatbot sử dụng GitHub Models API (GPT-4o).
 */
class ChatRepository(private val githubToken: String) {

    private val api: GithubApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://models.inference.ai.azure.com/")
            .client(client)
            // CỰC KỲ QUAN TRỌNG: Dùng Moshi từ NetworkModule để hỗ trợ Kotlin data class
            .addConverterFactory(MoshiConverterFactory.create(NetworkModule.moshi))
            .build()
            .create(GithubApiService::class.java)
    }

    // ── Context từ dữ liệu thật của app ──────────────────────────────────────

    private var securityScore: SecurityScoreDto? = null
    private var alerts: List<AlertDto> = emptyList()
    private var fuelPrices: List<FuelPriceDto> = emptyList()
    private var gridLoad: List<GridLoadDto> = emptyList()
    private var recommendations: List<RecommendationDto> = emptyList()

    fun updateContext(
        securityScore: SecurityScoreDto? = null,
        alerts: List<AlertDto>? = null,
        fuelPrices: List<FuelPriceDto>? = null,
        gridLoad: List<GridLoadDto>? = null,
        recommendations: List<RecommendationDto>? = null,
    ) {
        securityScore?.let { this.securityScore = it }
        alerts?.let { this.alerts = it }
        fuelPrices?.let { this.fuelPrices = it }
        gridLoad?.let { this.gridLoad = it }
        recommendations?.let { this.recommendations = it }
    }

    // ── Gửi tin nhắn ─────────────────────────────────────────────────────────

    suspend fun sendMessage(
        userMessage: String,
        history: List<ChatMessage>,
    ): Result<String> {
        return try {
            val messages = mutableListOf<GithubMessage>()

            // 1. System Prompt
            messages.add(GithubMessage("system", SYSTEM_PROMPT))

            // 2. History
            messages.addAll(history.filter { !it.isError }.map {
                GithubMessage(if (it.role == "user") "user" else "assistant", it.text)
            })

            // 3. User Message with Context
            val messageWithContext = buildMessageWithContext(userMessage)
            messages.add(GithubMessage("user", messageWithContext))

            val request = GithubChatRequest(messages = messages)
            val response = api.githubChat("Bearer $githubToken", request)

            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content
                    ?: "Xin lỗi, mình không nhận được phản hồi."
                Result.success(text)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Lỗi API GitHub (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "GitHub API error")
            Result.failure(e)
        }
    }

    private fun buildMessageWithContext(userMessage: String): String {
        val contextParts = mutableListOf<String>()

        securityScore?.let { score ->
            contextParts += """
                [DỮ LIỆU HIỆN TẠI - Chỉ số an ninh năng lượng]
                Tổng điểm: ${score.overallScore}/100 (${score.status})
                T1: ${score.pillar1Score}, T2: ${score.pillar2Score}, T3: ${score.pillar3Score}, T4: ${score.pillar4Score}
            """.trimIndent()
        }

        if (alerts.isNotEmpty()) {
            val alertSummary = alerts.take(3).joinToString("\n") { alert ->
                "- [${alert.severity}] ${alert.metricType}: ${alert.message?.take(50) ?: ""}"
            }
            contextParts += """
                [Cảnh báo (${alerts.size})]
                $alertSummary
            """.trimIndent()
        }

        if (fuelPrices.isNotEmpty()) {
            val priceSummary = fuelPrices.take(3).joinToString("\n") { p ->
                "- ${p.fuelType}: ${p.price}"
            }
            contextParts += """
                [Giá nhiên liệu]
                $priceSummary
            """.trimIndent()
        }

        if (gridLoad.isNotEmpty()) {
            val loadSummary = gridLoad.take(2).joinToString("\n") { g ->
                "- ${g.regionName ?: g.regionCode}: ${g.loadPct}%"
            }
            contextParts += """
                [Tải lưới]
                $loadSummary
            """.trimIndent()
        }

        if (recommendations.isNotEmpty()) {
            val recSummary = recommendations.take(2).joinToString("\n") { r ->
                "- ${r.title}: ${r.message?.take(50) ?: ""}"
            }
            contextParts += """
                [Đề xuất]
                $recSummary
            """.trimIndent()
        }

        return if (contextParts.isEmpty()) {
            userMessage
        } else {
            "Bối cảnh hệ thống:\n${contextParts.joinToString(" | ")}\n\nCâu hỏi: $userMessage"
        }
    }

    companion object {
        private const val SYSTEM_PROMPT = """
Bạn là trợ lý AI chuyên về an ninh năng lượng, tích hợp trong ứng dụng VES-Monitor.

NHIỆM VỤ CỦA BẠN:
1. Giải thích các chỉ số an ninh năng lượng (điểm trụ cột, tổng điểm, trạng thái)
2. Phân tích cảnh báo đang xảy ra và giải thích nguyên nhân
3. Giải thích các đề xuất hành động theo ngôn ngữ dễ hiểu
4. Trả lời câu hỏi về giá nhiên liệu và tải lưới điện
5. Hướng dẫn người dùng điều hướng trong app khi được hỏi

PHONG CÁCH:
- Trả lời bằng tiếng Việt, ngắn gọn và rõ ràng
- Dùng số liệu thực tế từ dữ liệu được cung cấp khi có thể
- Giải thích thuật ngữ kỹ thuật bằng ngôn ngữ đơn giản
- Nếu không có đủ dữ liệu để trả lời, hãy nói rõ

GIỚI HẠN:
- Chỉ trả lời về năng lượng và ứng dụng này
- Không đưa ra quyết định thay người dùng
- Không bịa đặt số liệu nếu không có trong dữ liệu được cung cấp
        """
    }
}
