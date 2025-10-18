package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

/**
 * 
 * @TableName vector_store_rag
 */
@TableName(value = "vector_store_rag")
data class VectorStoreRag(
    /**
     * 
     */
    @TableId
    var id: Any? = null,

    /**
     * 
     */
    var content: String? = null,

    /**
     * 
     */
    var metadata: Any? = null,

    /**
     * 
     */
    var embedding: Any? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
