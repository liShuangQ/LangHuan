package com.langhuan.model.pojo

/**
 * Lucene 搜索结果
 *
 * 包含文档ID、相关度分数和文档信息。
 *
 * @author Langhuan System
 * @since 1.0.0
 */
data class SearchResult(
    /**
     * 文档ID
     */
    val id: String,

    /**
     * 相关度分数
     */
    val score: Float,

    /**
     * Lucene 内部文档ID
     */
    val docId: Int
)