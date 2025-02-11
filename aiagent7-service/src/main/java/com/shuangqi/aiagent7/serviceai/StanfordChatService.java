package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.BusinessException;
import com.shuangqi.aiagent7.common.Constant;
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
//        用合适的美观的html格式的字符串的形式回复，当字符串中存在双引号的时候使用单引号替代。
        this.chatClient = chatClientBuilder.defaultSystem("""
                       你是一个高级对话管理系统的一部分，负责协调多个具有独特个性的AI角色之间的互动。
                       你的目标是确保每一次对话都是连贯的、有意义的，并能够反映各角色的个性特征。
                       所有角色都应遵循基本的礼貌原则，尊重彼此的观点，并致力于构建一个积极、富有建设性的对话环境。
                       你需要只是针对当前角色的角度去说话。每次的回复不用太多，大约100 200字即可。
                        """)
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
                                            .withModel(modelName)
                                            .build()
                            )
                    )
                    .user(q)
                    .advisors(
                            a -> a
                                    .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                    )
                    .call().chatResponse().getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

}
