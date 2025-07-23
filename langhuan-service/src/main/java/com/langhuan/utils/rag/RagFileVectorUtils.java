package com.langhuan.utils.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhuan.common.Constant;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.pojo.RagMetaData;
import com.langhuan.serviceai.ChatGeneralAssistanceService;
import com.langhuan.utils.rag.config.SplitConfig;
import com.langhuan.utils.rag.factory.SplitterFactory;
import com.langhuan.utils.rag.splitter.TextSplitter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.langhuan.common.BusinessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

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
     * 创建元数据
     *
     * @param ragFile 文件信息
     * @return 元数据
     */
    public RagMetaData makeMateData(TRagFile ragFile) {
        RagMetaData metadata = new RagMetaData();
        metadata.setFilename(ragFile.getFileName());
        metadata.setFileId(String.valueOf(ragFile.getId()));
        metadata.setGroupId(ragFile.getFileGroupId());
        metadata.setRank(0);
        return metadata;
    }

    /**
     * 创建元数据
     *
     * @param fileName 文件名
     * @param fileId   文件ID
     * @param groupId  分组ID
     * @return 元数据
     */
    public RagMetaData makeMateData(String fileName, String fileId, String groupId) {
        RagMetaData metadata = new RagMetaData();
        metadata.setFilename(fileName);
        metadata.setFileId(fileId);
        metadata.setGroupId(groupId);
        metadata.setRank(0);
        return metadata;
    }

    /**
     * 读取并分割文档
     *
     * @param file 要解析的文件
     * @return 分割后的文档块列表
     */
    @SneakyThrows
    public List<String> readAndSplitDocument(MultipartFile file, SplitConfig splitConfig) {
        // 使用TikaDocumentReader读取文件内容
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        List<Document> documents = tikaDocumentReader.read();

        // 合并所有文档内容为一个字符串
        /*List<String> documentLines = new ArrayList<>();
        for (Document doc : documents) {
            documentLines.add(doc.getFormattedContent());
        }
        String documentText = String.join("\n", documentLines);

        System.out.println("documentText: " + documentText);

        // 移除Tika读取时产生的无效源URI信息
        if (documentText != null && documentText.contains("source: Invalid source URI")) {
            documentText = documentText.replaceAll(
                    "(?m)^\\s*source: Invalid source URI: InputStream resource \\[resource loaded through InputStream\\] cannot be resolved to URL\\s*$",
                    "").trim();
        }*/
        StringBuilder documentTextBuilder = new StringBuilder();
        for (Document doc : documents) {
            documentTextBuilder.append(doc.getFormattedContent()).append("\n");
        }
        String documentText = documentTextBuilder.toString();

        if (documentText.contains("source: Invalid source URI")) {
            documentText = documentText.replaceAll(
                    "(?m)^\\s*source: Invalid source URI: InputStream resource \\[resource loaded through InputStream\\] cannot be resolved to URL\\s*$",
                    "").trim();
        }


        /*List<String> apply = null;

        // TODO 当大文档时候的性能优化
        if (splitFileMethod.equals("FixedWindowTextSplitter")) {
            apply = new FixedWindowTextSplitter((Integer) methodData.get("windowSize")).apply(documentText);
        }
        if (splitFileMethod.equals("PatternTokenTextSplitter")) {
            apply = new PatternTokenTextSplitter(Pattern.compile((String) methodData.get("splitPattern")))
                    .apply(documentText);
        }
        // if (splitFileMethod.equals("SlidingWindowTextSplitter")) {
        // apply = new SlidingWindowTextSplitter((Integer) methodData.get("windowSize"),
        // (Integer) methodData.get("overlapSize")).apply(documentText);
        // }
        // if (splitFileMethod.equals("OpenNLPSentenceSplitter")) {
        // apply = new OpenNLPSentenceSplitter().apply(documentText);
        // }
        if (splitFileMethod.equals("LlmTextSplitter")) {
            apply = new LlmTextSplitter((Integer) methodData.get("windowSize"), (String) methodData.get("modelName"),
                    chatGeneralAssistanceService).apply(documentText);
        }

        return apply;*/
        TextSplitter splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService);
        return splitter.apply(documentText);
    }

    /**
     * 将文档块添加到VectorStore中
     * ¬
     *
     * @param documents 分割后的文档块列表
     * @param metadata  元数据
     * @throws BusinessException
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
                throw new BusinessException(e.getMessage());
            }
            return true;
        } catch (Exception e) {
            log.error("writeDocumentsToVectorStore error", e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 生成文档流
     * 
     * @param documents 分割后的文档块列表
     * @return 文档内容输入流资源
     */
    @SneakyThrows
    public InputStreamResource generateDocumentStreamByFileId(List<String> documentContents) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            for (String content : documentContents) {
                writer.write(content);
                writer.write(System.lineSeparator());
                writer.write(Constant.DEFAULT_RAG_EXPORT_SPLIT);
                writer.write(System.lineSeparator());
            }
            writer.flush();
        }
        return new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray()));
    }

}
