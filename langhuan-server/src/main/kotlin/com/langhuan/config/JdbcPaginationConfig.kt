package com.langhuan.config

import com.langhuan.common.GlobalExceptionHandler
import com.langhuan.utils.pagination.JdbcPaginationHelper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

/**
 * JdbcTemplate分页工具配置类
 * 配置分页查询相关的Bean
 *
 * @author system
 */
@Configuration
class JdbcPaginationConfig {
    companion object {
        private val log = LoggerFactory.getLogger(JdbcPaginationConfig::class.java)
    }

    /**
     * 注册JdbcTemplate分页工具
     *
     * @param jdbcTemplate JdbcTemplate实例
     * @return 分页工具实例
     */
    @Bean
    fun jdbcPaginationHelper(jdbcTemplate: JdbcTemplate): JdbcPaginationHelper {
        log.info("初始化JdbcTemplate分页工具")
        return JdbcPaginationHelper(jdbcTemplate)
    }
}
