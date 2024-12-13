package com.shuangqi.aiagent7.service.base;

import com.shuangqi.aiagent7.model.dto.MyChatRequest;
import com.shuangqi.aiagent7.model.vo.MyChatResponse;
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
    public MyChatResponse chat(MyChatRequest request) {
        log.info("chat-Prompt: {}", request.getSystemPrompt());
        log.info("chat-Message: {}", request.getMessage());
        try {
            String response = "";
            if (request.getSystemPrompt() == null || request.getSystemPrompt().isEmpty()) {
                response = chatClient.prompt().user(request.getMessage()).call().content();
            } else {
                response = chatClient.prompt(request.getSystemPrompt()).user(request.getMessage()).call().content();
            }
            log.info("chat-vo: {}", response);
            return MyChatResponse.builder()
                    .response(response)
                    .status("success")
                    .build();
        } catch (Exception e) {
            return MyChatResponse.builder()
                    .response("Error processing dto: " + e.getMessage())
                    .status("error")
                    .build();
        }
    }
}
