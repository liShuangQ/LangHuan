package com.langhuan.model.pojo

import com.langhuan.model.domain.TRagFile
import lombok.Data

@Data
data class RagWriteDocumentsReq(
    var documents: List<String>? = null,
    var ragFile: TRagFile? = null
)
