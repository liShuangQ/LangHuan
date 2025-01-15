package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.BusinessException;
import com.shuangqi.aiagent7.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final InMemoryChatMemory inMemoryChatMemory;
    private final ApplicationContext applicationContext;
    private final String chatMemoryRetrieveSizeKey = "10";

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();

        this.inMemoryChatMemory = inMemoryChatMemory;

        String defaultSystem = """
                用户会向你提出一个问题，你的任务是提供一个细致且准确的答案。
                回答的内容，请用合适的美观的html格式的字符串的形式回复，当字符串中存在双引号的时候使用单引号替代。
                """;

        this.chatClient = chatClientBuilder.defaultSystem(defaultSystem)
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(vectorStore,
                                SearchRequest.defaults().withTopK(Constant.WITHTOPK)
                                        .withSimilarityThreshold(Constant.WITHSIMILARITYTHRESHOLD), Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT),
                        new MessageChatMemoryAdvisor(inMemoryChatMemory),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
//                .defaultFunctions(
//                        Arrays.stream(applicationContext.getBeanNamesForType(BiFunction.class))
//                                .filter(name -> name.startsWith("chat_"))
//                                .toArray(String[]::new)
//                )
                .build();
    }

    public ChatClient getRoleChatClient() {
        return this.chatClient;
    }

    public String chat(String id, String p, String q) {
        try {
            return this.getRoleChatClient().prompt(p)
                    .user(q)
                    .advisors(
                            a -> a
                                    .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemoryRetrieveSizeKey)
                    ).call().chatResponse().getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

    public Flux<String> chatFlux(String id, String p, String q) {
        try {
            return this.getRoleChatClient().prompt(p)
                    .user(q)
                    .advisors(
                            a -> a
                                    .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemoryRetrieveSizeKey)
                    ).stream()
                    .content();
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }


    public String clear(String id) {
        log.info("advisor-clear: {}", "用户id-" + id);
        this.inMemoryChatMemory.clear(id);
        return "清除成功";
    }

}
