package com.langhuan.utils.rag.loader

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.langhuan.common.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Component

/**
 * @author Afish
 * @date 2025/7/23 13:47
 */
@Component
class VectorStoreLoader {

    companion object {
        private val log = LoggerFactory.getLogger(VectorStoreLoader::class.java)
    }

    fun load(documents: List<String>, metadata: Map<String, Any>, vectorStore: VectorStore): Boolean {
        try {
            val documentsListBatch = mutableListOf<List<Document>>()
            val objectMapper = ObjectMapper()
            val json = objectMapper.writeValueAsString(metadata)
            val personMap: Map<String, Any> = objectMapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
            
            try {
                for (i in documents.indices step 10) {
                    val documentsList = mutableListOf<Document>()
                    for (document in documents.subList(i, minOf(i + 10, documents.size))) {
                        log.info("writeDocumentsToVectorStore documents.add {}", document)
                        documentsList.add(Document(document, personMap))
                    }
                    documentsListBatch.add(documentsList)
                }
                for (listBatch in documentsListBatch) {
                    vectorStore.add(listBatch)
                }
            } catch (e: Exception) {
                log.error("writeDocumentsToVectorStore documentsList.add error", e)
                throw BusinessException(e.message ?: "")
            }
            return true
        } catch (e: Exception) {
            log.error("writeDocumentsToVectorStore error", e)
            throw BusinessException(e.message ?: "")
        }
    }
}
