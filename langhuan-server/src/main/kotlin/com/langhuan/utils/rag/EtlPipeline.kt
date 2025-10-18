package com.langhuan.utils.rag

import com.langhuan.serviceai.ChatGeneralAssistanceService
import com.langhuan.utils.rag.config.SplitConfig
import com.langhuan.utils.rag.extractor.DocumentExtractor
import com.langhuan.utils.rag.factory.SplitterFactory
import com.langhuan.utils.rag.loader.VectorStoreLoader
import com.langhuan.utils.rag.metadata.RagMetadataFactory
import com.langhuan.utils.rag.splitter.TextSplitter
import com.langhuan.utils.rag.transformer.TextTransformer
import com.langhuan.utils.rag.util.DocumentStreamUtils
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Etl总逻辑
 */
@Component
class EtlPipeline(
    private val documentExtractor: DocumentExtractor,
    private val textTransformer: TextTransformer,
    private val vectorStoreLoader: VectorStoreLoader,
    val metadataFactory: RagMetadataFactory,
    private val documentStreamUtils: DocumentStreamUtils,
    private val chatGeneralAssistanceService: ChatGeneralAssistanceService
) {

    @Throws(Exception::class)
    fun process(file: MultipartFile, splitConfig: SplitConfig): List<String> {
        val rawText = documentExtractor.extract(file)
        val splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService)
        return textTransformer.transform(rawText, splitter)
    }

    fun process(text: String, splitConfig: SplitConfig, isText: Boolean): List<String> {
        val splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService)
        return textTransformer.transform(text, splitter)
    }

    @Throws(Exception::class)
    fun process(url: String, splitConfig: SplitConfig): List<String> {
        val rawText = documentExtractor.extract(url)
        val splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService)
        return textTransformer.transform(rawText, splitter)
    }

    fun writeToVectorStore(chunks: List<String>, metadata: Map<String, Any>, vectorStore: VectorStore): Boolean {
        return vectorStoreLoader.load(chunks, metadata, vectorStore)
    }

    fun exportChunks(chunks: List<String>): InputStreamResource {
        return documentStreamUtils.generateStream(chunks)
    }
}
