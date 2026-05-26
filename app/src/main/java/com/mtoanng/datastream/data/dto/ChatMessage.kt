package com.mtoanng.datastream.data.dto

/**
 * Một tin nhắn trong cuộc hội thoại với chatbot.
 *
 * [role] : "user" | "model"
 * [text] : nội dung tin nhắn
 * [isError] : true nếu đây là thông báo lỗi từ app (không phải từ AI)
 */
data class ChatMessage(
    val role: String,
    val text: String,
    val isError: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis(),
) {
    val isUser: Boolean get() = role == "user"
    val isModel: Boolean get() = role == "model"
}
