package com.langhuan.controller;

import com.alibaba.fastjson2.JSONObject;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.pojo.RagWriteDocumentsReq;
import com.langhuan.service.TRagFileService;
import com.langhuan.serviceai.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rag")
public class RagFileController {
    private final TRagFileService ragFileService;
    private final RagService ragService;
    public RagFileController(TRagFileService ragFileService, RagService ragService) {
        this.ragFileService = ragFileService;
        this.ragService = ragService;
    }


    @PostMapping("/file/delete")
    public Result deleteFile(@RequestParam Long id) {
        log.info("Deleting file with ID: {}", id);
        boolean success = ragFileService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }


    @PostMapping("/file/query")
    public Result queryFiles(@RequestParam(required = false) String fileName,
                             @RequestParam(required = false) String fileType,
                             @RequestParam(required = false) String fileGroupId,
                             @RequestParam int pageNum,
                             @RequestParam int pageSize) {
        log.info("Querying file with fileName: {}, fileType: {},fileGroupId:{}, page: {}, size: {}", fileName, fileType,fileGroupId, pageNum, pageSize);
        return Result.success(ragFileService.queryFiles(fileName, fileType, fileGroupId,pageNum, pageSize));
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

    @PostMapping("/changeFileAndDocuments")
    public Result changeFileAndDocuments(
            @Valid @RequestBody TRagFile ragFile
    ) {
        return Result.success(ragService.changeFileAndDocuments(ragFile));
    }
}
