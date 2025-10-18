package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

/**
 * 系统权限表
 * @TableName t_permission
 */
@TableName(value = "t_permission")
data class TPermission(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 名称
     */
    var name: String? = null,

    /**
     * 接口路径
     */
    var url: String? = null,

    /**
     * 父级权限id
     */
    var parentId: Int? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
