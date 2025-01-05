package com.shuangqi.aiagent7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuangqi.aiagent7.model.domain.TRole;
import com.shuangqi.aiagent7.model.domain.TRolePermission;
import com.shuangqi.aiagent7.model.domain.TUserRole;
import com.shuangqi.aiagent7.model.mapper.TRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lishuangqi
 * @description 针对表【t_role(系统角色表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
public class RoleService extends ServiceImpl<TRoleMapper, TRole> {
    private final RolePermissionService rolePermissionService;
    private final UserRoleService userRoleService;

    public RoleService(RolePermissionService rolePermissionService, UserRoleService userRoleService) {
        this.rolePermissionService = rolePermissionService;
        this.userRoleService = userRoleService;
    }

    public Boolean add(TRole role) {
        return super.save(role);
    }

    //数据库事务 出现异常回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer roleId) {
        rolePermissionService.remove(new LambdaQueryWrapper<TRolePermission>()
                .eq(TRolePermission::getRoleId, roleId));
        userRoleService.remove(new LambdaQueryWrapper<TUserRole>()
                .eq(TUserRole::getRoleId, roleId));
        return super.removeById(roleId);
    }

    public Boolean change(TRole role) {
        return super.update(role, new LambdaQueryWrapper<TRole>().eq(TRole::getId, role.getId()));
    }

    public Page<TRole> getPageList(String name, String remark, int currentPage, int pageSize) {
        return super.page(new Page<>(currentPage, pageSize),
                new LambdaQueryWrapper<TRole>()
                        .like(!name.isEmpty(), TRole::getName, name)
                        .like(!remark.isEmpty(), TRole::getRemark, remark)
        );
    }
}




