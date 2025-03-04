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
import org.springframework.ai.model.Content;
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
//                       你是一个高级对话管理系统的一部分，负责协调多个具有独特个性的AI角色之间的互动。
//                       你的目标是确保每一次对话都是连贯的、有意义的，并能够反映各角色的个性特征。
//                       所有角色都应遵循基本的礼貌原则，尊重彼此的观点，并致力于构建一个积极、富有建设性的对话环境。
        this.chatClient = chatClientBuilder.defaultSystem("""
                        在接下来的对话中，你将作为一个独特的个体参与交流。
                        你将与其他几位同样有着鲜明个性的角色一起，就各种话题展开深入讨论。
                        请结合上下文，基于你的背景和性格，积极贡献你的见解，同时也要认真倾听他人的观点。
                        无论讨论的主题是什么，请确保你的发言既真实反映你的角色特质，又能促进一场有意义的对话，同时注意对话不要脱离主题。
                        你需要只是针对当前角色的角度去说话。在回答中不要说明你是谁，不要重复说明当前背景和个性。
                        不要重复说上面已说的观点，每次回答不超过200字。
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
                                            .model(modelName)
                                            .build()
                            )
                    )
                    .user(q)
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
