package com.example.zusammen

data class ChatMessage(
val text: String,
val isUserMessage: Boolean,
val timestamp: Long = System.currentTimeMillis()
)
