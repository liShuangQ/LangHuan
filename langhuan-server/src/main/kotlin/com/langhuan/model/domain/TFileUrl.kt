package com.langhuan.model.domain

/**
 * 
 * @TableName t_file_url
 */
data class TFileUrl(
    /**
     * 
     */
    var id: Int? = null,

    /**
     * 
     */
    var fileId: Int? = null,

    /**
     * 
     */
    var fUrl: String? = null,

    /**
     * 
     */
    var fStatus: String? = null
)
