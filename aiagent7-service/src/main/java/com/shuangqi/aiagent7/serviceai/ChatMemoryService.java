package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.model.pojo.MyChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
@Slf4j
public class ChatMemoryService {

    private final ChatClient chatClient;
//    private MyChatMemory myChatMemory;
    private InMemoryChatMemory inMemoryChatMemory;

    public ChatMemoryService(ChatClient.Builder chatClientBuilder) {
//        MyChatMemory myChatMemory = new MyChatMemory();
        InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();

        this.inMemoryChatMemory = inMemoryChatMemory;
        this.chatClient = chatClientBuilder.defaultSystem(Constant.AIDEFAULTSYSTEMPROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(inMemoryChatMemory), // 检索内存并将其作为消息集合添加到提示符中。此方法维护会话历史记录的结构。请注意，并非所有 AI 模型都支持此方法。
                        // PromptChatMemoryAdvisor：检索内存并将其添加到提示的系统文本中
                        // VectorStoreChatMemoryAdvisor：从 VectorStore 中检索内存，并将其添加到提示符的系统文本中。此 advisor 可用于从大型数据集中高效搜索和检索相关信息。主要侧重于利用向量存储（Vector Store）来管理聊天记忆。
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
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
                        a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
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
//        this.myChatMemory.clear(id);
        this.inMemoryChatMemory.clear(id);
        return "清除成功";
    }
}
