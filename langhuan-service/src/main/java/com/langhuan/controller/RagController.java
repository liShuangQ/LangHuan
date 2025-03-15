package com.langhuan.controller;

import com.alibaba.fastjson2.JSONObject;
import com.langhuan.common.Result;
import com.langhuan.model.pojo.RagWriteDocumentsReq;
import com.langhuan.serviceai.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {
    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/readAndSplitDocument")
    public Result readAndSplitDocument(
            MultipartFile file,
            String splitFileMethod,
            String methodData
    ) {
        JSONObject jsonObject = JSONObject.parseObject(methodData);
        List<String> list = ragService.readAndSplitDocument(file, splitFileMethod, jsonObject);
        return Result.success(list);
    }

    @PostMapping("/writeDocumentsToVectorStore")
    public Result writeDocumentsToVectorStore(
            @RequestBody RagWriteDocumentsReq ragWriteDocumentsReq
    ) {
        return Result.success(ragService.writeDocumentsToVectorStore(ragWriteDocumentsReq.getDocuments(), ragWriteDocumentsReq.getRagFile()));

    }

    @PostMapping("/deleteFileAndDocuments")
    public Result deleteFileAndDocuments(
            @RequestParam(value = "id", required = true) Integer fileId
    ) {
        return Result.success(ragService.deleteFileAndDocuments(fileId));

    }
}
