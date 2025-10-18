package com.langhuan.service

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.dao.TRagFileGroupDao
import com.langhuan.model.domain.TRagFileGroup
import com.langhuan.model.mapper.TRagFileGroupMapper
import com.langhuan.utils.other.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group】的数据库操作Service实现
 * @createDate 2025-01-17 15:08:49
 */
@Service
class TRagFileGroupService(
    private val tRagFileGroupDao: TRagFileGroupDao
) : ServiceImpl<TRagFileGroupMapper, TRagFileGroup>() {
    
    companion object {
        private val log = LoggerFactory.getLogger(TRagFileGroupService::class.java)
    }

    /**
     * 根据权限获取文件组枚举列表
     * @param isRead 是否为读取权限
     * @return 文件组枚举列表
     */
    fun getEnum(isRead: Boolean?): List<Map<String, Any>> {
        val isAdmin = SecurityUtils.hasAdminRole()
        val currentUser = SecurityUtils.getCurrentUsername()
        return if (isAdmin) {
            tRagFileGroupDao.getEnumForAdmin()
        } else {
            tRagFileGroupDao.getEnumForUser(isRead as Boolean, currentUser as String)
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
    fun queryFileGroups(
        groupName: String?, 
        groupType: String?, 
        visibility: String?,
        pageNum: Int, 
        pageSize: Int
    ): IPage<Map<String, Any>> {
        val currentUser = SecurityUtils.getCurrentUsername()
        val isAdmin = SecurityUtils.hasAdminRole()
        return if (isAdmin) {
            tRagFileGroupDao.queryFileGroupsForAdmin(groupName, groupType, visibility, pageNum, pageSize)
        } else {
            tRagFileGroupDao.queryFileGroupsForUser(groupName, groupType, visibility, pageNum, pageSize, currentUser as String)
        }
    }
}
