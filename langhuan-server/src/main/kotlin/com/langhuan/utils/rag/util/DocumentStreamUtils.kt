package com.langhuan.utils.rag.util

import com.langhuan.common.Constant
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * @author Afish
 * @date 2025/7/23 13:53
 */
@Component
class DocumentStreamUtils {
    fun generateStream(contents: List<String>): InputStreamResource {
        val outputStream = ByteArrayOutputStream()
        try {
            OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                for (content in contents) {
                    writer.write(content)
                    writer.write(System.lineSeparator())
                    writer.write(Constant.DEFAULT_RAG_EXPORT_SPLIT)
                    writer.write(System.lineSeparator())
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate document stream", e)
        }
        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }
}
