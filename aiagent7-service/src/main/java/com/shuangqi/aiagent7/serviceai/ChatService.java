package com.shuangqi.aiagent7.serviceai;

import com.alibaba.fastjson.JSONObject;
import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.BusinessException;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.utils.rag.RagVectorUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ApplicationContext applicationContext;
    private final InMemoryChatMemory inMemoryChatMemory;
    private final String chatMemoryRetrieveSizeKey = "7";
    private final RagVectorUtils ragVectorUtils;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, ApplicationContext applicationContext, RagVectorUtils ragVectorUtils) {
        this.ragVectorUtils = ragVectorUtils;
        this.inMemoryChatMemory = new InMemoryChatMemory();
        this.vectorStore = vectorStore;
        this.applicationContext = applicationContext;
//        用合适的美观的html格式的字符串的形式回复，当字符串中存在双引号的时候使用单引号替代。
        this.chatClient = chatClientBuilder.defaultSystem("""
                        用户会向你提出一个问题，你的任务是提供一个细致且准确的答案。
                         以JSON格式返回。
                         确保你的回答遵循以下结构：
                         {
                            "desc": "回复内容"
                         }
                        """)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(inMemoryChatMemory),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
                .defaultToolContext(
                        Map.of("type", "chat")
                )
                .build();
    }

    public String chat(String id, String p, String q, Boolean isRag, String groupId, Boolean isFunction, String modelName) {
        String[] funcs = isFunction ? Arrays.stream(applicationContext.getBeanNamesForType(BiFunction.class))
                .filter(name -> name.startsWith("chat_"))
                .toArray(String[]::new) : new String[0];
        try {
            if (isRag) {
                QuestionAnswerAdvisor questionAnswerAdvisor = groupId.isEmpty()
                        ? new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.defaults().withTopK(Constant.WITHTOPK)
                                .withSimilarityThreshold(Constant.WITHSIMILARITYTHRESHOLD), Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT)
                        : new QuestionAnswerAdvisor(vectorStore,
                        SearchRequest.defaults().withTopK(Constant.WITHTOPK)
                                .withFilterExpression("groupId == " + groupId)
                                .withSimilarityThreshold(Constant.WITHSIMILARITYTHRESHOLD), Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT);
                return this.chatClient.prompt(
                                new Prompt(
                                        p,
                                        OpenAiChatOptions.builder()
                                                .withModel(modelName)
                                                .build()
                                )
                        )
                        .user(q)
                        .advisors(questionAnswerAdvisor)
                        .advisors(
                                a -> a
                                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemoryRetrieveSizeKey)
                        )
                        .functions(funcs)
                        .call().chatResponse().getResult().getOutput().getContent();
            } else {
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
                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemoryRetrieveSizeKey)
                        )
                        .functions(funcs)
                        .call().chatResponse().getResult().getOutput().getContent();
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


    @SneakyThrows
    public String addRagVector(MultipartFile file, String parentFileId) {
        return ragVectorUtils.addRagFileVector(file, vectorStore, parentFileId);
    }

    public String ragSearch(String q, String groupId) {
        List<Document> searchDocuments = null;
        if (groupId.isEmpty()) {
            searchDocuments = vectorStore.similaritySearch(
                    SearchRequest.defaults()
                            .withQuery(q)
                            .withTopK(5) // 单独设置多一些
                            .withSimilarityThreshold(0.5));
        } else {
            searchDocuments = vectorStore.similaritySearch(
                    SearchRequest.defaults()
                            .withQuery(q)
                            .withTopK(5)
                            .withSimilarityThreshold(0.5)
                            .withFilterExpression("groupId == " + groupId));//设置过滤条件
        }
        log.debug("ragSearch: " + searchDocuments);
        StringBuilder contents = new StringBuilder();
        int i = 0;
        for (Document document : searchDocuments) {
            i += 1;
            contents.append("<p>").append(i).append(":").append("&nbsp;").append(document.getContent()).append("</p>");
        }
        JSONObject json = new JSONObject();
        json.put("desc", contents.toString());
        return json.toString();
    }

}
