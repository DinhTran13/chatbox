package com.example.educhatbot

data class Message(
    val text: String,
    val isSentByUser: Boolean // true = User gửi, false = AI gửi
)