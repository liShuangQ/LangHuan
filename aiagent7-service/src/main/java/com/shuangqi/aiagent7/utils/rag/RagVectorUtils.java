package com.shuangqi.aiagent7.utils.rag;

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
public class RagVectorUtils {

    @SneakyThrows
    public String addRagVector(MultipartFile file, VectorStore vectorStore) {
        log.debug("向量库添加文件：" + file.getOriginalFilename());
        try {
            if (addTikaRagVector(file, vectorStore)) {
                return "文件：" + file.getOriginalFilename() + " 已添加到向量库";
            }
        } catch (Exception e) {
            log.error("文件：" + file.getOriginalFilename() + " 添加到向量库失败", e);
        }
        return "文件：" + file.getOriginalFilename() + " 添加到向量库失败";
    }

    @SneakyThrows
    public Boolean addTikaRagVector(MultipartFile file, VectorStore vectorStore) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        List<Document> documents = tikaDocumentReader.read();
        String documentText = joinDocumentContents(documents);
        List<Document> splitDocuments = splitDocumentText(documentText, file);
        vectorStore.add(splitDocuments);
        return true;
    }

    private String joinDocumentContents(List<Document> documents) {
        List<String> documentLines = new ArrayList<>();
        for (Document doc : documents) {
            documentLines.add(doc.getContent());
        }
        return String.join("\n", documentLines);
    }

    private List<Document> splitDocumentText(String documentText, MultipartFile file) {
        return new MyTokenTextSplitter()
                .apply(documentText, Map.of(
                        "filename", file.getOriginalFilename(),
                        "filetype", file.getContentType()));
    }
}
