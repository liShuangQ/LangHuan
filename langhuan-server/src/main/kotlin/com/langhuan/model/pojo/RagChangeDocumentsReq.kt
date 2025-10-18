package com.langhuan.model.pojo

import com.langhuan.model.domain.TRagFile
import lombok.Data

@Data
data class RagChangeDocumentsReq(
    var documents: String? = null,
    var documentId: String? = null,
    var ragFile: TRagFile? = null
)
