package com.langhuan.utils.rag;

import com.langhuan.serviceai.ChatGeneralAssistanceService;
import com.langhuan.utils.rag.config.SplitConfig;
import com.langhuan.utils.rag.extractor.DocumentExtractor;
import com.langhuan.utils.rag.factory.SplitterFactory;
import com.langhuan.utils.rag.loader.VectorStoreLoader;
import com.langhuan.utils.rag.metadata.RagMetadataFactory;
import com.langhuan.utils.rag.splitter.TextSplitter;
import com.langhuan.utils.rag.transformer.TextTransformer;
import com.langhuan.utils.rag.util.DocumentStreamUtils;
import lombok.Getter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Etl总逻辑
 */
@Component
public class EtlPipeline {
    private final DocumentExtractor documentExtractor;
    private final TextTransformer textTransformer;
    private final VectorStoreLoader vectorStoreLoader;
    @Getter
    private final RagMetadataFactory metadataFactory;
    private final DocumentStreamUtils documentStreamUtils;
    private final ChatGeneralAssistanceService chatGeneralAssistanceService;

    public EtlPipeline(
            DocumentExtractor documentExtractor,
            TextTransformer textTransformer,
            VectorStoreLoader vectorStoreLoader,
            RagMetadataFactory metadataFactory,
            DocumentStreamUtils documentStreamUtils,
            ChatGeneralAssistanceService chatGeneralAssistanceService) {
        this.documentExtractor = documentExtractor;
        this.textTransformer = textTransformer;
        this.vectorStoreLoader = vectorStoreLoader;
        this.metadataFactory = metadataFactory;
        this.documentStreamUtils = documentStreamUtils;
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
    }

    public List<String> process(MultipartFile file, SplitConfig splitConfig) {
        String rawText = documentExtractor.extract(file);
        TextSplitter splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService);
        return textTransformer.transform(rawText, splitter);
    }

    public List<String> process(String text, SplitConfig splitConfig, Boolean isText) {
        TextSplitter splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService);
        return textTransformer.transform(text, splitter);
    }

    public List<String> process(String url, SplitConfig splitConfig) throws Exception {
        String rawText = documentExtractor.extract(url);
        TextSplitter splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService);
        return textTransformer.transform(rawText, splitter);
    }

    public boolean writeToVectorStore(List<String> chunks, Map<String, Object> metadata, VectorStore vectorStore) {
        return vectorStoreLoader.load(chunks, metadata, vectorStore);
    }

    public InputStreamResource exportChunks(List<String> chunks) {
        return documentStreamUtils.generateStream(chunks);
    }
}
