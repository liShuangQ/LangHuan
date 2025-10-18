package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.util.*

/**
 * 
 * @TableName t_user_chat_window
 */
@TableName(value = "t_user_chat_window")
data class TUserChatWindow(
    /**
     * 
     */
    @TableId
    var id: Int? = null,

    /**
     * 
     */
    var userId: String? = null,

    /**
     * 
     */
    var conversationId: String? = null,

    /**
     * 
     */
    var conversationName: String? = null,

    /**
     * 
     */
    var createdTime: Date? = null
)
