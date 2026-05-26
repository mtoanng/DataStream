package com.mtoanng.datastream.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.dto.ChatMessage
import com.mtoanng.datastream.data.dto.FuelPriceDto
import com.mtoanng.datastream.data.dto.GridLoadDto
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.dto.SecurityScoreDto
import timber.log.Timber

/**
 * Quản lý cuộc hội thoại với Gemini AI.
 *
 * Mỗi lần user gửi tin nhắn, [sendMessage] sẽ:
 * 1. Đính kèm context dữ liệu thật (security score, alerts, ...) vào system prompt
 * 2. Gửi toàn bộ lịch sử chat để Gemini nhớ ngữ cảnh
 * 3. Trả về câu trả lời dạng String
 *
 * Dữ liệu context được cập nhật từ ViewModel trước mỗi lần gửi
 * thông qua [updateContext].
 */
class ChatRepository(apiKey: String) {

    // ── Gemini model setup ────────────────────────────────────────────────────

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",   // nhanh + miễn phí
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            maxOutputTokens = 1024
        },
        systemInstruction = content {
            text(SYSTEM_PROMPT)
        },
    )

    // ── Context từ dữ liệu thật của app ──────────────────────────────────────

    private var securityScore: SecurityScoreDto? = null
    private var alerts: List<AlertDto> = emptyList()
    private var fuelPrices: List<FuelPriceDto> = emptyList()
    private var gridLoad: List<GridLoadDto> = emptyList()
    private var recommendations: List<RecommendationDto> = emptyList()

    /**
     * Cập nhật context dữ liệu thật trước khi gửi tin nhắn.
     * Gọi từ ViewModel mỗi khi dữ liệu trên màn hình thay đổi.
     */
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

    /**
     * Gửi [userMessage] kèm toàn bộ [history] đến Gemini và trả về câu trả lời.
     *
     * @param userMessage  Tin nhắn mới nhất của user
     * @param history      Lịch sử chat trước đó (không gồm [userMessage])
     * @return Câu trả lời của AI, hoặc null nếu có lỗi
     */
    suspend fun sendMessage(
        userMessage: String,
        history: List<ChatMessage>,
    ): Result<String> {
        return try {
            // Xây dựng history cho Gemini (bỏ qua các tin nhắn lỗi)
            val chatHistory = history
                .filter { !it.isError }
                .map { msg ->
                    content(role = msg.role) { text(msg.text) }
                }

            val chat = model.startChat(history = chatHistory)

            // Đính kèm context dữ liệu thật vào tin nhắn
            val messageWithContext = buildMessageWithContext(userMessage)

            val response = chat.sendMessage(messageWithContext)
            val text = response.text ?: "Xin lỗi, mình không hiểu câu hỏi này."
            Timber.d("Gemini response: ${text.take(100)}...")
            Result.success(text)
        } catch (e: Exception) {
            Timber.e(e, "Gemini API error")
            Result.failure(e)
        }
    }

    // ── Build context ─────────────────────────────────────────────────────────

    /**
     * Gắn context dữ liệu thật vào cuối tin nhắn của user.
     * Gemini sẽ dùng thông tin này để trả lời chính xác hơn.
     */
    private fun buildMessageWithContext(userMessage: String): String {
        val contextParts = mutableListOf<String>()

        securityScore?.let { score ->
            contextParts += """
                [DỮ LIỆU HIỆN TẠI - Chỉ số an ninh năng lượng]
                Tổng điểm: ${score.overallScore}/100 (${score.status})
                Trụ cột 1 (Cung cấp): ${score.pillar1Score}
                Trụ cột 2 (Thị trường): ${score.pillar2Score}
                Trụ cột 3 (Lưới điện): ${score.pillar3Score}
                Trụ cột 4 (Chuyển đổi): ${score.pillar4Score}
                Cập nhật lúc: ${score.computedAt}
            """.trimIndent()
        }

        if (alerts.isNotEmpty()) {
            val alertSummary = alerts.take(5).joinToString("\n") { alert ->
                "- [${alert.severity}] ${alert.metricType}: ${alert.message ?: "Không có mô tả"}"
            }
            contextParts += """
                [DỮ LIỆU HIỆN TẠI - Cảnh báo đang hoạt động (${alerts.size} cảnh báo)]
                $alertSummary
            """.trimIndent()
        }

        if (fuelPrices.isNotEmpty()) {
            val priceSummary = fuelPrices.take(5).joinToString("\n") { p ->
                "- ${p.fuelType}: ${p.price} ${p.priceUnit ?: ""} (${p.location ?: p.region ?: "N/A"})"
            }
            contextParts += """
                [DỮ LIỆU HIỆN TẠI - Giá nhiên liệu]
                $priceSummary
            """.trimIndent()
        }

        if (gridLoad.isNotEmpty()) {
            val loadSummary = gridLoad.take(3).joinToString("\n") { g ->
                "- ${g.regionName ?: g.regionCode}: ${g.loadPct ?: "N/A"}% tải (${g.status ?: "N/A"})"
            }
            contextParts += """
                [DỮ LIỆU HIỆN TẠI - Tải lưới điện]
                $loadSummary
            """.trimIndent()
        }

        if (recommendations.isNotEmpty()) {
            val recSummary = recommendations.take(3).joinToString("\n") { r ->
                "- [Trụ cột ${r.pillar}] ${r.title}: ${r.message?.take(80) ?: ""}"
            }
            contextParts += """
                [DỮ LIỆU HIỆN TẠI - Đề xuất hành động]
                $recSummary
            """.trimIndent()
        }

        return if (contextParts.isEmpty()) {
            userMessage
        } else {
            "${contextParts.joinToString("\n\n")}\n\n[CÂU HỎI CỦA NGƯỜI DÙNG]\n$userMessage"
        }
    }

    // ── System prompt ─────────────────────────────────────────────────────────

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
