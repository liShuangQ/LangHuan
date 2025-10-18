package com.langhuan.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.langhuan.common.ApiLog
import com.langhuan.common.Result
import com.langhuan.model.domain.TRagFile
import com.langhuan.service.TRagFileService
import com.langhuan.serviceai.RagService
import com.langhuan.utils.other.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

import java.io.IOException

@RestController
@RequestMapping("/rag")
class RagFileController(
    private val ragFileService: TRagFileService,
    private val ragService: RagService
) {

    companion object {
        private val log = LoggerFactory.getLogger(RagFileController::class.java)
    }

    @ApiLog(apiName = "RAG删除文件", description = "根据文件ID删除文件", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/file/delete')")
    @PostMapping("/file/delete")
    fun deleteFile(@RequestParam id: Long): Result<String> {
        log.info("Deleting file with ID: {}", id)
        val success = ragFileService.removeById(id)
        return if (success) Result.success("删除成功") else Result.error("删除失败")
    }

    // @PreAuthorize("hasAuthority('/rag/file/query')")
    @PostMapping("/file/query")
    fun queryFiles(
        @RequestParam(required = false) fileName: String?,
        @RequestParam(required = false) fileType: String?,
        @RequestParam(required = false) fileGroupName: String?,
        @RequestParam pageNum: Int,
        @RequestParam pageSize: Int
    ): Result<*> {
        log.info("Querying file with fileName: {}, fileType: {},fileGroupName:{}, page: {}, size: {}", fileName,
            fileType,
            fileGroupName, pageNum, pageSize)

        // 处理空字符串参数
        val processedFileName = if (fileName?.isBlank() == true) null else fileName
        val processedFileType = if (fileType?.isBlank() == true) null else fileType
        val processedFileGroupName = if (fileGroupName?.isBlank() == true) null else fileGroupName

        println(SecurityUtils.getCurrentUserRoles())
        return Result.success(ragFileService.queryFiles(processedFileName, processedFileType, processedFileGroupName, pageNum, pageSize))
    }

    // @PreAuthorize("hasAuthority('/rag/file/queryDocumentsByFileId')")
    @PostMapping("/file/queryDocumentsByFileId")
    fun queryDocumentsByFileId(
        @RequestParam(required = true) fileId: Int,
        @RequestParam content: String,
        @RequestParam pageNum: Int,
        @RequestParam pageSize: Int
    ): Result<*> {
        return Result.success(ragService.queryDocumentsByFileId(fileId, content, pageNum, pageSize))
    }

    @PreAuthorize("hasAuthority('/rag/file/getFilesByGroupId')")
    @PostMapping("/file/getFilesByGroupId")
    fun getFilesByGroupId(@RequestParam(required = true) groupId: String): Result<*> {
        return Result.success(ragFileService.list(
            QueryWrapper<TRagFile>().eq("fileGroupId", groupId)
        ))
    }

    @PreAuthorize("hasAuthority('/rag/file/generateDocumentStreamByFileId')")
    @PostMapping("/file/generateDocumentStreamByFileId")
    fun generateDocumentStreamByFileId(@RequestParam(required = true) fileId: Int): ResponseEntity<ByteArray> {
        val resource = ragFileService.generateDocumentStreamByFileId(fileId)
        return try {
            val bytes = resource.inputStream.readAllBytes()
            ResponseEntity.ok()
                .header("Content-Type", "text/plain;charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=document.txt")
                .body(bytes)
        } catch (e: IOException) {
            log.error("Error reading input stream", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
