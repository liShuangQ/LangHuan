package com.langhuan.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.model.domain.TPermission
import com.langhuan.model.domain.TRolePermission
import com.langhuan.model.mapper.TPermissionMapper
import com.langhuan.utils.other.SecurityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author lishuangqi
 * @description 针对表【t_permission(系统权限表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
class TPermissionService(
    private val tRolePermissionService: TRolePermissionService,
    private val cacheService: CacheService
) : ServiceImpl<TPermissionMapper, TPermission>() {

    fun add(permission: TPermission): Boolean {
        return super.save(permission)
    }

    //数据库事务 出现异常回滚
    @Transactional(rollbackFor = [Exception::class])
    fun delete(permissionId: Int): Boolean {
        tRolePermissionService.remove(
            QueryWrapper<TRolePermission>()
                .eq("permission_id", permissionId)
        )
        val b = super.removeById(permissionId)
        if (b) {
            cacheService.clearPermissionCache()
            return true
        }
        return false
    }

    fun change(permission: TPermission): Boolean {
        val b = super.update(permission, QueryWrapper<TPermission>().eq("id", permission.id))
        if (b) {
            cacheService.clearPermissionCache(SecurityUtils.getCurrentUsername() as String)
            return true
        }
        return false
    }

    fun getPageList(name: String, url: String, parentId: Int?, pageNum: Int, pageSize: Int): Page<TPermission> {
        return super.page(
            Page(pageNum.toLong(), pageSize.toLong()),
            QueryWrapper<TPermission>()
                .like(name.isNotEmpty(), "name", name)
                .like(url.isNotEmpty(), "url", url)
                .eq(parentId != null, "parentId", parentId)
        )
    }
}
