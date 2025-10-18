package com.langhuan.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.dao.TRoleDao
import com.langhuan.model.domain.TRole
import com.langhuan.model.domain.TRolePermission
import com.langhuan.model.domain.TUserRole
import com.langhuan.model.mapper.TRoleMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author lishuangqi
 * @description 针对表【t_role(系统角色表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
class TRoleService(
    private val tRolePermissionService: TRolePermissionService,
    private val tUserRoleService: TUserRoleService,
    private val tRoleDao: TRoleDao,
    private val cacheService: CacheService
) : ServiceImpl<TRoleMapper, TRole>() {

    fun add(role: TRole): Boolean {
        return super.save(role)
    }

    //数据库事务 出现异常回滚
    @Transactional(rollbackFor = [Exception::class])
    fun delete(roleId: Int): Boolean {
        tRolePermissionService.remove(
            QueryWrapper<TRolePermission>()
                .eq("role_id", roleId)
        )
        tUserRoleService.remove(
            QueryWrapper<TUserRole>()
                .eq("role_id", roleId)
        )
        val b = super.removeById(roleId)
        if (b) {
            cacheService.clearPermissionCache()
            return true
        }
        return false
    }

    fun change(role: TRole): Boolean {
        val b = super.update(
            role,
            QueryWrapper<TRole>().eq("id", role.id)
        )
        if (b) {
            cacheService.clearPermissionCache()
            return true
        }
        return false
    }

    fun getPageList(name: String, remark: String, pageNum: Int, pageSize: Int): Page<TRole> {
        return super.page(
            Page(pageNum.toLong(), pageSize.toLong()),
            QueryWrapper<TRole>()
                .like(name.isNotEmpty(), "name", name)
                .like(remark.isNotEmpty(), "remark", remark)
        )
    }

    fun getRolePermission(roleId: Int): List<Map<String, Any>> {
        return tRoleDao.getRolePermission(roleId)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun relevancyPermissions(roleId: Int, permissionIds: List<Int>) {
        tRolePermissionService.remove(
            QueryWrapper<TRolePermission>().eq("role_id", roleId)
        )
        val rolePermissions = mutableListOf<TRolePermission>()
        permissionIds.forEach { permissionId ->
            val rolePermission = TRolePermission()
            rolePermission.roleId = roleId
            rolePermission.permissionId = permissionId
            rolePermissions.add(rolePermission)
        }
        tRolePermissionService.saveBatch(rolePermissions)
        cacheService.clearPermissionCache()
    }
}
