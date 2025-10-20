package com.langhuan.config;

import com.langhuan.utils.pagination.JdbcPaginationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JdbcTemplate分页工具配置类
 * 配置分页查询相关的Bean
 *
 * @author system
 */
@Slf4j
@Configuration
public class JdbcPaginationConfig {

    /**
     * 注册JdbcTemplate分页工具
     * 
     * @param jdbcTemplate JdbcTemplate实例
     * @return 分页工具实例
     */
    @Bean
    public JdbcPaginationHelper jdbcPaginationHelper(JdbcTemplate jdbcTemplate) {
        log.info("初始化JdbcTemplate分页工具");
        return new JdbcPaginationHelper(jdbcTemplate);
    }
} 