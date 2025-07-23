package com.langhuan.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langhuan.common.ApiLog;
import com.langhuan.common.Constant;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.pojo.RagChangeDocumentsReq;
import com.langhuan.model.pojo.RagDeleteDocumentsReq;
import com.langhuan.model.pojo.RagWriteDocumentsReq;
import com.langhuan.serviceai.RagService;
import com.langhuan.utils.rag.config.SplitConfig;
import org.springframework.ai.document.Document;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class RagController {
    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PreAuthorize("hasAuthority('/rag/readAndSplitDocument')")
    @PostMapping("/rag/readAndSplitDocument")
    public Result readAndSplitDocument(
            MultipartFile file,
            String splitFileMethod,
            String methodData) {
        /*JSONObject jsonObject = JSONUtil.parseObj(methodData);
        List<String> list = ragService.readAndSplitDocument(file, splitFileMethod, jsonObject);
        return Result.success(list);*/
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> methodDataMap;
        try {
            methodDataMap = objectMapper.readValue(methodData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid methodData JSON format", e);
        }

        // 封装为 SplitConfig
        SplitConfig config = new SplitConfig(splitFileMethod, methodDataMap);

        // 调用 Service，保持变量名不变
        List<String> list = ragService.readAndSplitDocument(file, config);
        return Result.success(list);
    }

    @ApiLog(apiName = "RAG写入向量库", description = "将文档写入向量库", logResponse = true, logRequest = false)
    @PreAuthorize("hasAuthority('/rag/writeDocumentsToVectorStore')")
    @PostMapping("/rag/writeDocumentsToVectorStore")
    public Result writeDocumentsToVectorStore(
            @RequestBody RagWriteDocumentsReq ragWriteDocumentsReq) throws Exception {
        if (Integer.parseInt(ragWriteDocumentsReq.getRagFile().getDocumentNum()) >= 1000) {
            return Result.error("切分数量已达上限");
        }

        return Result.success(ragService.writeDocumentsToVectorStore(ragWriteDocumentsReq.getDocuments(),
                ragWriteDocumentsReq.getRagFile()));

    }

    @ApiLog(apiName = "RAG删除文件和文档", description = "根据文件ID删除文件和文档", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/deleteFileAndDocuments')")
    @PostMapping("/rag/deleteFileAndDocuments")
    public Result deleteFileAndDocuments(
            @RequestParam(value = "id", required = true) Integer fileId) {
        return Result.success(ragService.deleteFileAndDocuments(fileId));
    }

    @ApiLog(apiName = "RAG修改文件和文档", description = "修改文件和文档", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/changeFileAndDocuments')")
    @PostMapping("/rag/changeFileAndDocuments")
    public Result changeFileAndDocuments(
            @Valid @RequestBody TRagFile ragFile) {
        return Result.success(ragService.changeFileAndDocuments(ragFile));
    }

    @PreAuthorize("hasAuthority('/rag/recallTesting')")
    @PostMapping("/rag/recallTesting")
    public Result recallTesting(
            @RequestParam(name = "q", required = true) String q,
            @RequestParam(name = "groupId", required = true) String groupId,
            @RequestParam(name = "fileId", required = true) String fileId) throws Exception {
        List<Map<String, Object>> out = new ArrayList<>();
        List<Document> documentList = ragService.ragSearch(q, groupId, fileId, Constant.ISRAGRERANK);
        for (Document document : documentList) {
            out.add(Map.of(
                    "id", document.getId(),
                    "text", document.getText(),
                    "metadata", document.getMetadata()));
        }
        return Result.success(out);
    }

    @ApiLog(apiName = "RAG修改文档排序", description = "修改文档排序", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/changeDocumentsRank')")
    @PostMapping("/rag/changeDocumentsRank")
    public Result changeDocumentsRank(
            @RequestParam(name = "id", required = true) String id,
            @RequestParam(name = "rank", required = true) int rank) {
        if (rank < 0 || rank > 100) {
            return Result.error("Rank需在0到100之间");
        }
        return Result.success(ragService.changeDocumentsRank(id, rank));
    }

    @ApiLog(apiName = "RAG修改文档文本", description = "修改文档文本", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/changeDocumentText')")
    @PostMapping("/rag/changeDocumentText")
    public Result changeDocumentText(
            @RequestBody RagChangeDocumentsReq ragChangeDocumentsReq) throws Exception {
        return Result.success(ragService.changeDocumentText(ragChangeDocumentsReq.getDocuments(),
                ragChangeDocumentsReq.getDocumentId(),
                ragChangeDocumentsReq.getRagFile()));
    }

    @ApiLog(apiName = "RAG删除文档文本", description = "删除文档文本", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/deleteDocumentText')")
    @PostMapping("/rag/deleteDocumentText")
    public Result deleteDocumentText(
            @RequestBody RagDeleteDocumentsReq ragDeleteDocumentsReq) {
        return Result.success(ragService.deleteDocumentText(
                ragDeleteDocumentsReq.getDocumentId(),
                ragDeleteDocumentsReq.getRagFile()));
    }

}
