package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.BusinessException;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.service.TPromptsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Service
public class StanfordChatService {

    private final ChatClient chatClient;
    private final InMemoryChatMemory inMemoryChatMemory;


    public StanfordChatService(ChatClient.Builder chatClientBuilder, ChatClient chatClient, VectorStore vectorStore, ApplicationContext applicationContext) {
        this.inMemoryChatMemory = new InMemoryChatMemory();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(inMemoryChatMemory),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
                .build();
    }

    public String chat(String id, String p, String q, String modelName) {
        try {
            return this.chatClient.prompt(
                            new Prompt(
                                    p,
                                    OpenAiChatOptions.builder()
                                            .model(modelName)
                                            .build()
                            )
                    )
                    .user(q)
                    .system(TPromptsService.getCachedTPromptsByMethodName("StanfordChatService"))
                    .advisors(
                            a -> a
                                    .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 200)
                    )
                    .call().chatResponse().getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

}
