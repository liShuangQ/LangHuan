package com.langhuan.task

import com.langhuan.service.LuceneIndexService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Lucene 索引定时任务
 *
 * 负责定时执行 Lucene 索引重建任务，确保索引与数据库保持同步。
 *
 * @author Langhuan System
 * @since 1.0.0
 */
@Component
class LuceneIndexTask {

    companion object {
        private val log = LoggerFactory.getLogger(LuceneIndexTask::class.java)
    }

    @Autowired
    private lateinit var luceneIndexService: LuceneIndexService

    /**
     * 定时重建 Lucene 索引
     *
     * 根据配置的 cron 表达式定时执行索引重建任务。
     * 默认为每 2 天凌晨 2 点执行 (task.lucene.cron = 0 0 2 1/2 * ?)
     */
    @Scheduled(cron = "\${task.lucene.cron}")
    fun rebuildIndexTask() {
        log.info("开始执行定时 Lucene 索引重建任务")

        try {
            val startTime = System.currentTimeMillis()
            val documentCount = luceneIndexService.rebuildIndex()
            val duration = (System.currentTimeMillis() - startTime) / 1000.0

            log.info("定时 Lucene 索引重建任务完成，处理文档数: {}，耗时: {:.2f} 秒", documentCount, duration)

        } catch (e: Exception) {
            log.error("定时 Lucene 索引重建任务执行失败", e)
        }
    }
}