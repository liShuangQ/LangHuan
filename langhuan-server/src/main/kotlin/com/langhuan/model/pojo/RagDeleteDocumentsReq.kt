package com.langhuan.model.pojo

import com.langhuan.model.domain.TRagFile
import lombok.Data

@Data
data class RagDeleteDocumentsReq(
    var documentId: String? = null,
    var ragFile: TRagFile? = null
)
