package com.shuangqi.aiagent7.service;

import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.utils.MyTokenTextSplitter;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;


    public ChatRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.defaultSystem(Constant.AIDEFAULTSYSTEMPROMPT)
                .defaultAdvisors(
                        // 此 advisor 使用向量存储来提供问答功能，实现 RAG（检索增强生成）模式。
                        new QuestionAnswerAdvisor(this.vectorStore, SearchRequest.defaults()),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor()

                ).defaultAdvisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 100))
                .build();

    }

    public String chat(String q) {
        return this.chatClient.prompt()
                .user(q)
                .call()
                .content();
    }

    public Flux<String> stream(String q) {
        return chatClient.prompt()
                .user(q)
                .stream()
                .content();
    }

    public ChatResponse chatWithPrompt(String p, String q) {
        return chatClient.prompt(new Prompt(p))
                .user(q)
                .call()
                .chatResponse();
    }

    @SneakyThrows
    public String addVector(MultipartFile file) {
        // 从IO流中读取文件
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        // 将文本内容合并成一个单一的字符串
        List<Document> documents = tikaDocumentReader.read();
        List<String> documentLines = new ArrayList<>();
        for (Document doc : documents) {
            documentLines.add(doc.getContent());
        }
        String documentText = String.join("\n", documentLines); // 使用换行符连接
        // 将文本内容划分成更小的块
        List<Document> splitDocuments = new MyTokenTextSplitter()
                .apply(documentText, Map.of(
                        "filename", file.getOriginalFilename(),
                        "filetype", file.getContentType())); //创建元数据映射
        // 存入向量数据库，这个过程会自动调用embeddingModel,将文本变成向量再存入。
        this.vectorStore.add(splitDocuments);
        return "添加成功";
    }


}
