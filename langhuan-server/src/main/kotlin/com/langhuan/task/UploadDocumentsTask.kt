package com.langhuan.task

import com.langhuan.model.domain.TRagFile
import com.langhuan.model.domain.TRagFileGroup
import com.langhuan.service.MinioService
import com.langhuan.service.TRagFileGroupService
import com.langhuan.service.TRagFileService
import jakarta.annotation.PostConstruct
import jakarta.annotation.Resource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.time.LocalDate

@Component
class UploadDocumentsTask {
    
    companion object {
        private val log = LoggerFactory.getLogger(UploadDocumentsTask::class.java)
    }

    @Value("\${minio.file-bucket-name}")
    private lateinit var bucketName: String

    @Value("\${minio.url}")
    private lateinit var minioUrl: String

    private lateinit var publicBaseUrl: String

    @Resource
    private lateinit var ragFileService: TRagFileService

    @Resource
    private lateinit var ragFileGroupService: TRagFileGroupService

    @Resource
    private lateinit var minioService: MinioService

    @PostConstruct
    fun init() {
        // 确保 bucket 存在
        try {
            minioService.ensureBucketExists(bucketName)
        } catch (e: Exception) {
            log.error("Failed to ensure bucket exists", e)
        }

        publicBaseUrl = minioUrl + "/" + bucketName
        if (!publicBaseUrl.endsWith("/")) {
            publicBaseUrl += "/"
        }
    }

    /**
     * 定时任务：每隔七天凌晨 2:00 执行
     */
    @Scheduled(cron = "\${task.schedule.documents}")
    fun exportAndUploadDocuments() {
        log.info("【定时任务】开始执行文档导出并上传至 MinIO...")

        // 1. 获取当前日期作为文件夹名
        val folderName = LocalDate.now().toString()

        try {
            // 2. 查询所有文件组，获取文件组ID和名称映射
            val fileGroups = ragFileGroupService.list()
            val groupMap = fileGroups.associateBy { it.id }.mapValues { it.value.groupName }

            if (groupMap.isEmpty()) {
                log.warn("未找到任何文件组，跳过上传任务。")
                return
            }

            log.info("共找到 {} 个文件组", groupMap.size)

            // 3. 查询所有有效文件
            val allFiles = ragFileService.list()
            log.info("共找到 {} 个文件", allFiles.size)

            for (file in allFiles) {
                val fileGroupId = file.fileGroupId?.toInt()
                var fileName = file.fileName
                val fileId = file.id

                if (fileName.isNullOrBlank()) {
                    fileName = "未知文件" // 默认名
                }

                // 去除扩展名（如果包含）
                val baseName = if (fileName.contains(".")) {
                    fileName.substring(0, fileName.lastIndexOf('.'))
                } else {
                    fileName
                }

                val groupName = groupMap.getOrDefault(fileGroupId, "未知组")

                try {
                    log.info("正在处理文件: {} - {}", groupName, baseName)

                    // 4. 生成文档流
                    val resource = ragFileService.generateDocumentStreamByFileId(fileId)
                    val inputStream = resource.inputStream
                    val content = inputStream.readAllBytes()

                    if (content.isEmpty()) {
                        log.warn("fileId={} 的内容为空，跳过上传", fileId)
                        continue
                    }

                    // 5. 构造对象名：时间/文件组名-文件名.txt
                    val objectName = groupName?.let { "$folderName/${it.trim()}-${baseName.trim()}.txt" }

                    // 6. 检查是否已存在
                    if (minioService.fileExists(objectName as String, bucketName)) {
                        log.info("文件已存在，跳过: {}", objectName)
                        continue
                    }

                    // 7. 上传到 MinIO
                    ByteArrayInputStream(content).use { inputStream ->
                        minioService.handleUpload(objectName, inputStream, content.size.toLong(), bucketName)
                    }

                    log.info("成功上传文档: {} -> {}", fileId, objectName)

                } catch (e: Exception) {
                    log.error("处理文件失败: fileId={}, name={}", fileId, fileName, e)
                }
            }

        } catch (e: Exception) {
            log.error("【定时任务】执行失败", e)
        }

        log.info("【定时任务】文档导出与上传完成。")
    }
}
