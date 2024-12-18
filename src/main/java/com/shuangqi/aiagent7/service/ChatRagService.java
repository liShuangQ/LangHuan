package com.shuangqi.aiagent7.service;

import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.utils.MyTokenTextSplitter;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
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

@Service
public class ChatRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;


    public ChatRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {

        this.vectorStore = vectorStore;

//        VectorStoreChatMemoryAdvisor
//        new VectorStoreChatMemoryAdvisor(this.vectorStore, "1", 10, "", 1),
//        从 VectorStore 中检索内存，并将其添加到提示符的系统文本中。此 advisor 可用于从大型数据集中高效搜索和检索相关信息。

//        SafeGuardAdvisor
//        一个简单的 advisor，旨在防止模型生成有害或不适当的内容。
        this.chatClient = chatClientBuilder.defaultSystem(Constant.AIDEFAULTSYSTEMPROMPT)
                .defaultAdvisors(
                        // 此 advisor 使用向量存储来提供问答功能，实现 RAG（检索增强生成）模式。
                        new QuestionAnswerAdvisor(this.vectorStore, SearchRequest.defaults()),
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

    @SneakyThrows
    public String addVector(MultipartFile file) {
// 手动添加RAG
//        List<Document> documents = List.of(
//                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
//                new Document("The World is Big and Salvation Lurks Around the Corner hei hei hei"),
//                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

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
                .apply(documentText);
        // 存入向量数据库，这个过程会自动调用embeddingModel,将文本变成向量再存入。
        this.vectorStore.add(splitDocuments);
        return "添加成功";
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


}
