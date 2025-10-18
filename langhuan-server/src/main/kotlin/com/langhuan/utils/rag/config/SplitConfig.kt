package com.langhuan.utils.rag.config

/**
 * 统一参数配置
 */
data class SplitConfig(
    val splitMethod: String,
    val methodData: Map<String, Any>
)
