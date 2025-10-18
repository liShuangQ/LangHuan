package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.util.*

/**
 * 
 * @TableName t_prompts
 */
@TableName(value = "t_prompts")
data class TPrompts(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 
     */
    var content: String? = null,

    /**
     * 
     */
    var category: String? = null,

    /**
     * 
     */
    var createdAt: Date? = null,

    /**
     * 
     */
    var updatedAt: Date? = null,

    /**
     * 
     */
    var methodName: String? = null,

    /**
     * 
     */
    var description: String? = null
)
