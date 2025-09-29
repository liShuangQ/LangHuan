package com.langhuan.utils.chatMemory;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Map;

public class ChatMemoryUtils {
    public static Message createdMessage(String text, Map<String, Object> metadata, MessageType messageType) {
        return new Message() {
            @Override
            public String getText() {
                return text;
            }

            @Override
            public Map<String, Object> getMetadata() {
                return metadata;
            }

            @Override
            public MessageType getMessageType() {
                return messageType;
            }
        };
    }
}
