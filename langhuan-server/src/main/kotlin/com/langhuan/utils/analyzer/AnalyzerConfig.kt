package com.langhuan.utils.analyzer

import org.apache.lucene.analysis.Analyzer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Lucene Analyzer 配置类
 *
 * 提供各种 Lucene 分析器的 Spring Bean 配置
 *
 * @author 系统生成
 * @since 1.0.0
 */
@Configuration
class AnalyzerConfig {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(AnalyzerConfig::class.java)
    }

    /**
     * 注册 HanLP 中文分析器 Bean
     *
     * @return HanLPAnalyzer 实例
     */
    @Bean("hanLPAnalyzer")
    fun hanLPAnalyzer(): Analyzer {
        log.info("创建 HanLP Analyzer Bean")
        return HanLPAnalyzer()
    }

    /**
     * 获取标准中文分析器实例
     *
     * @return HanLPAnalyzer 实例
     */
    fun getChineseAnalyzer(): Analyzer {
        return HanLPAnalyzer()
    }

    /**
     * 创建用于索引的分析器
     *
     * @return 专门用于索引的 HanLPAnalyzer 实例
     */
    fun getIndexAnalyzer(): Analyzer {
        return HanLPAnalyzer()
    }

    /**
     * 创建用于搜索的分析器
     *
     * @return 专门用于搜索的 HanLPAnalyzer 实例
     */
    fun getSearchAnalyzer(): Analyzer {
        return HanLPAnalyzer()
    }
}