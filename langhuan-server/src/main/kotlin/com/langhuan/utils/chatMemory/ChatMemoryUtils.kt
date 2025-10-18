package com.langhuan.utils.chatMemory

import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType

object ChatMemoryUtils {
    
    fun createdMessage(
        text: String,
        metadata: Map<String, Any>,
        messageType: MessageType
    ): Message {
        return object : Message {
            override fun getText(): String = text
            
            override fun getMetadata(): Map<String, Any> = metadata
            
            override fun getMessageType(): MessageType = messageType
        }
    }
}
