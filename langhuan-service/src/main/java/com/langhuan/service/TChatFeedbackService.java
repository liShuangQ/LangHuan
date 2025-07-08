package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.model.mapper.TChatFeedbackMapper;
import org.springframework.stereotype.Service;

/**
 * @author lishuangqi
 * @description 针对表【t_chat_feedback】的数据库操作Service实现
 * @createDate 2025-04-24 14:55:15
 */
@Service
public class TChatFeedbackService extends ServiceImpl<TChatFeedbackMapper, TChatFeedback> {

    public IPage<TChatFeedback> chatFeedbackSearch(String userId, String interaction, int pageNum, int pageSize) {
        return super.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TChatFeedback>()
                        .like(userId != null && !userId.isEmpty(), TChatFeedback::getUserId, userId)
                        .eq(interaction != null && !interaction.isEmpty(), TChatFeedback::getInteraction, interaction)
                        .last("ORDER BY CASE interaction " +
                                "WHEN 'dislike' THEN 1 " +
                                "WHEN 'like' THEN 2 " +
                                "WHEN 'end' THEN 3 " +
                                "ELSE 4 END, interaction_time DESC"));
    }
}
