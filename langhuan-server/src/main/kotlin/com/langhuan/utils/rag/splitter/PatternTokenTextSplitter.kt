package com.langhuan.utils.rag.splitter

import java.util.regex.Pattern

class PatternTokenTextSplitter(private val splitPattern: Pattern) : TextSplitter {

    /**
     * 根据指定的分割模式将文本分割成多个文档
     * 此方法主要用于文本预处理阶段，将连续的文本内容根据提供的分割模式分割成独立的句子，
     * 并将这些句子转换成Document对象列表这有助于后续的文本处理或分析
     *
     * @param text 待处理的文本内容
     * @return 文本中提取出的Document对象列表，每个Document对象代表一个分割后的句子
     */
    override fun apply(text: String): List<String> {
        // 初始化文档列表，用于存储分割后的句子创建的Document对象
        val documents = mutableListOf<String>()
        // 使用提供的分割模式将文本分割成句子数组
        val sentences = splitPattern.split(text)

        // 遍历分割后的每个句子
        for (sentence in sentences) {
            // 去除句子前后的空白字符
            val trimmedSentence = sentence.trim()
            // 如果句子非空，则创建一个新的Document对象，并添加到文档列表中
            if (trimmedSentence.isNotEmpty()) {
                documents.add(trimmedSentence)
//                documents.add(new Document(sentence, metadata));
            }
        }
        // XXX 临时处理
        if (documents[0].contains("source: Invalid source URI")) {
            documents[0] = documents[0].replace(
                "source: Invalid source URI: InputStream resource [resource loaded through InputStream] cannot be resolved to URL",
                "").trim()
        }
        // 返回包含所有创建的Document对象的列表
        return documents
    }
}
