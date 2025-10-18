package com.langhuan.utils.rag.splitter

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONUtil
import com.langhuan.serviceai.ChatGeneralAssistanceService
import org.slf4j.LoggerFactory

class LlmTextSplitter(
    private val windowSize: Int,
    private val modelName: String,
    private val chatGeneralAssistanceService: ChatGeneralAssistanceService
) : TextSplitter {

    companion object {
        private val log = LoggerFactory.getLogger(LlmTextSplitter::class.java)
    }

    override fun apply(text: String): List<String> {
        val documents = mutableListOf<String>()
        val length = text.length
        for (i in 0 until length step windowSize) {
            val chunk = text.substring(i, minOf(length, i + windowSize))
            log.info("llmOutString:{}", chunk)
            var llmOutString = chatGeneralAssistanceService.llmTextSplitter(modelName, chunk)
            if (llmOutString != null) {
                llmOutString = llmOutString.replace("```json", "")
            }
            if (llmOutString != null) {
                llmOutString = llmOutString.replace("```", "")
            }
            val content = JSONUtil.parseObj(llmOutString).getJSONArray("content")
            val list = content.stream()
                    .map { it.toString() }
                    .toList()
            documents.addAll(list)
        }
        return documents
    }
}
