package com.langhuan.serviceai;

import com.langhuan.utils.rag.RagFileVectorUtils;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final RagFileVectorUtils ragFileVectorUtils;
    private final VectorStore vectorStore;

    public RagService(RagFileVectorUtils ragFileVectorUtils, VectorStore vectorStore) {
        this.ragFileVectorUtils = ragFileVectorUtils;
        this.vectorStore = vectorStore;
    }

    public List<String> readAndSplitDocument(MultipartFile file, String splitFileMethod,
                                             Map<String, Object> methodData) {
        return ragFileVectorUtils.readAndSplitDocument(file, splitFileMethod, methodData);
    }

    public String writeDocumentsToVectorStore(List<String> documents, Map<String, Object> metadata) {
        if (ragFileVectorUtils.writeDocumentsToVectorStore(documents, metadata, vectorStore)) {
            return "添加成功";
        } else {
            return "添加失败，请检查日志。";
        }
    }
}
