package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.common.ApiLog;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.service.TRagFileService;
import com.langhuan.serviceai.RagService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
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

    @ApiLog(apiName = "RAG删除文件", description = "根据文件ID删除文件", logResponse = true, logRequest = true)
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
            @RequestParam(required = false) String fileGroupName,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        log.info("Querying file with fileName: {}, fileType: {},fileGroupName:{}, page: {}, size: {}", fileName,
                fileType,
                fileGroupName, pageNum, pageSize);
        
        // 处理空字符串参数
        fileName = (fileName != null && fileName.trim().isEmpty()) ? null : fileName;
        fileType = (fileType != null && fileType.trim().isEmpty()) ? null : fileType;
        fileGroupName = (fileGroupName != null && fileGroupName.trim().isEmpty()) ? null : fileGroupName;
        
        return Result.success(ragFileService.queryFiles(fileName, fileType, fileGroupName, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('/rag/file/queryDocumentsByFileId')")
    @PostMapping("/file/queryDocumentsByFileId")
    public Result queryDocumentsByFileId(
            @RequestParam(required = true) Integer fileId,
            @RequestParam String content,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        return Result.success(ragService.queryDocumentsByFileId(fileId, content, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('/rag/file/getFilesByGroupId')")
    @PostMapping("/file/getFilesByGroupId")
    public Result getFilesByGroupId(
            @RequestParam(required = true) String groupId) {
        return Result.success(ragFileService.list(
                new LambdaQueryWrapper<TRagFile>().eq(TRagFile::getFileGroupId, groupId)));
    }

    @PreAuthorize("hasAuthority('/rag/file/generateDocumentStreamByFileId')")
    @PostMapping("/file/generateDocumentStreamByFileId")
    public ResponseEntity<byte[]> generateDocumentStreamByFileId(@RequestParam(required = true) Integer fileId) {
        InputStreamResource resource = ragFileService.generateDocumentStreamByFileId(fileId);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain;charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=document.txt")
                    .body(bytes);
        } catch (IOException e) {
            log.error("Error reading input stream", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
