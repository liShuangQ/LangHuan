package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.service.TRagFileService;
import com.langhuan.serviceai.RagService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PreAuthorize("hasAuthority('/rag/file/delete')")
    @PostMapping("/file/delete")
    public Result deleteFile(@RequestParam Long id) {
        log.info("Deleting file with ID: {}", id);
        boolean success = ragFileService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PreAuthorize("hasAuthority('/rag/file/query')")
    @PostMapping("/file/query")
    public Result queryFiles(@RequestParam(required = false) String fileName,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String fileGroupId,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        log.info("Querying file with fileName: {}, fileType: {},fileGroupId:{}, page: {}, size: {}", fileName, fileType,
                fileGroupId, pageNum, pageSize);
        return Result.success(ragFileService.queryFiles(fileName, fileType, fileGroupId, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('/rag/file/queryDocumentsByFileId')")
    @PostMapping("/file/queryDocumentsByFileId")
    public Result queryDocumentsByFileId(@RequestParam(required = true) Integer fileId) {
        return Result.success(ragService.queryDocumentsByFileId(fileId));
    }

    @PreAuthorize("hasAuthority('/rag/file/getFilesByGroupId')")
    @PostMapping("/file/getFilesByGroupId")
    public Result getFilesByGroupId(
            @RequestParam(required = true) String groupId) {
        return Result.success(ragFileService.list(
                new LambdaQueryWrapper<TRagFile>().eq(TRagFile::getFileGroupId, groupId)));
    }

}
