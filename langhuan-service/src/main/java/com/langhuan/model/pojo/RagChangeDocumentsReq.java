package com.langhuan.model.pojo;

import com.langhuan.model.domain.TRagFile;
import lombok.Data;

import java.util.List;

@Data
public class RagChangeDocumentsReq {
    private List<String> documents;
    private String documentId;
    private TRagFile ragFile;
}
