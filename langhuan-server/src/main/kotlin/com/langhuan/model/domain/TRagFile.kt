package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.fasterxml.jackson.annotation.JsonFormat
import java.util.*

/**
 * @TableName t_rag_file
 */
@TableName(value = "t_rag_file")
data class TRagFile(
    /**
     *
     */
    @TableId
    var id: Int? = null,

    /**
     *
     */
    var fileName: String? = null,

    /**
     *
     */
    var fileType: String? = null,

    /**
     *
     */
    var fileSize: String? = null,

    /**
     *
     */
    var documentNum: String? = null,

    /**
     *
     */
    var fileDesc: String? = null,

    /**
     *
     */
    var fileGroupId: String? = null,

    /**
     *
     */
    var uploadedBy: String? = null,

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var uploadedAt: Date? = null
)
