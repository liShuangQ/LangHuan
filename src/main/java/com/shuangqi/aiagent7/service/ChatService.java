package com.shuangqi.aiagent7.service;

import com.shuangqi.aiagent7.model.ChatRequest;
import com.shuangqi.aiagent7.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are a helpful AI assistant. Provide clear and concise responses.";

    public ChatResponse chat(ChatRequest request) {
        try {
            String systemPrompt = request.getSystemPrompt() != null ?
                    request.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;

            String response = chatClient.prompt(systemPrompt).user(request.getMessage()).call().content();

            return ChatResponse.builder()
                    .response(response)
                    .status("success")
                    .build();
        } catch (Exception e) {
            return ChatResponse.builder()
                    .response("Error processing request: " + e.getMessage())
                    .status("error")
                    .build();
        }
    }
}
