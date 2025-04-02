package com.langhuan.controllerai;

import com.alibaba.fastjson2.JSONObject;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.pojo.RagWriteDocumentsReq;
import com.langhuan.serviceai.RagService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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
        List<Document> documentList = ragService.ragSearch(q, groupId, fileId);
        StringBuilder contents = new StringBuilder();
        int i = 0;
        for (Document document : documentList) {
            i += 1;
            contents.append("<p>").append(i).append(":").append("&nbsp;").append(document.getText()).append("</p>");
        }
        return Result.success(contents.toString());
    }

}
