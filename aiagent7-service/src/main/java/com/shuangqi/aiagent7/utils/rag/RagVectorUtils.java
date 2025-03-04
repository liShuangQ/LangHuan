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
    public String addRagFileVector(MultipartFile file, VectorStore vectorStore) {
        log.debug("向量库添加文件：" + file.getOriginalFilename());
        try {
            if (addTikaRagVector(file, vectorStore, "all")) {
                return "文件：" + file.getOriginalFilename() + " 已添加到向量库";
            }
        } catch (Exception e) {
            log.error("文件：" + file.getOriginalFilename() + " 添加到向量库失败", e);
        }
        return "文件：" + file.getOriginalFilename() + " 添加到向量库失败";
    }

    @SneakyThrows
    public String addRagFileVector(MultipartFile file, VectorStore vectorStore, String parentFileId) {
        log.debug("向量库添加文件：" + file.getOriginalFilename());
        try {
            if (addTikaRagVector(file, vectorStore, parentFileId)) {
                return "文件：" + file.getOriginalFilename() + " 已添加到向量库";
            }
        } catch (Exception e) {
            log.error("文件：" + file.getOriginalFilename() + " 添加到向量库失败", e);
        }
        return "文件：" + file.getOriginalFilename() + " 添加到向量库失败";
    }

    /**
     * 向VectorStore中添加Tika解析的文档向量
     * 该方法首先使用Tika解析上传的文件，然后将解析的文档内容分割成小块，并将这些文档块添加到VectorStore中
     *
     * @param file         要解析的文件
     * @param vectorStore  存储文档向量的向量存储
     * @param parentFileId 父文件ID，用于关联文档块与其原始文件
     * @return 总是返回true，表示操作完成
     */
    @SneakyThrows
    public Boolean addTikaRagVector(MultipartFile file, VectorStore vectorStore, String parentFileId) {
        // 使用TikaDocumentReader读取文件内容
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        List<Document> documents = tikaDocumentReader.read();

        // 合并所有文档内容为一个字符串
        String documentText = joinDocumentContents(documents);

        // 分割文档文本为多个文档块，并附加文件信息
        List<Document> splitDocuments = splitDocumentText(documentText, Map.of(
                        "filename", file.getOriginalFilename(),
                        "filetype", file.getContentType(),
                        "parentFileId", parentFileId
                )
        );

        // 将文档块添加到VectorStore中
        vectorStore.add(splitDocuments);

        // 表示操作完成
        return true;
    }

    /**
     * 合并文档内容
     * 将多个文档的内容合并为一个字符串，每个文档内容之间用换行符分隔
     *
     * @param documents 要合并的文档列表
     * @return 合并后的文档内容字符串
     */
    private String joinDocumentContents(List<Document> documents) {
        List<String> documentLines = new ArrayList<>();
        for (Document doc : documents) {
            documentLines.add(doc.getFormattedContent());
        }
        return String.join("\n", documentLines);
    }

    /**
     * 分割文档文本
     * 使用文本分割器将文档文本分割成多个文档块，并附加额外的信息
     *
     * @param documentText 要分割的文档文本
     * @param map          包含额外信息的映射，如文件名、文件类型和父文件ID
     * @return 分割后的文档块列表
     */
    private List<Document> splitDocumentText(String documentText, Map<String, Object> map) {
        return new PatternTokenTextSplitter()
                .apply(documentText, map);
    }
}
