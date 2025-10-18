package com.langhuan.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.model.domain.TRolePermission
import com.langhuan.model.mapper.TRolePermissionMapper
import org.springframework.stereotype.Service

/**
 * @author lishuangqi
 * @description 针对表【t_role_permission(角色-权限关联表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
class TRolePermissionService : ServiceImpl<TRolePermissionMapper, TRolePermission>()
