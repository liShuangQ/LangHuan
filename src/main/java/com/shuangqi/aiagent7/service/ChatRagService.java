package com.shuangqi.aiagent7.service;

import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.utils.RagVectorUtils;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@Service
public class ChatRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RagVectorUtils ragVectorUtils;


    public ChatRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, RagVectorUtils ragVectorUtils) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.defaultSystem(Constant.AIDEFAULTSYSTEMPROMPT)
                .defaultAdvisors(
                        // 此 advisor 使用向量存储来提供问答功能，实现 RAG（检索增强生成）模式。
                        new QuestionAnswerAdvisor(this.vectorStore, SearchRequest.defaults()),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor()

                )
                .build();

        this.ragVectorUtils = ragVectorUtils;
    }

    public String chat(String q) {
        return this.chatClient.prompt()
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 100))
                .user(q)
                .call()
                .content();
    }

    public Flux<String> stream(String q) {
        return chatClient.prompt()
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 100))
                .user(q)
                .stream()
                .content();
    }

    public ChatResponse chatWithPrompt(String p, String q) {
        return chatClient.prompt(new Prompt(p))
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 100))
                .user(q)
                .call()
                .chatResponse();
    }

    @SneakyThrows
    public String addRagVector(MultipartFile file) {
        return ragVectorUtils.addRagVector(file, vectorStore);
    }

}
