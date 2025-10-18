package com.langhuan.model.dto

data class MyChatRequest(
    private val message: String,
    private val systemPrompt: String
)
