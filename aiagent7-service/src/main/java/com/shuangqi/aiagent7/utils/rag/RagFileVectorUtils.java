package com.shuangqi.aiagent7.utils.rag;

import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.model.dao.RagMetaData;
import com.shuangqi.aiagent7.utils.rag.splitter.PatternTokenTextSplitter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RagFileVectorUtils {
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
    private Map<String, Object> readAndSplitDocument(MultipartFile file, String parentFileId, String splitFileMethod,
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

        RagMetaData ragMetaData = new RagMetaData();
        ragMetaData.setFilename(file.getOriginalFilename());
        ragMetaData.setFiletype(file.getContentType());
        ragMetaData.setParentFileId(parentFileId);
        List<String> apply = null;

        if (splitFileMethod.equals("PatternTokenTextSplitter")) {
            // Pattern pattern = Pattern.compile((String) methodData.get("pattern"));
            // Integer chunkSize = (Integer) methodData.get("chunkSize");
            // Integer chunkOverlap = (Integer) methodData.get("chunkOverlap");
            // PatternTokenTextSplitter patternTokenTextSplitter = new PatternTokenTextSplitter(pattern, chunkSize,
            //         chunkOverlap);
        }
        return Map.of(
                "documents", apply,
                "metadata", Map.of("parentFileId", ragMetaData));
    }

    /**
     * 将文档块添加到VectorStore中
     *
     * @param documents 分割后的文档块列表
     * @param metadata  元数据
     */
    private void writeDocumentsToVectorStore(List<String> documents, Map<String, Object> metadata,
            VectorStore vectorStore) {
        List<Document> documentsList = new ArrayList<>();
        for (String document : documents) {
            documentsList.add(new Document(document, metadata));
        }
        vectorStore.add(documentsList);
    }

}
