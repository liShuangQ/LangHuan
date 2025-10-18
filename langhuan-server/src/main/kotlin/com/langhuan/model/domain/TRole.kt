package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

/**
 * 系统角色表
 * @TableName t_role
 */
@TableName(value = "t_role")
data class TRole(
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
     * 备注
     */
    var remark: String? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
