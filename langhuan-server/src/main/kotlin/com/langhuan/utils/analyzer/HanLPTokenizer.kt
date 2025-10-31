package com.langhuan.utils.analyzer

import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.common.Term
import org.apache.lucene.analysis.tokenattributes.*
import org.apache.lucene.util.AttributeFactory
import java.io.IOException
import java.io.Reader
import java.util.*

/**
 * 基于 HanLP 的 Lucene 中文分词器
 *
 * 使用 HanLP.segment() 进行中文分词，支持 TextField
 *
 * @author 系统生成
 * @since 1.0.0
 */
class HanLPTokenizer(input: Reader) : org.apache.lucene.analysis.Tokenizer() {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(HanLPTokenizer::class.java)
    }

    // Lucene 属性
    private val termAttribute: CharTermAttribute = addAttribute(CharTermAttribute::class.java)
    private val offsetAttribute: OffsetAttribute = addAttribute(OffsetAttribute::class.java)
    private val positionIncrementAttribute: PositionIncrementAttribute = addAttribute(PositionIncrementAttribute::class.java)
    private val typeAttribute: TypeAttribute = addAttribute(TypeAttribute::class.java)

    // 分词相关属性
    private var terms: MutableList<Term> = mutableListOf()
    private var currentTermIndex: Int = 0
    private var textBuffer: StringBuilder = StringBuilder()
    private var offsetCorrection: Int = 0

    init {
        // 设置输入源
        super.setReader(input)
        // 重置 HanLP 配置，确保使用标准分词
        HanLP.Config.ShowTermNature = false
    }

    @Throws(IOException::class)
    override fun incrementToken(): Boolean {
        clearAttributes()

        // 首次调用时读取所有文本
        if (textBuffer.isEmpty()) {
            readAllText()
            performTokenization()
        }

        // 返回下一个分词结果
        if (currentTermIndex < terms.size) {
            val term = terms[currentTermIndex]

            // 设置词项内容
            termAttribute.setEmpty()
            termAttribute.append(term.word)

            // 设置偏移量
            val startOffset = term.offset
            val endOffset = startOffset + term.word.length
            offsetAttribute.setOffset(startOffset, endOffset)

            // 设置位置增量
            positionIncrementAttribute.setPositionIncrement(if (currentTermIndex == 0) 1 else 1)

            // 设置词类型
            typeAttribute.setType(if (isChinese(term.word)) "CN_WORD" else "WORD")

            currentTermIndex++
            return true
        }

        return false
    }

    /**
     * 读取所有输入文本
     */
    @Throws(IOException::class)
    private fun readAllText() {
        val buffer = CharArray(8192)
        var bytesRead: Int
        textBuffer = StringBuilder()

        while (true) {
            bytesRead = this.input.read(buffer)
            if (bytesRead == -1) {
                break
            }
            textBuffer.append(buffer, 0, bytesRead)
        }

        // 清理文本，移除控制字符
        val cleanText = textBuffer.toString().replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "")
        textBuffer = StringBuilder(cleanText)
    }

    /**
     * 执行分词
     */
    private fun performTokenization() {
        if (textBuffer.isNotEmpty()) {
            try {
                // 使用 HanLP 进行分词
                val segmentResult = HanLP.segment(textBuffer.toString())

                // 过滤掉空字符串和单个空格的词
                terms = segmentResult.filter { term ->
                    term.word.isNotBlank() && term.word.trim().isNotEmpty()
                }.toMutableList()

                currentTermIndex = 0

                log.debug("分词结果: {} -> {}", textBuffer, terms.map { it.word })

            } catch (e: Exception) {
                log.error("HanLP 分词失败: {}", e.message, e)
                terms.clear()
            }
        }
    }

    /**
     * 判断是否包含中文字符
     */
    private fun isChinese(text: String): Boolean {
        return text.any { char ->
            char.code in 0x4E00..0x9FFF || // CJK统一汉字
            char.code in 0x3400..0x4DBF || // CJK扩展A
            char.code in 0x20000..0x2A6DF || // CJK扩展B
            char.code in 0x2A700..0x2B73F || // CJK扩展C
            char.code in 0x2B740..0x2B81F || // CJK扩展D
            char.code in 0x2B820..0x2CEAF || // CJK扩展E
            char.code in 0x2CEB0..0x2EBEF || // CJK扩展F
            char.code in 0x3000..0x303F || // CJK符号和标点
            char.code in 0xFF00..0xFFEF   // 全角ASCII、全角标点
        }
    }

    @Throws(IOException::class)
    override fun reset() {
        super.reset()
        terms.clear()
        currentTermIndex = 0
        textBuffer = StringBuilder()
        offsetCorrection = 0
    }

    @Throws(IOException::class)
    override fun end() {
        super.end()
        val finalOffset = textBuffer.length
        offsetAttribute.setOffset(finalOffset, finalOffset)
    }
}