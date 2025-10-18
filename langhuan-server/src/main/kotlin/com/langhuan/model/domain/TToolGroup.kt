package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
import java.util.*

/**
 * 
 * @TableName t_tool_group
 */
@TableName(value = "t_tool_group")
data class TToolGroup(
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
    var groupDesc: String? = null,

    /**
     * 
     */
    var createdBy: String? = null,

    /**
     * 
     */
    var createdAt: Date? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
