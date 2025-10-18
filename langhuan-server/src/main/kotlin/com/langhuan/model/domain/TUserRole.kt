package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

/**
 * 用户-角色关系表
 * @TableName t_user_role
 */
@TableName(value = "t_user_role")
data class TUserRole(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 
     */
    var roleId: Int? = null,

    /**
     * 
     */
    var userId: Int? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
