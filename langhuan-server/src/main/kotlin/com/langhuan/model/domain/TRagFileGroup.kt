package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
import java.util.*

/**
 * 
 * @TableName t_rag_file_group
 */
@TableName(value = "t_rag_file_group")
data class TRagFileGroup(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 
     */
    var groupName: String? = null,

    /**
     *
     */
    var groupType: String? = null,

    /**
     * 
     */
    var groupDesc: String? = null,

    /**
     * 创建者
     */
    var createdBy: String? = null,

    /**
     * 创建时间
     */
    var createdAt: Date? = null,

    /**
     * 可见性：private-私有，public-公开
     */
    var visibility: String? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
