package com.langhuan.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Lucene 配置类
 *
 * 启用定时任务功能以支持 Lucene 索引的定时重建。
 *
 * @author Langhuan System
 * @since 1.0.0
 */
@Configuration
@EnableScheduling
class LuceneConfig {

    /**
     * 启用定时任务调度
     *
     * 通过 @EnableScheduling 注解启用 Spring 的定时任务功能，
     * 支持 LuceneIndexTask 中的 @Scheduled 注解定时执行索引重建。
     */
}