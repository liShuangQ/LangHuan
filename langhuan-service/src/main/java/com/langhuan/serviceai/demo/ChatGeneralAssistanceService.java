package com.langhuan.serviceai.demo;

import com.langhuan.advisors.MySimplelogAdvisor;
import com.langhuan.service.TPromptsService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatGeneralAssistanceService {

    private final ChatClient chatClient;

    public ChatGeneralAssistanceService(ChatClient.Builder chatClientBuilder) {
        chatClient = chatClientBuilder
                .defaultAdvisors(
                        new MySimplelogAdvisor()
                )
                .build();
    }

    public String tools(String p) {
        return chatClient
                .prompt(p)
                .call()
                .content();
    }

    public String otherQuestionsRecommended(String q) {
        return chatClient
                .prompt(TPromptsService.getCachedTPromptsByMethodName("otherQuestionsRecommended"))
                .user(q)
                .call()
                .content();
    }

    public String optimizePromptWords(String q) {

        return chatClient.prompt(TPromptsService.getCachedTPromptsByMethodName("optimizePromptWords"))
                .user(q)
                .call()
                .content();
    }

    public String parameterMatching(String q) {
        return chatClient.prompt(TPromptsService.getCachedTPromptsByMethodName("parameterMatching"))
                .user(q)
                .call()
                .content();
    }


}
