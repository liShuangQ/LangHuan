package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
import java.util.*

/**
 * 
 * @TableName t_tool
 */
@TableName(value = "t_tool")
data class TTool(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 
     */
    var toolName: String? = null,

    /**
     * 
     */
    var toolType: String? = null,

    /**
     * 
     */
    var toolDesc: String? = null,

    /**
     * 
     */
    var toolGroupId: String? = null,

    /**
     * 
     */
    var toolBy: String? = null,

    /**
     * 
     */
    var toolAt: Date? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
