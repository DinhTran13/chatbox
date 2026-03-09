package com.example.educhatbot

// Data class gửi câu hỏi lên OpenAI
data class ChatRequest(
    val model: String = "google/gemini-2.0-flash:free", // Có thể đổi thành gpt-4o-mini nếu muốn
    val messages: List<OpenAIMessage>
)

// Cấu trúc 1 tin nhắn của OpenAI (gồm role và content)
data class OpenAIMessage(
    val role: String, // "user" (người dùng), "assistant" (AI), hoặc "system" (cài đặt)
    val content: String
)

// Data class nhận câu trả lời từ OpenAI
data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: OpenAIMessage
)