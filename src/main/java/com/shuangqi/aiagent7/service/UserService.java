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
    // 用户映射器，用于执行用户相关的数据库操作
    private final TUserMapper mapper;
    // 用户角色服务，用于处理用户和角色之间的关系
    private final UserRoleService userRoleService;
    // 角色权限服务，用于处理角色和权限之间的关系
    private final RolePermissionService rolePermissionService;
    // 权限服务，用于执行权限相关的数据库操作
    private final PermissionService permissionService;

    // 构造方法，注入必要的服务和映射器
    public UserService(TUserMapper mapper, UserRoleService userRoleService, RolePermissionService rolePermissionService, PermissionService permissionService) {
        this.mapper = mapper;
        this.userRoleService = userRoleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
    }

    /**
     * 根据用户名获取权限列表
     * 首先通过用户名查询用户信息，然后调用getPermissionByUser方法获取权限列表
     *
     * @param username 用户名
     * @return 权限列表
     */
    public List<TPermission> getPermissionByUsername(String username) {
        TUser user = super.getOne(Wrappers.<TUser>lambdaQuery().eq(TUser::getUsername, username), true);
        return this.getPermissionByUser(user);
    }

    /**
     * 根据用户ID获取权限列表
     * 首先通过用户ID查询用户信息，然后调用getPermissionByUser方法获取权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<TPermission> getPermissionByUserId(Integer userId) {
        TUser user = super.getById(userId);
        return this.getPermissionByUser(user);
    }

    /**
     * 根据用户对象获取权限列表
     * 此方法首先检查用户是否不为空，然后通过用户角色关系获取角色ID列表，
     * 再通过角色权限关系获取权限ID列表，最后根据权限ID列表查询权限信息
     *
     * @param user 用户对象
     * @return 权限列表
     */
    public List<TPermission> getPermissionByUser(TUser user) {
        List<TPermission> permissions = new ArrayList<>();
        if (null != user) {
            // 获取用户的角色列表
            List<TUserRole> userRoles = userRoleService.list(Wrappers.<TUserRole>lambdaQuery().eq(TUserRole::getUserId, user.getId()));
            if (CollectionUtils.isNotEmpty(userRoles)) {
                // 提取角色ID列表
                List<Integer> roleIds = new ArrayList<>();
                userRoles.stream().forEach(userRole -> {
                    roleIds.add(userRole.getRoleId());
                });
                // 获取角色对应的权限列表
                List<TRolePermission> rolePermissions = rolePermissionService.list(Wrappers.<TRolePermission>lambdaQuery().in(TRolePermission::getRoleId, roleIds));
                if (CollectionUtils.isNotEmpty(rolePermissions)) {
                    // 提取权限ID列表
                    List<Integer> permissionIds = new ArrayList<>();
                    rolePermissions.stream().forEach(rolePermission -> {
                        permissionIds.add(rolePermission.getPermissionId());
                    });
                    // 根据权限ID列表查询权限信息
                    permissions = permissionService.list(Wrappers.<TPermission>lambdaQuery().in(TPermission::getId, permissionIds));
                }
            }
        }
        return permissions;
    }
}
