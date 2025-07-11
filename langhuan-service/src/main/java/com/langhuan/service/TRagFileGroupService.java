package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.mapper.TRagFileGroupMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group】的数据库操作Service实现
 * @createDate 2025-01-17 15:08:49
 */
@Service
public class TRagFileGroupService extends ServiceImpl<TRagFileGroupMapper, TRagFileGroup> {

    public List<Map<String, Object>> getEnum() {
        List<TRagFileGroup> list = super.list();
        return list.stream()
                .map(e -> Map.of("id", (Object) e.getId(), "groupName", (Object) e.getGroupName()))
                .collect(Collectors.toList());

    }

    public Page<TRagFileGroup> queryFileGroups(String groupName, String groupType, int pageNum, int pageSize) {

        return super.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TRagFileGroup>()
                        .like(!groupName.isEmpty(), TRagFileGroup::getGroupName, groupName)
                        .like(!groupType.isEmpty(), TRagFileGroup::getGroupType, groupType)
        );
    }
}
