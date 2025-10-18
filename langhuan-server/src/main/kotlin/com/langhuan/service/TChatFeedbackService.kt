package com.langhuan.service

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.dao.TChatFeedbackDao
import com.langhuan.model.domain.TChatFeedback
import com.langhuan.model.mapper.TChatFeedbackMapper
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author lishuangqi
 * @description 针对表【t_chat_feedback】的数据库操作Service实现
 * @createDate 2025-04-24 14:55:15
 */
@Service
class TChatFeedbackService : ServiceImpl<TChatFeedbackMapper, TChatFeedback>() {

    @Autowired
    private val tChatFeedbackDao: TChatFeedbackDao? = null

    /**
     * 聊天反馈搜索
     *
     * @param username    用户名
     * @param interaction 交互类型
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    fun chatFeedbackSearch(username: String?, interaction: String?, pageNum: Int, pageSize: Int): IPage<Map<String, Any>> {
        return tChatFeedbackDao!!.searchChatFeedback(username, interaction, pageNum, pageSize) as IPage<Map<String, Any>>
    }
}
