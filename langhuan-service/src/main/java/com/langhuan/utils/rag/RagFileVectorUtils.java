package com.langhuan.utils.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhuan.model.pojo.RagMetaData;
import com.langhuan.serviceai.ChatGeneralAssistanceService;
import com.langhuan.utils.rag.splitter.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.sentdetect.SentenceDetectorME;
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
//        if (splitFileMethod.equals("OpenNLPSentenceSplitter")) {
//            apply = new OpenNLPSentenceSplitter().apply(documentText);
//        }
        if (splitFileMethod.equals("LlmTextSplitter")) {
            apply = new LlmTextSplitter((Integer) methodData.get("windowSize"), (String) methodData.get("modelName"), chatGeneralAssistanceService).apply(documentText);
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
            List<List<Document>> documentsListBatch = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(metadata);
            Map<String, Object> personMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            try {
                for (int i = 0; i < documents.size(); i += 10) {
                    List<Document> documentsList = new ArrayList<>();
                    for (String document : documents.subList(i, Math.min(i + 10, documents.size()))) {
                        log.info("writeDocumentsToVectorStore documents.add {}", document);
                        documentsList.add(new Document(document, personMap));
                    }
                    documentsListBatch.add(documentsList);
                }
                for (List<Document> listBatch : documentsListBatch) {
                    vectorStore.add(listBatch);
                }
            } catch (Exception e) {
                log.error("writeDocumentsToVectorStore documentsList.add error", e);
            }
            return true;
        } catch (Exception e) {
            log.error("writeDocumentsToVectorStore error", e);
            return false;
        }
    }

}
