package com.langhuan.utils.rag

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.langhuan.common.BusinessException
import com.langhuan.common.Constant
import com.langhuan.model.domain.TRagFile
import com.langhuan.model.pojo.RagMetaData
import com.langhuan.serviceai.ChatGeneralAssistanceService
import com.langhuan.utils.rag.config.SplitConfig
import com.langhuan.utils.rag.factory.SplitterFactory
import com.langhuan.utils.rag.splitter.TextSplitter
import lombok.SneakyThrows
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

@Component
class RagFileVectorUtils(
    private val chatGeneralAssistanceService: ChatGeneralAssistanceService
) {

    companion object {
        private val log = LoggerFactory.getLogger(RagFileVectorUtils::class.java)
    }

    private fun splitFileMethod(): List<String> {
        return listOf("PatternTokenTextSplitter", "OpenNLPSentenceSplitter", "FixedWindowTextSplitter",
                "SlidingWindowTextSplitter")
    }

    /**
     * 创建元数据
     *
     * @param ragFile 文件信息
     * @return 元数据
     */
    fun makeMateData(ragFile: TRagFile): RagMetaData {
        val metadata = RagMetaData()
        metadata.filename = ragFile.fileName
        metadata.fileId = ragFile.id.toString()
        metadata.groupId = ragFile.fileGroupId
        metadata.rank = 0
        return metadata
    }

    /**
     * 创建元数据
     *
     * @param fileName 文件名
     * @param fileId   文件ID
     * @param groupId  分组ID
     * @return 元数据
     */
    fun makeMateData(fileName: String, fileId: String, groupId: String): RagMetaData {
        val metadata = RagMetaData()
        metadata.filename = fileName
        metadata.fileId = fileId
        metadata.groupId = groupId
        metadata.rank = 0
        return metadata
    }

    /**
     * 读取并分割文档
     *
     * @param file 要解析的文件
     * @return 分割后的文档块列表
     */
    @SneakyThrows
    fun readAndSplitDocument(file: MultipartFile, splitConfig: SplitConfig): List<String> {
        // 使用TikaDocumentReader读取文件内容
        val tikaDocumentReader = TikaDocumentReader(InputStreamResource(file.inputStream))
        val documents = tikaDocumentReader.read()

        // 合并所有文档内容为一个字符串
        val documentTextBuilder = StringBuilder()
        for (doc in documents) {
            documentTextBuilder.append(doc.formattedContent).append("\n")
        }
        var documentText = documentTextBuilder.toString()

        if (documentText.contains("source: Invalid source URI")) {
            documentText = documentText.replace(
                    "(?m)^\\s*source: Invalid source URI: InputStream resource \\[resource loaded through InputStream\\] cannot be resolved to URL\\s*$",
                    "").trim()
        }

        val splitter = SplitterFactory.createSplitter(splitConfig, chatGeneralAssistanceService)
        return splitter.apply(documentText)
    }

    /**
     * 将文档块添加到VectorStore中
     * ¬
     *
     * @param documents 分割后的文档块列表
     * @param metadata  元数据
     * @throws BusinessException
     */
    fun writeDocumentsToVectorStore(documents: List<String>, metadata: RagMetaData,
                                   vectorStore: VectorStore): Boolean {
        try {
            val documentsListBatch = mutableListOf<List<Document>>()
            val objectMapper = ObjectMapper()
            val json = objectMapper.writeValueAsString(metadata)
            val personMap: Map<String, Any> = objectMapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
            
            try {
                for (i in documents.indices step 10) {
                    val documentsList = mutableListOf<Document>()
                    for (document in documents.subList(i, minOf(i + 10, documents.size))) {
                        log.info("writeDocumentsToVectorStore documents.add {}", document)
                        documentsList.add(Document(document, personMap))
                    }
                    documentsListBatch.add(documentsList)
                }
                for (listBatch in documentsListBatch) {
                    vectorStore.add(listBatch)
                }
            } catch (e: Exception) {
                log.error("writeDocumentsToVectorStore documentsList.add error", e)
                throw BusinessException(e.message ?: "")
            }
            return true
        } catch (e: Exception) {
            log.error("writeDocumentsToVectorStore error", e)
            throw BusinessException(e.message ?: "")
        }
    }

    /**
     * 生成文档流
     * 
     * @param documents 分割后的文档块列表
     * @return 文档内容输入流资源
     */
    @SneakyThrows
    fun generateDocumentStreamByFileId(documentContents: List<String>): InputStreamResource {
        val outputStream = ByteArrayOutputStream()
        OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
            for (content in documentContents) {
                writer.write(content)
                writer.write(System.lineSeparator())
                writer.write(Constant.DEFAULT_RAG_EXPORT_SPLIT)
                writer.write(System.lineSeparator())
            }
            writer.flush()
        }
        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }
}
