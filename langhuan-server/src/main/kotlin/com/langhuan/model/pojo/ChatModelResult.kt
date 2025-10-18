package com.langhuan.model.pojo

import lombok.Data
import org.springframework.ai.document.Document

@Data
data class ChatModelResult(
    var chat: String? = null,
    var rag: List<Document>? = null
)
