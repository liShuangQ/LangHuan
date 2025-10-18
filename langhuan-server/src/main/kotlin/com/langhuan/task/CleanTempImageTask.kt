package com.langhuan.task

import com.langhuan.model.domain.TFileUrl
import com.langhuan.service.MinioService
import com.langhuan.service.TFileUrlService
import jakarta.annotation.Resource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * @author Afish
 * @date 2025/7/31 14:44
 */
@Component
class CleanTempImageTask {
    
    companion object {
        private val log = LoggerFactory.getLogger(CleanTempImageTask::class.java)
    }
    
    @Value("\${minio.img-bucket-name}")
    private lateinit var bucketName: String

    @Resource
    private lateinit var tFileUrlService: TFileUrlService

    @Resource
    private lateinit var minioService: MinioService

    @Scheduled(cron = "\${task.schedule.image}") // 每天 00:00:00 执行
    @Transactional(rollbackFor = [Exception::class])
    fun cleanupTemporaryImages() {
        log.info("Starting cleanup of temporary images at midnight...")

        // 查询 f_status='临时'的记录
        val expiredUrls = tFileUrlService.query()
            .eq("f_status", "临时")
            .list()

        if (expiredUrls.isEmpty()) {
            log.info("No expired temporary images found.")
            return
        }

        // 提取需要删除的 MinIO objectNames
        val objectNames = expiredUrls
            .map { it.fUrl }
            .map { url -> minioService.extractObjectName(url as String, bucketName) }
            .filter { objName -> !objName.isNullOrEmpty() }

        // 删除 MinIO 中的对象
        for (objectName in objectNames) {
            try {
                minioService.handleDelete(objectName, bucketName)
                log.info("Successfully removed MinIO object: {}", objectName)
            } catch (e: Exception) {
                log.error("Failed to remove MinIO object: {}", objectName, e)
            }
        }

        // 获取要删除的记录ID列表
        val idsToDelete = expiredUrls.map { it.id }

        // 批量删除数据库记录
        if (idsToDelete.isNotEmpty()) {
            val deleteSuccess = tFileUrlService.removeByIds(idsToDelete)
            if (deleteSuccess) {
                log.info("Successfully deleted {} expired records from database.", idsToDelete.size)
            } else {
                log.error("Failed to delete some or all expired records from database.")
            }
        }

        log.info("Finished daily cleanup. Total cleaned: {} files", expiredUrls.size)
    }
}
