package com.langhuan.serviceai

import cn.hutool.core.util.IdUtil
import cn.hutool.json.JSONUtil
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.langhuan.common.BusinessException
import com.langhuan.common.Constant
import com.langhuan.config.VectorStoreConfig
import com.langhuan.dao.TFileUrlDao
import com.langhuan.dao.VectorStoreRagDao
import com.langhuan.model.domain.TFileUrl
import com.langhuan.model.domain.TRagFile
import com.langhuan.model.domain.TRagFileGroup
import com.langhuan.service.*
import com.langhuan.utils.other.SecurityUtils
import com.langhuan.utils.rag.EtlPipeline
import com.langhuan.utils.rag.config.SplitConfig
import jakarta.annotation.Resource
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class RagService(
    vectorStoreConfig: VectorStoreConfig,
    private val ragFileService: TRagFileService,
    private val ragFileGroupService: TRagFileGroupService,
    private val etlPipeline: EtlPipeline,
    private val tFileUrlService: TFileUrlService,
    private val vectorStoreRagDao: VectorStoreRagDao,
    private val tFileUrlDao: TFileUrlDao,
    private val ragCallBackService: RagCallBackService
) {

    companion object {
        private val log = LoggerFactory.getLogger(RagService::class.java)
    }

    private val ragVectorStore: VectorStore = vectorStoreConfig.ragVectorStore()

    @Value("\${minio.img-bucket-name}")
    private var bucketName: String? = null

    @Resource
    private var cacheService: CacheService? = null

    @Resource
    private var minioService: MinioService? = null

    @Transactional(rollbackFor = [Exception::class])
    @Throws(Exception::class)
    fun changeDocumentText(documents: String, documentId: String, ragFile: TRagFile): String {
        log.info("Updating changeDocumentText: {}, documentId: {}, documents: {}", ragFile, documentId, documents)
        vectorStoreRagDao.deleteByDocumentId(documentId)
        return if (etlPipeline.writeToVectorStore(
                listOf(documents), etlPipeline.metadataFactory.createMetadata(ragFile) as Map<String, Any>,
                ragVectorStore
            )
        ) {
            "更新成功"
        } else {
            "更新失败，请检查日志。"
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @Throws(Exception::class)
    fun changeDocumentTextByString(documents: String, documentId: String): String {
        log.info("Updating changeDocumentTextByString: {}, documentId: {}, documents: {}", documentId, documents)

        val queryForList: List<Map<String, Any>> = vectorStoreRagDao.selectByDocumentId(documentId)
        val pgObject = queryForList[0]["metadata"] as PGobject
        val string = pgObject.value // 提取 JSON 字符串
        val `object` = JSONUtil.parseObj(string)
        val fileName = `object`.getStr("filename")
        val fileId = `object`.getStr("fileId")
        val groupId = `object`.getStr("groupId")

        vectorStoreRagDao.deleteByDocumentId(documentId)
        return if (etlPipeline.writeToVectorStore(
                listOf(documents),
                etlPipeline.metadataFactory.createMetadata(fileName, fileId, groupId),
                ragVectorStore
            )
        ) {
            "更新成功"
        } else {
            "更新失败，请检查日志。"
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteDocumentText(documentId: String, ragFile: TRagFile): String {
        log.info("Deleting document text: {}, documentId: {}", ragFile, documentId)

        vectorStoreRagDao.deleteByDocumentId(documentId)

        val fileUrlList = tFileUrlDao.selectByFileId(ragFile.id as Int)

        if (fileUrlList.isNotEmpty()) {
            deleteImage(fileUrlList)

            val deleteCount = tFileUrlDao.deleteByFileId(ragFile.id as Int)
            log.info("Deleted {} records from t_file_url", deleteCount)
        }

        val newDocNum = ragFile.documentNum?.let { maxOf(0, it.toInt() - 1) }
        ragFile.documentNum = newDocNum.toString()
        val updated = ragFileService.updateById(ragFile)
        if (!updated) {
            log.warn("Failed to update ragFile: {}", ragFile.id)
            throw RuntimeException("更新文件记录失败")
        }

        return "删除成功"
    }

    @Throws(Exception::class)
    fun readAndSplitDocument(file: MultipartFile, splitConfig: SplitConfig): List<String> {
        return etlPipeline.process(file, splitConfig)
    }

    @Throws(Exception::class)
    fun readAndSplitDocument(url: String, splitConfig: SplitConfig): List<String> {
        return if (url.startsWith("http")) {
            etlPipeline.process(url, splitConfig)
        } else {
            etlPipeline.process(url, splitConfig, false)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @Throws(Exception::class)
    fun writeDocumentsToVectorStore(documents: List<String>, ragFile: TRagFile): String {
        log.info("writeDocumentsToVectorStore: {}", ragFile)

        val isInsert = ragFile.id == null || ragFile.id == 0 || ragFile.id.toString().isEmpty()
        if (isInsert) {
            ragFile.id = IdUtil.getSnowflakeNextId().toInt()
        }
        ragFile.uploadedAt = Date()
        ragFile.uploadedBy = SecurityUtils.getCurrentUsername()

        // 写入向量库
        val writeSuccess = etlPipeline.writeToVectorStore(
            documents,
            etlPipeline.metadataFactory.createMetadata(ragFile) as Map<String, Any>,
            ragVectorStore
        )

        if (!writeSuccess) {
            return "添加失败，请检查日志。"
        }

        try {
            // 1. 从缓存中获取 extract 阶段生成的临时 fileId
            val tempFileId = cacheService?.getId(SecurityUtils.getCurrentUsername() + Constant.CACHE_KEY)
            if (tempFileId == null) {
                log.info("纯文本添加 或 未找到缓存的临时file_id，跳过t_file_url更新")
            } else {
                // 2. 使用 SQL 批量更新 t_file_url 表
                val updatedRows = tFileUrlDao.updateFileIdAndStatus(
                    ragFile.id as Int, // 新的 file_id
                    "在用", // 新状态
                    tempFileId, // 旧的临时 file_id
                    "临时" // 原状态
                )

                log.info("批量更新 t_file_url，影响行数: {}, file_id: {} -> {}", updatedRows, tempFileId, ragFile.id)

                // 3. 清理缓存
                cacheService?.removeId(SecurityUtils.getCurrentUsername() + Constant.CACHE_KEY)
            }

        } catch (e: Exception) {
            log.error("执行 t_file_url 批量更新时出错", e)
            throw Exception("更新图片引用状态失败", e) // 触发事务回滚
        }

        // 4. 保存或更新 ragFile 记录
        if (isInsert) {
            ragFileService.save(ragFile)
            log.info("保存新文件记录: {}", ragFile.id)
        } else {
            ragFileService.updateById(ragFile)
            log.info("更新已有文件记录: {}", ragFile.id)
        }

        return "添加成功"
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteFileAndDocuments(id: Int): Boolean {
        log.info("Deleting file and documents with ID: {}", id)

        val deletedRowsFromVectorStore = vectorStoreRagDao.deleteByFileId(id.toString())
        log.info("Deleted {} rows from vector_store_rag", deletedRowsFromVectorStore)

        val fileUrlList = tFileUrlService.query()
            .eq("fileId", id)
            .select("fUrl", "id") // 只选择需要的字段
            .list()

        if (fileUrlList.isNotEmpty()) {
            deleteImage(fileUrlList)

            val idsToDelete = fileUrlList.map { it.id }
            val deletedFromTFileUrl = tFileUrlService.removeByIds(idsToDelete)
            if (!deletedFromTFileUrl) {
                log.warn("Failed to delete records from t_file_url")
                throw RuntimeException("删除 t_file_url 记录失败")
            }
            log.info("Deleted {} records from t_file_url", idsToDelete.size)
        }

        val deletedRagFile = ragFileService.removeById(id)
        if (!deletedRagFile) {
            log.warn("Failed to delete TRagFile record with ID: {}", id)
            throw RuntimeException("删除 TRagFile 记录失败")
        }

        return true
    }

    private fun deleteImage(fileUrlList: List<TFileUrl>) {
        val urlsToDelete = fileUrlList.map { it.fUrl }
            .filter { url: String? -> url != null && url.trim().isNotEmpty() }

        for (url in urlsToDelete) {
            try {
                val objectName = minioService?.extractObjectName(url as String, bucketName as String) // 使用之前的提取方法
                if (objectName != null) {
                    minioService?.handleDelete(objectName, bucketName as String)
                    log.info("MinIO object deleted: {}", objectName)
                }
            } catch (e: Exception) {
                log.error("Failed to delete MinIO object: {}", url, e)
                throw RuntimeException("删除 MinIO 文件失败: $url", e) // 触发事务回滚
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun changeFileAndDocuments(ragFile: TRagFile): String {
        log.info("Updating changeFileAndDocuments: {}", ragFile)
        vectorStoreRagDao.updateGroupIdByFileId(ragFile.fileGroupId as String, ragFile.id.toString())
        ragFileService.updateById(ragFile)
        return "更新成功"
    }

    fun changeDocumentsRank(documentId: String, rank: Int): String {
        log.info("Updating changeDocumentsRank: {} rank: {}", documentId, rank)
        vectorStoreRagDao.updateRankByDocumentId(documentId, rank)
        return "更新成功"
    }

    fun queryDocumentsByFileId(fileId: Int?, content: String, pageNum: Int, pageSize: Int): Map<String, Any> {
        if (fileId == null) {
            throw BusinessException("fileId不能为空")
        }
        log.info("queryDocumentsByFileId: {}", fileId)
        return mapOf(
            "list" to vectorStoreRagDao.selectByFileIdAndContent(
                fileId.toString(), content, pageSize,
                (pageNum - 1) * pageSize
            ),
            "count" to vectorStoreRagDao.countByFileIdAndContent(fileId.toString(), content)
        )
    }

    fun queryDocumentsByIds(fileIds: String): List<Map<String, Any>> {
        val fileIdsArray = fileIds.split(",").toTypedArray()
        if (fileIdsArray.isEmpty()) {
            throw BusinessException("fileId不能为空")
        }
        if (fileIdsArray.size > 10) {
            throw BusinessException("fileId数量不能超过10")
        }

        val documentIds = listOf(*fileIdsArray)
        // 执行查询
        log.info("queryDocumentsByFileIds: {}", fileIds)
        val queryForList = vectorStoreRagDao.selectByIds(documentIds)
        return queryForList
    }

    @Throws(Exception::class)
    fun addDocumentToMySpace(documents: List<String>): Boolean {
        val user = SecurityUtils.getCurrentUsername()
        // HACK 和前端约定的知识空间文件组名称
        val setFileGroupName = Constant.DEFAULTFILEGROUPNAME.replace("\${user}", user as String)
        val setFileName = Constant.DEFAULTFILEGROUPFILE.replace("\${user}", user)
        var ragFile = TRagFile()

        val fileGroupList = ragFileGroupService
            .list(QueryWrapper<TRagFileGroup>().eq("group_name", setFileGroupName))

        if (fileGroupList.isEmpty()) {
            val ragFileGroup = TRagFileGroup()
            ragFileGroup.groupName = setFileGroupName
            ragFileGroup.groupType = "个人知识空间"
            ragFileGroup.groupDesc = "对话知识空间生成"
            ragFileGroup.createdBy = user
            ragFileGroup.visibility = "private"
            ragFileGroupService.save(ragFileGroup)
            val fileGroup = ragFileGroupService.getOne(
                QueryWrapper<TRagFileGroup>()
                    .eq("group_name", setFileGroupName)
            )
            ragFile.fileGroupId = fileGroup.id.toString()
        } else {
            // 如果文件组已存在，使用现有文件组的ID
            val existingGroup = fileGroupList[0]
            ragFile.fileGroupId = existingGroup.id.toString()
        }

        // 文件操作
        val fileList = ragFileService.list(QueryWrapper<TRagFile>().eq("file_name", setFileName))
        if (fileList.isEmpty()) {
            ragFile.fileName = setFileName
            ragFile.fileType = "对话知识空间"
            ragFile.fileSize = "无"
            ragFile.documentNum = documents.size.toString()
            ragFile.fileDesc = "$setFileName。在对话中产生。"
            // fileGroupId已经在上面设置过了
            ragFile.uploadedBy = user
            ragFileService.save(ragFile)
        } else {
            val first = fileList.first()
            first.documentNum = first.documentNum?.let { (it.toInt() + documents.size).toString() }
            ragFile = first
            ragFileService.updateById(first)
        }
        val s = writeDocumentsToVectorStore(documents, ragFile)
        return s.contains("成功")
    }

}
