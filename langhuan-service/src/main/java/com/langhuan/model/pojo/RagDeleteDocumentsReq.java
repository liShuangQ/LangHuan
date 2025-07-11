package com.langhuan.model.pojo;

import com.langhuan.model.domain.TRagFile;
import lombok.Data;

import java.util.List;

@Data
public class RagDeleteDocumentsReq {
    private String documentId;
    private TRagFile ragFile;
}
