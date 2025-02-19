package com.shuangqi.aiagent7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuangqi.aiagent7.model.domain.TRagFileGroup;
import com.shuangqi.aiagent7.model.mapper.TRagFileGroupMapper;
import org.springframework.stereotype.Service;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group】的数据库操作Service实现
 * @createDate 2025-01-17 15:08:49
 */
@Service
public class TRagFileGroupService extends ServiceImpl<TRagFileGroupMapper, TRagFileGroup> {

    public Page<TRagFileGroup> queryFileGroups(String groupName, String groupType, int pageNum, int pageSize) {
        Page<TRagFileGroup> tRagFileGroupPage = super.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TRagFileGroup>()
                        .like(!groupName.isEmpty(), TRagFileGroup::getGroupName, groupName)
                        .like(!groupType.isEmpty(), TRagFileGroup::getGroupType, groupType)
        );

        return tRagFileGroupPage;
    }
}
