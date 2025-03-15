package com.langhuan.model.pojo;

import com.langhuan.model.domain.TRagFile;
import lombok.Data;

import java.util.List;

@Data
public class RagWriteDocumentsReq {
    private List<String> documents;
    private TRagFile ragFile;
}
