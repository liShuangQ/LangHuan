package com.langhuan.model.pojo

import lombok.Data

@Data
data class RagMetaData(
    var filename: String? = null,
    var fileId: String? = null,
    var groupId: String? = null, // HACK: 改动字段名注意搜索：filterExpression
    var rank: Int = 0 // 0-100的整数。 HACK: 改动字段名注意搜索：filterExpression
)
