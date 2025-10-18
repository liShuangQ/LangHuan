package com.langhuan.utils.rag.metadata

import com.langhuan.model.domain.TRagFile
import org.springframework.stereotype.Component

/**
 * @author Afish
 * @date 2025/7/23 13:51
 */
@Component
class RagMetadataFactory {
    fun createMetadata(ragFile: TRagFile): Map<String, Any?> {
        return mapOf(
                "filename" to ragFile.fileName,
                "fileId" to ragFile.id.toString(),
                "groupId" to ragFile.fileGroupId,
                "rank" to 0
        )
    }

    fun createMetadata(fileName: String, fileId: String, groupId: String): Map<String, Any> {
        return mapOf(
                "filename" to fileName,
                "fileId" to fileId,
                "groupId" to groupId,
                "rank" to 0
        )
    }
}
