package com.langhuan.utils.rag.metadata;

import com.langhuan.model.domain.TRagFile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Afish
 * @date 2025/7/23 13:51
 */
@Component
public class RagMetadataFactory {
    public Map<String, Object> createMetadata(TRagFile ragFile) {
        return Map.of(
                "filename", ragFile.getFileName(),
                "fileId", String.valueOf(ragFile.getId()),
                "groupId", ragFile.getFileGroupId(),
                "rank", 0
        );
    }

    public Map<String, Object> createMetadata(String fileName, String fileId, String groupId) {
        return Map.of(
                "filename", fileName,
                "fileId", fileId,
                "groupId", groupId,
                "rank", 0
        );
    }
}
