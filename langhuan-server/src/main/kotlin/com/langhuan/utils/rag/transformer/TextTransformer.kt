package com.langhuan.utils.rag.transformer

import com.langhuan.utils.rag.splitter.TextSplitter
import org.springframework.stereotype.Component

/**
 * @author Afish
 * @date 2025/7/23 13:45
 */
@Component
class TextTransformer {
    fun transform(text: String, splitter: TextSplitter): List<String> {
        return splitter.apply(text)
    }
}
