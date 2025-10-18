package com.langhuan.utils.rag.splitter

/**
 * @author Afish
 * @date 2025/7/23 11:12
 */
interface TextSplitter {
    fun apply(text: String): List<String>
}
