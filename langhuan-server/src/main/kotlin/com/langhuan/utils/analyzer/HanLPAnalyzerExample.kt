package com.langhuan.utils.analyzer

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.*
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.ByteBuffersDirectory
import java.io.StringReader

/**
 * HanLP Analyzer 使用示例
 *
 * 展示如何使用 HanLPAnalyzer 进行中文文本分析和索引创建
 *
 * @author 系统生成
 * @since 1.0.0
 */
class HanLPAnalyzerExample {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(HanLPAnalyzerExample::class.java)

        /**
         * 测试分词功能
         */
        @JvmStatic
        fun testTokenization() {
            val analyzer = HanLPAnalyzer()
            val testText = "中华人民共和国今天成立了，中国人民从此站起来了！"

            try {
                val tokenStream = analyzer.tokenStream("content", StringReader(testText))
                val termAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)
                val offsetAttribute = tokenStream.addAttribute(OffsetAttribute::class.java)

                tokenStream.reset()
                println("分词结果：")
                while (tokenStream.incrementToken()) {
                    val term = termAttribute.toString()
                    val start = offsetAttribute.startOffset()
                    val end = offsetAttribute.endOffset()
                    println("词项: '$term' [${start}-${end}]")
                }
                tokenStream.end()
                tokenStream.close()
            } catch (e: Exception) {
                log.error("分词测试失败", e)
            } finally {
                analyzer.close()
            }
        }

        /**
         * 测试索引创建和搜索
         */
        @JvmStatic
        fun testIndexingAndSearch() {
            val analyzer = HanLPAnalyzer()
            val directory: Directory = ByteBuffersDirectory()
            val indexConfig = IndexWriterConfig(analyzer)

            try {
                // 创建索引
                val indexWriter = IndexWriter(directory, indexConfig)

                // 添加文档
                val doc1 = Document()
                doc1.add(TextField("title", "琅嬛福地是一个AI知识管理系统", Field.Store.YES))
                doc1.add(TextField("content", "基于Spring Boot和PostgreSQL构建，提供RAG检索增强生成功能", Field.Store.YES))
                indexWriter.addDocument(doc1)

                val doc2 = Document()
                doc2.add(TextField("title", "HanLP中文分词器", Field.Store.YES))
                doc2.add(TextField("content", "使用HanLP进行中文分词，支持Lucene索引和搜索", Field.Store.YES))
                indexWriter.addDocument(doc2)

                indexWriter.close()

                // 搜索测试
                val indexReader = DirectoryReader.open(directory)
                val indexSearcher = IndexSearcher(indexReader)

                // 搜索关键词
                val queries = listOf("中文分词", "AI知识", "RAG检索")

                queries.forEach { queryText ->
                    val query = TermQuery(Term("content", queryText))
                    val topDocs = indexSearcher.search(query, 10)

                    println("\n搜索 '$queryText' 的结果：")
                    if (topDocs.totalHits.value > 0) {
                        topDocs.scoreDocs.forEach { scoreDoc ->
                            val hitDoc = indexSearcher.doc(scoreDoc.doc)
                            val title = hitDoc.get("title")
                            val content = hitDoc.get("content")
                            val score = scoreDoc.score
                            println("  标题: $title")
                            println("  内容: $content")
                            println("  评分: $score")
                            println("  ---")
                        }
                    } else {
                        println("  未找到匹配结果")
                    }
                }

                indexReader.close()
            } catch (e: Exception) {
                log.error("索引和搜索测试失败", e)
            } finally {
                analyzer.close()
                directory.close()
            }
        }

        /**
         * 性能测试
         */
        @JvmStatic
        fun performanceTest() {
            val analyzer = HanLPAnalyzer()
            val testText = "这是一个用于测试性能的中文文本，包含了多个词汇和句子结构，用于评估HanLP分词器的处理速度和准确性。"

            try {
                val startTime = System.currentTimeMillis()
                val iterations = 1000

                repeat(iterations) {
                    val tokenStream = analyzer.tokenStream("content", StringReader(testText))
                    val termAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)

                    tokenStream.reset()
                    var tokenCount = 0
                    while (tokenStream.incrementToken()) {
                        tokenCount++
                    }
                    tokenStream.end()
                    tokenStream.close()
                }

                val endTime = System.currentTimeMillis()
                val totalTime = endTime - startTime
                val avgTime = totalTime.toDouble() / iterations

                println("性能测试结果：")
                println("  总次数: $iterations")
                println("  总时间: ${totalTime}ms")
                println("  平均时间: ${String.format("%.2f", avgTime)}ms")
                println("  每秒处理: ${String.format("%.2f", 1000.0 / avgTime)} 次")

            } catch (e: Exception) {
                log.error("性能测试失败", e)
            } finally {
                analyzer.close()
            }
        }

        /**
         * 主函数，运行所有测试
         */
        @JvmStatic
        fun main(args: Array<String>) {
            println("=== HanLP Analyzer 测试开始 ===")

            println("\n1. 分词功能测试")
            testTokenization()

            println("\n2. 索引和搜索测试")
            testIndexingAndSearch()

            println("\n3. 性能测试")
            performanceTest()

            println("\n=== HanLP Analyzer 测试完成 ===")
        }
    }
}