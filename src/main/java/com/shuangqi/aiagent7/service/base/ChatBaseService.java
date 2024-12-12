package com.shuangqi.aiagent7.service.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuangqi.aiagent7.common.BusinessException;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.model.request.ChatRequest;
import com.shuangqi.aiagent7.model.response.ChatResponse;
import com.shuangqi.aiagent7.model.response.ElephantExperimentRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatBaseService {

    private final ChatClient chatClient;

    public ChatBaseService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 聊天
     *
     * @param request
     * @return
     */
    public ChatResponse chat(ChatRequest request) {
        String systemPrompt = !request.getSystemPrompt().isEmpty() ?
                request.getSystemPrompt() : Constant.DEFAULT_SYSTEM_PROMPT;
        log.info("chat-Prompt: {}", systemPrompt);
        log.info("chat-Message: {}", request.getMessage());
        try {
            String response = chatClient.prompt(systemPrompt).user(request.getMessage()).call().content();
            log.info("chat-response: {}", response);
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
