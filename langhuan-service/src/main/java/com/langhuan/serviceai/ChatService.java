package com.langhuan.serviceai;

import com.langhuan.advisors.MySimplelogAdvisor;
import com.langhuan.functionTools.DateTimeToolsD;
import com.langhuan.functionTools.FileReadTools;
import com.langhuan.utils.rag.RagFileVectorUtils;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.service.TPromptsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final InMemoryChatMemory inMemoryChatMemory;
    private final RagService ragService;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, RagService ragService) {
        this.ragService = ragService;
        this.inMemoryChatMemory = new InMemoryChatMemory();
        this.vectorStore = vectorStore;
//        用合适的美观的html格式的字符串的形式回复，当字符串中存在双引号的时候使用单引号替代。
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(inMemoryChatMemory),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
                .build();

    }

    public String chat(String id, String p, String q, Boolean isRag, String groupId, Boolean isFunction, String modelName, int chatMemoryRetrieveSize) {
        ToolCallback[] tools = isFunction ? ToolCallbacks.from(new DateTimeToolsD(), new FileReadTools()) :
                ToolCallbacks.from();
        try {
            if (isRag) {
                QuestionAnswerAdvisor questionAnswerAdvisor = groupId.isEmpty()
                        ? new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.builder().topK(Constant.WITHTOPK)
                                .similarityThreshold(Constant.WITHSIMILARITYTHRESHOLD).build(), Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT)
                        : new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.builder().topK(Constant.WITHTOPK)
                                .filterExpression("groupId == '" + groupId + "'")//设置过滤条件
                                .similarityThreshold(Constant.WITHSIMILARITYTHRESHOLD).build(), Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT);
                return this.chatClient.prompt(
                                new Prompt(
                                        p,
                                        OpenAiChatOptions.builder()
                                                .model(modelName)
                                                .build()
                                )
                        )
                        .user(q)
                        .system(TPromptsService.getCachedTPromptsByMethodName("ChatService"))
                        .advisors(questionAnswerAdvisor)
                        .advisors(
                                a -> a
                                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemoryRetrieveSize)
                        )
                        .tools(tools)
                        .call().content();
                //chatResponse().getResult().getOutput().getText()
            } else {
                return this.chatClient.prompt(
                                new Prompt(
                                        p,
                                        OpenAiChatOptions.builder()
                                                .model(modelName)
                                                .build()
                                )
                        )
                        .user(q)
                        .system(TPromptsService.getCachedTPromptsByMethodName("ChatService"))
                        .advisors(
                                a -> a
                                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemoryRetrieveSize)
                        )
                        .tools(tools)
                        .call().content();
            }

        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

    public String clearChatMemory(String id) {
        log.info("advisor-clear: {}", "用户id-" + id);
        this.inMemoryChatMemory.clear(id);
        return "清除成功";
    }


    public String ragSearch(String q, String groupId) {
        return ragService.ragSearch(q, groupId, "");
    }

}
