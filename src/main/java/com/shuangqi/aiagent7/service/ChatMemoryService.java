package com.shuangqi.aiagent7.service;

import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.model.pojo.MyChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
@Slf4j
public class ChatMemoryService {

    private final ChatClient chatClient;

    public ChatMemoryService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(Constant.AIDEFAULTSYSTEMPROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new MyChatMemory()),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    /**
     * 带有记忆的聊天
     *
     * @param id
     * @param q
     * @return
     */
    public String chat(String id, String q) {
        return this.chatClient.prompt()
                .user(q)
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                ).call().chatResponse().getResult().getOutput().getContent();
    }

    /**
     * 带有记忆的聊天-流式返回
     *
     * @param id
     * @param q
     * @return
     */
    public Flux<String> chatStream(String id, String q) {
        return this.chatClient.prompt()
                .user(q)
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                ).stream().content();
    }

    /**
     * 有提示词带有记忆的聊天
     *
     * @param id
     * @param p
     * @param q
     * @return
     */
    public String chatWithPrompt(String id, String p, String q) {
        return this.chatClient.prompt(p)
                .user(q)
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                ).call().chatResponse().getResult().getOutput().getContent();
    }

    /**
     * 有提示词带有记忆的聊天-流式返回
     *
     * @param id
     * @param p
     * @param q
     * @return
     */
    public Flux<String> chatWithPromptStream(String id, String p, String q) {
        return this.chatClient.prompt(p)
                .user(q)
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                ).stream().content();
    }

    /**
     * 清除记忆
     *
     * @param id
     * @return
     */
    public String clear(String id) {
        log.info("advisor-clear: {}", "用户id-" + id);
        new MyChatMemory().clear(id);
        return "清除成功";
    }
}
