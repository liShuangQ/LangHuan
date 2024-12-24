package com.shuangqi.aiagent7.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuangqi.aiagent7.model.domain.TPermission;
import com.shuangqi.aiagent7.model.domain.TRolePermission;
import com.shuangqi.aiagent7.model.domain.TUser;
import com.shuangqi.aiagent7.model.domain.TUserRole;
import com.shuangqi.aiagent7.model.mapper.TUserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lishuangqi
 * @description 针对表【t_user(用户表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
public class UserService extends ServiceImpl<TUserMapper, TUser> {
    private final TUserMapper mapper;
    private final UserRoleService userRoleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;

    public UserService(TUserMapper mapper, UserRoleService userRoleService, RolePermissionService rolePermissionService, PermissionService permissionService) {
        this.mapper = mapper;
        this.userRoleService = userRoleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
    }

    public List<TPermission> getPermissionByUsername(String username) {
        TUser user = super.getOne(Wrappers.<TUser>lambdaQuery().eq(TUser::getUsername, username), true);
        return this.getPermissionByUser(user);
    }

    public List<TPermission> getPermissionByUserId(Integer userId) {
        TUser user = super.getById(userId);
        return this.getPermissionByUser(user);
    }

    public List<TPermission> getPermissionByUser(TUser user) {
        List<TPermission> permissions = new ArrayList<>();
        if (null != user) {
            List<TUserRole> userRoles = userRoleService.list(Wrappers.<TUserRole>lambdaQuery().eq(TUserRole::getUserId, user.getId()));
            if (CollectionUtils.isNotEmpty(userRoles)) {
                List<Integer> roleIds = new ArrayList<>();
                userRoles.stream().forEach(userRole -> {
                    roleIds.add(userRole.getRoleId());
                });
                List<TRolePermission> rolePermissions = rolePermissionService.list(Wrappers.<TRolePermission>lambdaQuery().in(TRolePermission::getRoleId, roleIds));
                if (CollectionUtils.isNotEmpty(rolePermissions)) {
                    List<Integer> permissionIds = new ArrayList<>();
                    rolePermissions.stream().forEach(rolePermission -> {
                        permissionIds.add(rolePermission.getPermissionId());
                    });
                    permissions = permissionService.list(Wrappers.<TPermission>lambdaQuery().in(TPermission::getId, permissionIds));
                }
            }
        }
        return permissions;
    }
}




