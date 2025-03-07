package com.shuangqi.aiagent7.utils.rag;

import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.model.dao.RagMetaData;
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

@Slf4j
@Component
public class RagFileVectorUtils {
    private List<String> splitFileMethod() {
        return List.of("PatternTokenTextSplitter", "OpenNLPSentenceSplitter", "FixedWindowTextSplitter", "SlidingWindowTextSplitter");
    }

    /**
     * 读取并分割文档
     *
     * @param file 要解析的文件
     * @return 分割后的文档块列表
     */
    @SneakyThrows
    private Map<String, Object> readAndSplitDocument(MultipartFile file, String parentFileId, String splitFileMethod) {
        // 使用TikaDocumentReader读取文件内容
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        List<Document> documents = tikaDocumentReader.read();

        // 合并所有文档内容为一个字符串
        List<String> documentLines = new ArrayList<>();
        for (Document doc : documents) {
            documentLines.add(doc.getFormattedContent());
        }
        String documentText = String.join("\n", documentLines);

        RagMetaData ragMetaData = new RagMetaData();
        ragMetaData.setFilename(file.getOriginalFilename());
        ragMetaData.setFiletype(file.getContentType());
        ragMetaData.setParentFileId(parentFileId);
        List<String> apply = null;
        // TODO
        if (splitFileMethod.equals("PatternTokenTextSplitter")) {
            apply = new PatternTokenTextSplitter(Constant.DEFAULT_RAG_SPLIT_PATTERN).apply(documentText);
        }
        // TODO
        if (splitFileMethod.equals("FixedWindowTextSplitter")) {
            apply = new PatternTokenTextSplitter(Constant.DEFAULT_RAG_SPLIT_PATTERN).apply(documentText);
        }
        // TODO
        if (splitFileMethod.equals("SlidingWindowTextSplitter")) {
            apply = new PatternTokenTextSplitter(Constant.DEFAULT_RAG_SPLIT_PATTERN).apply(documentText);
        }
        return Map.of(
                "documents", apply,
                "metadata", Map.of("parentFileId", ragMetaData)
        );
    }

    /**
     * 将文档块添加到VectorStore中
     *
     * @param documents 分割后的文档块列表
     * @param metadata  元数据
     */
    private void writeDocumentsToVectorStore(List<String> documents, Map<String, Object> metadata, VectorStore vectorStore) {
        List<Document> documentsList = new ArrayList<>();
        for (String document : documents) {
            documentsList.add(new Document(document, metadata));
        }
        vectorStore.add(documentsList);
    }

}
