package com.langhuan.serviceai;

import com.langhuan.common.Constant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class EasyChatService {
    private final ChatClient chatClient;

    public EasyChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    public String chat(String p, String q, String modelName) {
        return this.chatClient.prompt(
                        new Prompt(
                                p,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()
                        )
                )
                .user(q)
                .call().content();
    }

}
