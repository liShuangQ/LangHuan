package com.langhuan.utils.analyzer

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import java.io.StringReader

/**
 * HanLP Analyzer 简单测试
 *
 * @author 系统生成
 * @since 1.0.0
 */
object HanLPTest {

    private val log = org.slf4j.LoggerFactory.getLogger(HanLPTest::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        testTokenization()
    }

    private fun testTokenization() {
        val analyzer = HanLPAnalyzer()
        val testText = "中华人民共和国今天成立了，中国人民从此站起来了！琅嬛福地是一个AI知识管理系统。"

        try {
            val tokenStream = analyzer.tokenStream("content", StringReader(testText))
            val termAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)

            tokenStream.reset()
            println("HanLP 中文分词测试结果：")
            println("原文：$testText")
            println("分词结果：")

            var count = 0
            while (tokenStream.incrementToken()) {
                val term = termAttribute.toString()
                count++
                println("${String.format("%2d", count)}: $term")
            }

            tokenStream.end()
            tokenStream.close()

            println("\n总共分出 $count 个词汇")

        } catch (e: Exception) {
            log.error("分词测试失败", e)
        } finally {
            analyzer.close()
        }
    }
}