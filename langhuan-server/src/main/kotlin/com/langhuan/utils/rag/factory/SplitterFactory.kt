package com.langhuan.utils.rag.factory

import com.langhuan.serviceai.ChatGeneralAssistanceService
import com.langhuan.utils.rag.config.SplitConfig
import com.langhuan.utils.rag.splitter.FixedWindowTextSplitter
import com.langhuan.utils.rag.splitter.LlmTextSplitter
import com.langhuan.utils.rag.splitter.PatternTokenTextSplitter
import com.langhuan.utils.rag.splitter.TextSplitter
import java.util.regex.Pattern

/**
 * 分割器工厂
 */
object SplitterFactory {
    fun createSplitter(config: SplitConfig, chatService: ChatGeneralAssistanceService): TextSplitter {
        return when (config.splitMethod) {
            "FixedWindowTextSplitter" -> FixedWindowTextSplitter(config.methodData["windowSize"] as Int)
            "PatternTokenTextSplitter" -> PatternTokenTextSplitter(Pattern.compile(config.methodData["splitPattern"] as String))
            "LlmTextSplitter" -> LlmTextSplitter(
                    config.methodData["windowSize"] as Int,
                    config.methodData["modelName"] as String,
                    chatService
            )
            else -> throw IllegalArgumentException("Unsupported split method: " + config.splitMethod)
        }
    }
}
