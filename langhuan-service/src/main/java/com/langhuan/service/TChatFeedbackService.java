package com.langhuan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.dao.TChatFeedbackDao;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.model.mapper.TChatFeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author lishuangqi
 * @description 针对表【t_chat_feedback】的数据库操作Service实现
 * @createDate 2025-04-24 14:55:15
 */
@Service
@RequiredArgsConstructor
public class TChatFeedbackService extends ServiceImpl<TChatFeedbackMapper, TChatFeedback> {

    private final TChatFeedbackDao tChatFeedbackDao;

    /**
     * 聊天反馈搜索
     *
     * @param username    用户名
     * @param interaction 交互类型
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    public IPage<Map<String, Object>> chatFeedbackSearch(String username, String interaction, int pageNum,
            int pageSize) {
        return tChatFeedbackDao.searchChatFeedback(username, interaction, pageNum, pageSize);
    }
}
