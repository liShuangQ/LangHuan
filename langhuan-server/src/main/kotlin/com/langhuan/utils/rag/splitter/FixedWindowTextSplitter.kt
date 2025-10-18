package com.langhuan.utils.rag.splitter

class FixedWindowTextSplitter(private val windowSize: Int) : TextSplitter {

    override fun apply(text: String): List<String> {
        val documents = mutableListOf<String>()
        val length = text.length
        for (i in 0 until length step windowSize) {
            val chunk = text.substring(i, minOf(length, i + windowSize))
            documents.add(chunk)
        }
        return documents
    }
}
