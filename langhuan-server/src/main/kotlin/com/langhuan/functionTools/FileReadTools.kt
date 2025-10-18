package com.langhuan.functionTools

import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.core.io.FileSystemResource

class FileReadTools {

    companion object {
        private val log = LoggerFactory.getLogger(FileReadTools::class.java)
    }

    @Tool(description = "文档解析函数", returnDirect = true)
    fun DocumentAnalyzer(@ToolParam(description = "需要解析的文档路径") path: String): String {
        log.info("调用工具：DocumentAnalyzer")
        // ai解析用户的提问得到path参数，使用tika读取本地文件获取内容。
        return try {
            log.debug("DocumentAnalyzer path: {}", path)
            val tikaDocumentReader = TikaDocumentReader(FileSystemResource(path))
            val documents = tikaDocumentReader.read()
            val stringBuilder = StringBuilder()
            for (document in documents) {
                stringBuilder.append(document.formattedContent)
            }
//            val prompt = """
//                      ***tools***
//                      你是一个文件阅读专家，擅长阅读文件内容，并给出文件内容的摘要。
//                      请根据用户提供的文件内容，给出文件内容的摘要。
//
//                      输入格式：
//                      文件内容：一段文件内容。
//                      文档内容使用```包裹。
//
//                      输出格式：
//                      直接输出一个字符串，内容为文件内容的摘要。
//                      ******
//                    """
//            return prompt + "```" + stringBuilder.toString() + "```"
            stringBuilder.toString()

        } catch (e: Exception) {
            log.error("解析文件失败", e)
            "解析文件失败"
        }
    }
}
