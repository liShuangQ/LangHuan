package com.langhuan.model.domain

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.util.*

/**
 * 
 * @TableName t_chat_feedback
 */
@TableName(value = "t_chat_feedback")
data class TChatFeedback(
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
    var userInfo: Any? = null,

    /**
     * 
     */
    var questionId: String? = null,

    /**
     * 
     */
    var questionContent: String? = null,

    /**
     * 
     */
    var answerContent: String? = null,

    /**
     * 
     */
    var interaction: String? = null,

    /**
     * 
     */
    var interactionTime: Date? = null,

    /**
     * 
     */
    var knowledgeBaseIds: String? = null,

    /**
     * 
     */
    var suggestion: String? = null,

    /**
     * 
     */
    var usePrompt: String? = null,

    /**
     * 
     */
    var useModel: String? = null,

    /**
     * 
     */
    var useFileGroupId: String? = null,

    /**
     * 
     */
    var useRank: Boolean? = null
)
