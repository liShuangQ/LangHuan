package com.langhuan.utils.analyzer

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.*
import java.io.Reader

/**
 * 基于 HanLP 的 Lucene 中文分析器
 *
 * 提供中文文本的 TokenStream 处理，使用 HanLP 进行中文分词。
 * 适用于 TextField 字段的索引和搜索分析。
 *
 * @author 系统生成
 * @since 1.0.0
 */
class HanLPAnalyzer : Analyzer() {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(HanLPAnalyzer::class.java)
    }

    /**
     * 创建 TokenStreamComponents
     *
     * @param fieldName 字段名称
     * @return TokenStreamComponents 组件
     */
    override fun createComponents(fieldName: String): Analyzer.TokenStreamComponents {
        val tokenizer = HanLPTokenizerWrapper()
        return Analyzer.TokenStreamComponents(tokenizer)
    }

    /**
     * 简化的 Tokenizer 包装器
     */
    private class HanLPTokenizerWrapper : org.apache.lucene.analysis.Tokenizer() {

        private val termAttribute: CharTermAttribute = addAttribute(CharTermAttribute::class.java)
        private val offsetAttribute: OffsetAttribute = addAttribute(OffsetAttribute::class.java)
        private var processed: Boolean = false
        private var tokens: List<String> = emptyList()
        private var currentIndex: Int = 0

        override fun incrementToken(): Boolean {
            clearAttributes()

            if (!processed) {
                processText()
                processed = true
            }

            if (currentIndex < tokens.size) {
                val token = tokens[currentIndex]
                termAttribute.setEmpty()
                termAttribute.append(token)
                offsetAttribute.setOffset(currentIndex, currentIndex + token.length)
                currentIndex++
                return true
            }

            return false
        }

        private fun processText() {
            try {
                val text = readAllText()
                if (text.isNotBlank()) {
                    // 使用 HanLP 进行分词
                    val segmentResult = com.hankcs.hanlp.HanLP.segment(text)
                    tokens = segmentResult.map { it.word }.filter { it.isNotBlank() }
                }
            } catch (e: Exception) {
                log.error("分词处理失败", e)
                tokens = emptyList()
            }
        }

        private fun readAllText(): String {
            val buffer = CharArray(1024)
            val builder = StringBuilder()
            var bytesRead: Int
            while (this.input.read(buffer).also { bytesRead = it } != -1) {
                builder.append(buffer, 0, bytesRead)
            }
            return builder.toString()
        }

        override fun reset() {
            super.reset()
            processed = false
            tokens = emptyList()
            currentIndex = 0
        }
    }
}