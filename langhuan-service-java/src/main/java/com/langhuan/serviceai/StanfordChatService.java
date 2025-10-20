package com.langhuan.serviceai;

import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.service.TPromptsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StanfordChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    @Autowired
    ChatMemoryRepository chatMemoryRepository;

    public StanfordChatService(ChatClient.Builder chatClientBuilder) {
        chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public String chat(String id, String p, String q, String modelName) {
        try {
            return this.chatClient.prompt(
                    new Prompt(
                            p,
                            OpenAiChatOptions.builder()
                                    .model(modelName)
                                    .build()))
                    .user(q)
                    .advisors(a -> a.param(chatMemory.CONVERSATION_ID, id))
                    .system(TPromptsService.getCachedTPromptsByMethodName("StanfordChatService"))
                    .call().chatResponse().getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

}
