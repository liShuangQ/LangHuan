package com.langhuan.utils.rag;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhuan.config.VectorStoreConfig;
import com.langhuan.model.pojo.RagMetaData;
import com.langhuan.serviceai.ChatGeneralAssistanceService;
import com.langhuan.utils.rag.splitter.FixedWindowTextSplitter;
import com.langhuan.utils.rag.splitter.PatternTokenTextSplitter;
import com.langhuan.utils.rag.splitter.SlidingWindowTextSplitter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RagFileVectorUtils {
    private final ChatGeneralAssistanceService chatGeneralAssistanceService;

    public RagFileVectorUtils(ChatGeneralAssistanceService chatGeneralAssistanceService) {
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
    }

    private List<String> splitFileMethod() {
        return List.of("PatternTokenTextSplitter", "OpenNLPSentenceSplitter", "FixedWindowTextSplitter",
                "SlidingWindowTextSplitter");
    }

    /**
     * 读取并分割文档
     *
     * @param file 要解析的文件
     * @return 分割后的文档块列表
     */
    @SneakyThrows
    public List<String> readAndSplitDocument(MultipartFile file, String splitFileMethod,
                                             Map<String, Object> methodData) {
        // 使用TikaDocumentReader读取文件内容
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        List<Document> documents = tikaDocumentReader.read();

        // 合并所有文档内容为一个字符串
        List<String> documentLines = new ArrayList<>();
        for (Document doc : documents) {
            documentLines.add(doc.getFormattedContent());
        }
        String documentText = String.join("\n", documentLines);

        List<String> apply = null;

        // TODO 当大文档时候的性能优化
        if (splitFileMethod.equals("FixedWindowTextSplitter")) {
            apply = new FixedWindowTextSplitter((Integer) methodData.get("windowSize")).apply(documentText);
        }
        if (splitFileMethod.equals("PatternTokenTextSplitter")) {
            apply = new PatternTokenTextSplitter(Pattern.compile((String) methodData.get("splitPattern"))).apply(documentText);
        }
//        if (splitFileMethod.equals("SlidingWindowTextSplitter")) {
//            apply = new SlidingWindowTextSplitter((Integer) methodData.get("windowSize"), (Integer) methodData.get("overlapSize")).apply(documentText);
//        }
        if (splitFileMethod.equals("LlmTextSplitter")) {
            String llmOutString = this.chatGeneralAssistanceService.llmTextSplitter((String) methodData.get("modelName"), documentText);
            JSONArray content = JSONObject.parseObject(llmOutString).getJSONArray("content");
            apply = content.stream()
                    .map(Object::toString)
                    .toList();
        }

        return apply;
    }

    /**
     * 将文档块添加到VectorStore中
     * ¬
     *
     * @param documents 分割后的文档块列表
     * @param metadata  元数据
     */
    public Boolean writeDocumentsToVectorStore(List<String> documents, RagMetaData metadata,
                                               VectorStore vectorStore) {
        try {
            List<Document> documentsList = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(metadata);
            Map<String, Object> personMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            try {
                for (String document : documents) {
                    documentsList.add(new Document(document, personMap));
                }
            } catch (Exception e) {
                log.error("writeDocumentsToVectorStore documentsList.add error", e);
            }
            vectorStore.add(documentsList);
            return true;
        } catch (Exception e) {
            log.error("writeDocumentsToVectorStore error", e);
            return false;
        }
    }

}
