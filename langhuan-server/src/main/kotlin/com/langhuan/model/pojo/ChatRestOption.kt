package com.langhuan.model.pojo

import lombok.Data

@Data
data class ChatRestOption(
    var chatId: String? = null,
    var prompt: String? = null,
    // 对应userMessage
    var userMessage: String? = null,
    var ragGroupId: String? = null,
    var isReRank: Boolean? = null,
    var isFunction: Boolean? = null,
    var modelName: String? = null
)
