package com.langhuan.model.pojo;

import java.util.List;

/**
 * @author Afish
 * @date 2025/7/29 15:48
 */
public class DocumentProcessResult {
    private List<String> documents;
    private String fileId;

    public DocumentProcessResult(List<String> documents, String fileId) {
        this.documents = documents;
        this.fileId = fileId;
    }
}
