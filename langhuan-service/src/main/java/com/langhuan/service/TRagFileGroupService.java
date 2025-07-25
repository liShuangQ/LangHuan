package com.langhuan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.dao.TRagFileGroupDao;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.mapper.TRagFileGroupMapper;
import com.langhuan.utils.other.SecurityUtils;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group】的数据库操作Service实现
 * @createDate 2025-01-17 15:08:49
 */
@Slf4j
@Service
public class TRagFileGroupService extends ServiceImpl<TRagFileGroupMapper, TRagFileGroup> {

    private final TRagFileGroupDao tRagFileGroupDao;

    public TRagFileGroupService(TRagFileGroupDao tRagFileGroupDao) {
        this.tRagFileGroupDao = tRagFileGroupDao;
    }

    /**
     * 根据权限获取文件组枚举列表
     * @param isRead 是否为读取权限
     * @return 文件组枚举列表
     */
    public List<Map<String, Object>> getEnum(Boolean isRead) {
        boolean isAdmin = SecurityUtils.hasAdminRole();
        String currentUser = SecurityUtils.getCurrentUsername();
        if (isAdmin) {
            return tRagFileGroupDao.getEnumForAdmin();
        }else{
            return tRagFileGroupDao.getEnumForUser(isRead, currentUser);
        }
    }

    /**
     * 根据权限查询文件组
     * 超级管理员可以查询所有文件组
     * 普通用户只能查询公开的、自己创建的和被分享的文件组
     * @param groupName 文件组名称
     * @param groupType 文件组类型
     * @param visibility 可见性
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页查询结果
     */
    public IPage<Map<String, Object>> queryFileGroups(String groupName, String groupType, String visibility,
            int pageNum, int pageSize) {
        String currentUser = SecurityUtils.getCurrentUsername();
        boolean isAdmin = SecurityUtils.hasAdminRole();
        if (isAdmin) {
            return tRagFileGroupDao.queryFileGroupsForAdmin(groupName, groupType, visibility, pageNum, pageSize);
        }
        return tRagFileGroupDao.queryFileGroupsForUser(groupName, groupType, visibility, pageNum, pageSize, currentUser);
    }
}
