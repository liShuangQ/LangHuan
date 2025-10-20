package com.langhuan.utils.rag.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhuan.common.BusinessException;
import com.langhuan.serviceai.ChatGeneralAssistanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Afish
 * @date 2025/7/23 13:47
 */
@Slf4j
@Component
public class VectorStoreLoader {

    public boolean load(List<String> documents, Map<String, Object> metadata, VectorStore vectorStore) {
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
}
