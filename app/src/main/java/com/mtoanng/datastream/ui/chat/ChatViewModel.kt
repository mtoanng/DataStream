package com.mtoanng.datastream.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.dto.ChatMessage
import com.mtoanng.datastream.data.dto.FuelPriceDto
import com.mtoanng.datastream.data.dto.GridLoadDto
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.dto.SecurityScoreDto
import com.mtoanng.datastream.data.repository.ChatRepository
import kotlinx.coroutines.launch

/**
 * Backs màn hình Chat với AI.
 *
 * Flow:
 * 1. Fragment gọi [updateContext] khi có dữ liệu mới từ các màn hình khác
 * 2. User nhập tin nhắn → Fragment gọi [sendMessage]
 * 3. Fragment observe [messages] để cập nhật RecyclerView
 */
class ChatViewModel(private val repo: ChatRepository) : ViewModel() {

    // ── Messages ──────────────────────────────────────────────────────────────

    private val _messages = MutableLiveData<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "Xin chào! Mình là trợ lý AI của VES-Monitor. " +
                    "Bạn có thể hỏi mình về chỉ số an ninh năng lượng, " +
                    "cảnh báo đang xảy ra, giá nhiên liệu, hoặc các đề xuất hành động. 👋",
            )
        )
    )
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    // ── Send message ──────────────────────────────────────────────────────────

    /**
     * Gửi tin nhắn của user và nhận câu trả lời từ Gemini.
     * Tự động thêm tin nhắn vào [messages] trước và sau khi nhận response.
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(role = "user", text = text.trim())
        appendMessage(userMsg)
        _isTyping.value = true

        viewModelScope.launch {
            // Lấy history trước khi gửi (không gồm tin nhắn vừa thêm)
            val history = _messages.value.orEmpty().dropLast(1)

            val result = repo.sendMessage(
                userMessage = text.trim(),
                history = history,
            )

            _isTyping.value = false

            result.fold(
                onSuccess = { reply ->
                    appendMessage(ChatMessage(role = "model", text = reply))
                },
                onFailure = { error ->
                    appendMessage(
                        ChatMessage(
                            role = "model",
                            text = "Xin lỗi, mình gặp lỗi kết nối: ${error.localizedMessage}. Bạn thử lại nhé!",
                            isError = true,
                        )
                    )
                }
            )
        }
    }

    // ── Context update ────────────────────────────────────────────────────────

    /**
     * Cập nhật dữ liệu thật để AI trả lời chính xác hơn.
     * Gọi từ Fragment khi observe dữ liệu từ các ViewModel khác.
     */
    fun updateContext(
        securityScore: SecurityScoreDto? = null,
        alerts: List<AlertDto>? = null,
        fuelPrices: List<FuelPriceDto>? = null,
        gridLoad: List<GridLoadDto>? = null,
        recommendations: List<RecommendationDto>? = null,
    ) {
        repo.updateContext(securityScore, alerts, fuelPrices, gridLoad, recommendations)
    }

    /** Xóa toàn bộ lịch sử chat, giữ lại tin chào mừng. */
    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(
                role = "model",
                text = "Lịch sử chat đã được xóa. Mình có thể giúp gì cho bạn?",
            )
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun appendMessage(msg: ChatMessage) {
        _messages.value = _messages.value.orEmpty() + msg
    }
}
