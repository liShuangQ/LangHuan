package com.langhuan.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.langhuan.common.ApiLog
import com.langhuan.common.Result
import com.langhuan.model.domain.TRagFile
import com.langhuan.model.pojo.RagChangeDocumentsReq
import com.langhuan.model.pojo.RagDeleteDocumentsReq
import com.langhuan.model.pojo.RagWriteDocumentsReq
import com.langhuan.serviceai.RagCallBackService
import com.langhuan.serviceai.RagService
import com.langhuan.utils.rag.config.SplitConfig
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@RestController
class RagController(
    private val ragService: RagService,
    private val ragCallBackService: RagCallBackService
) {

    companion object {
        private val log = LoggerFactory.getLogger(RagController::class.java)
    }

    @PreAuthorize("hasAuthority('/rag/readAndSplitDocument')")
    @PostMapping("/rag/readAndSplitFileDocument")
    @Throws(Exception::class)
    fun readAndSplitFileDocument(
        file: MultipartFile,
        splitFileMethod: String,
        methodData: String
    ): Result<*> {
        /*
         * JSONObject jsonObject = JSONUtil.parseObj(methodData);
         * List<String> list = ragService.readAndSplitDocument(file, splitFileMethod,
         * jsonObject);
         * return Result.success(list);
         */
        val objectMapper = ObjectMapper()
        val methodDataMap: Map<String, Any>
        try {
            methodDataMap = objectMapper.readValue(methodData, object : TypeReference<Map<String, Any>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid methodData JSON format", e)
        }

        // 封装为 SplitConfig
        val config = SplitConfig(splitFileMethod, methodDataMap)

        // 调用 Service，保持变量名不变
        val list = ragService.readAndSplitDocument(file, config)
        return Result.success(list)
    }

    @PreAuthorize("hasAuthority('/rag/readAndSplitDocument')")
    @PostMapping("/rag/readAndSplitTextDocument")
    @Throws(Exception::class)
    fun readAndSplitTextDocument(
        text: String,
        splitFileMethod: String,
        methodData: String
    ): Result<*> {
        val objectMapper = ObjectMapper()
        val methodDataMap: Map<String, Any>
        try {
            methodDataMap = objectMapper.readValue(methodData, object : TypeReference<Map<String, Any>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid methodData JSON format", e)
        }

        // 封装为 SplitConfig
        val config = SplitConfig(splitFileMethod, methodDataMap)

        // 调用 Service，保持变量名不变
        val list = ragService.readAndSplitDocument(text, config)
        return Result.success(list)
    }

    @PreAuthorize("hasAuthority('/rag/readAndSplitDocument')")
    @PostMapping("/rag/readAndSplitHtmlDocument")
    @Throws(Exception::class)
    fun readAndSplitHtmlDocument(
        url: String,
        splitFileMethod: String,
        methodData: String
    ): Result<*> {
        val objectMapper = ObjectMapper()
        val methodDataMap: Map<String, Any>
        try {
            methodDataMap = objectMapper.readValue(methodData, object : TypeReference<Map<String, Any>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid methodData JSON format", e)
        }

        // 封装为 SplitConfig
        val config = SplitConfig(splitFileMethod, methodDataMap)

        // 调用 Service，保持变量名不变
        val list = ragService.readAndSplitDocument(url, config)
        return Result.success(list)
    }

    @ApiLog(apiName = "RAG写入向量库", description = "将文档写入向量库", logResponse = true, logRequest = false)
    @PreAuthorize("hasAuthority('/rag/writeDocumentsToVectorStore')")
    @PostMapping("/rag/writeDocumentsToVectorStore")
    @Throws(Exception::class)
    fun writeDocumentsToVectorStore(@RequestBody ragWriteDocumentsReq: RagWriteDocumentsReq): Result<Any> {
        ragWriteDocumentsReq.ragFile?.documentNum?.let {
            if (it.toInt() >= 2000) {
                return Result.error("切分块数量已达上限，最大单文件2000条")
            }
        }
        val fileGroupId = ragWriteDocumentsReq.ragFile?.fileGroupId
        if (fileGroupId.isNullOrEmpty()) {
            return Result.error("请选择文件组")
        }

        return Result.success(
            ragService.writeDocumentsToVectorStore(
                ragWriteDocumentsReq.documents as List<String>,
                ragWriteDocumentsReq.ragFile as TRagFile
            )
        )
    }

    @ApiLog(
        apiName = "RAG删除文件和文档",
        description = "根据文件ID删除文件和文档",
        logResponse = true,
        logRequest = true
    )
    @PreAuthorize("hasAuthority('/rag/deleteFileAndDocuments')")
    @PostMapping("/rag/deleteFileAndDocuments")
    fun deleteFileAndDocuments(@RequestParam(value = "id", required = true) fileId: Int): Result<*> {
        return Result.success(ragService.deleteFileAndDocuments(fileId))
    }

    @ApiLog(apiName = "RAG修改文件和文档", description = "修改文件和文档", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/changeFileAndDocuments')")
    @PostMapping("/rag/changeFileAndDocuments")
    fun changeFileAndDocuments(@Valid @RequestBody ragFile: TRagFile): Result<*> {
        return Result.success(ragService.changeFileAndDocuments(ragFile))
    }

    @PreAuthorize("hasAuthority('/rag/recallTesting')")
    @PostMapping("/rag/recallTesting")
    @Throws(Exception::class)
    fun recallTesting(
        @RequestParam(name = "q", required = true) q: String,
        @RequestParam(name = "groupId", required = true) groupId: String,
        @RequestParam(name = "fileId", required = true) fileId: String
    ): Result<*> {
        val out = mutableListOf<Map<String, Any>>()
        val documentList = ragCallBackService.ragSearch(q, groupId, fileId, false)
        for (document in documentList) {
            out.add(
                mapOf(
                    "id" to (document.id ?: ""),
                    "text" to (document.text ?: ""),
                    "metadata" to (document.metadata ?: emptyMap<String, Any>())
                )
            )
        }
        return Result.success(out)
    }

    @ApiLog(apiName = "RAG修改文档排序", description = "修改文档排序", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/changeDocumentsRank')")
    @PostMapping("/rag/changeDocumentsRank")
    fun changeDocumentsRank(
        @RequestParam(name = "id", required = true) id: String,
        @RequestParam(name = "rank", required = true) rank: Int
    ): Result<Any> {
        if (rank < 0 || rank > 100) {
            return Result.error("Rank需在0到100之间")
        }
        return Result.success(ragService.changeDocumentsRank(id, rank))
    }

    @ApiLog(apiName = "RAG修改文档文本", description = "修改文档文本", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/changeDocumentText')")
    @PostMapping("/rag/changeDocumentText")
    @Throws(Exception::class)
    fun changeDocumentText(@RequestBody ragChangeDocumentsReq: RagChangeDocumentsReq): Result<*> {
        return Result.success(
            ragService.changeDocumentText(
                ragChangeDocumentsReq.documents as String,
                ragChangeDocumentsReq.documentId as String,
                ragChangeDocumentsReq.ragFile as TRagFile
            )
        )
    }

    @ApiLog(apiName = "RAG删除文档文本", description = "删除文档文本", logResponse = true, logRequest = true)
    @PreAuthorize("hasAuthority('/rag/deleteDocumentText')")
    @PostMapping("/rag/deleteDocumentText")
    fun deleteDocumentText(@RequestBody ragDeleteDocumentsReq: RagDeleteDocumentsReq): Result<*> {
        return Result.success(
            ragService.deleteDocumentText(
                ragDeleteDocumentsReq.documentId as String,
                ragDeleteDocumentsReq.ragFile as TRagFile
            )
        )
    }

    @ApiLog(apiName = "RAG分享文档文本", description = "分享文档文本", logResponse = true, logRequest = true)
    @PostMapping("/rag/shareDocumentToOtherFile")
    @Throws(java.lang.Exception::class)
    fun shareDocumentToOtherFile(
        @RequestParam(name = "documentId", required = true) documentId: String?,
        @RequestParam(name = "documentText", required = true) documentText: String?,
        @RequestParam(name = "toGroupId", required = true) toGroupId: String?,
        @RequestParam(name = "toFileId", required = true) toFileId: String?,
        @RequestParam(name = "toFileName", required = true) toFileName: String?
    ): Result<String> {
        return Result.success(
            ragService.shareDocumentToOtherFile(
                documentId,
                documentText,
                toGroupId,
                toFileId,
                toFileName
            )
        )
    }
}
