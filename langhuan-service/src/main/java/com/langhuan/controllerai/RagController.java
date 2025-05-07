package com.langhuan.controllerai;

import com.alibaba.fastjson2.JSONObject;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.pojo.RagChangeDocumentsReq;
import com.langhuan.model.pojo.RagDeleteDocumentsReq;
import com.langhuan.model.pojo.RagWriteDocumentsReq;
import com.langhuan.serviceai.RagService;
import org.springframework.ai.document.Document;
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

    @PostMapping("/rag/readAndSplitDocument")
    public Result readAndSplitDocument(
            MultipartFile file,
            String splitFileMethod,
            String methodData
    ) {
        JSONObject jsonObject = JSONObject.parseObject(methodData);
        List<String> list = ragService.readAndSplitDocument(file, splitFileMethod, jsonObject);
        return Result.success(list);
    }

    @PostMapping("/rag/writeDocumentsToVectorStore")
    public Result writeDocumentsToVectorStore(
            @RequestBody RagWriteDocumentsReq ragWriteDocumentsReq
    ) {
        return Result.success(ragService.writeDocumentsToVectorStore(ragWriteDocumentsReq.getDocuments(), ragWriteDocumentsReq.getRagFile()));

    }

    @PostMapping("/rag/deleteFileAndDocuments")
    public Result deleteFileAndDocuments(
            @RequestParam(value = "id", required = true) Integer fileId
    ) {
        return Result.success(ragService.deleteFileAndDocuments(fileId));
    }

    @PostMapping("/rag/changeFileAndDocuments")
    public Result changeFileAndDocuments(
            @Valid @RequestBody TRagFile ragFile
    ) {
        return Result.success(ragService.changeFileAndDocuments(ragFile));
    }

    @PostMapping("/rag/recallTesting")
    public Result recallTesting(
            @RequestParam(name = "q", required = true) String q,
            @RequestParam(name = "groupId", required = true) String groupId,
            @RequestParam(name = "fileId", required = true) String fileId
    ) {
        List<Map<String, Object>> out = new ArrayList<>();
        List<Document> documentList = ragService.ragSearch(q, groupId, fileId);
        for (Document document : documentList) {
            out.add(Map.of(
                    "id", document.getId(),
                    "text", document.getText(),
                    "metadata", document.getMetadata()
            ));
        }
        return Result.success(out);
    }

    @PostMapping("/rag/changeDocumentsRank")
    public Result changeDocumentsRank(
            @RequestParam(name = "id", required = true) String id,
            @RequestParam(name = "rank", required = true) int rank
    ) {
        return Result.success(ragService.changeDocumentsRank(id, rank));
    }

    @PostMapping("/rag/changeDocumentText")
    public Result changeDocumentText(
            @RequestBody RagChangeDocumentsReq ragChangeDocumentsReq
    ) {
        if (ragChangeDocumentsReq.getDocuments().size() != 1) {
            return Result.error("只能修改一条信息");
        }
        return Result.success(ragService.changeDocumentText(ragChangeDocumentsReq.getDocuments(),
                ragChangeDocumentsReq.getDocumentId(),
                ragChangeDocumentsReq.getRagFile()
        ));
    }

    @PostMapping("/rag/deleteDocumentText")
    public Result deleteDocumentText(
            @RequestBody RagDeleteDocumentsReq ragDeleteDocumentsReq
    ) {
        return Result.success(ragService.deleteDocumentText(
                ragDeleteDocumentsReq.getDocumentId(),
                ragDeleteDocumentsReq.getRagFile()
        ));
    }

}
