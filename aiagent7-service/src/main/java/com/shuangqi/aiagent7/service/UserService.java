package com.shuangqi.aiagent7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuangqi.aiagent7.common.BusinessException;
import com.shuangqi.aiagent7.model.domain.TPermission;
import com.shuangqi.aiagent7.model.domain.TRolePermission;
import com.shuangqi.aiagent7.model.domain.TUser;
import com.shuangqi.aiagent7.model.domain.TUserRole;
import com.shuangqi.aiagent7.model.dto.UserLoginDTO;
import com.shuangqi.aiagent7.model.mapper.TUserMapper;
import com.shuangqi.aiagent7.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author lishuangqi
 * @description 针对表【t_user(用户表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Slf4j
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
    private final JwtUtil jwtUtil;

    // 构造方法，注入必要的服务和映射器
    public UserService(TUserMapper mapper, UserRoleService userRoleService, RolePermissionService rolePermissionService, PermissionService permissionService, JwtUtil jwtUtil) {
        this.mapper = mapper;
        this.userRoleService = userRoleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 根据用户名获取权限列表
     * 首先通过用户名查询用户信息，然后调用getPermissionByUser方法获取权限列表
     */
    public List<TPermission> getPermissionByUsername(String username) {
        TUser user = super.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, username), true);
        return this.getPermissionByUser(user);
    }

    /**
     * 根据用户ID获取权限列表
     * 首先通过用户ID查询用户信息，然后调用getPermissionByUser方法获取权限列表
     */
    public List<TPermission> getPermissionByUserId(Integer userId) {
        TUser user = super.getById(userId);
        return this.getPermissionByUser(user);
    }

    /**
     * 根据用户对象获取权限列表
     * 此方法首先检查用户是否不为空，然后通过用户角色关系获取角色ID列表，
     * 再通过角色权限关系获取权限ID列表，最后根据权限ID列表查询权限信息
     */
    public List<TPermission> getPermissionByUser(TUser user) {
        List<TPermission> permissions = new ArrayList<>();
        if (null != user) {
            // 获取用户的角色列表
            List<TUserRole> userRoles = userRoleService.list(new LambdaQueryWrapper<TUserRole>().eq(TUserRole::getUserId, user.getId()));
            if (CollectionUtils.isNotEmpty(userRoles)) {
                // 提取角色ID列表
                List<Integer> roleIds = new ArrayList<>();
                userRoles.stream().forEach(userRole -> {
                    roleIds.add(userRole.getRoleId());
                });
                // 获取角色对应的权限列表
                List<TRolePermission> rolePermissions = rolePermissionService.list(new LambdaQueryWrapper<TRolePermission>().in(TRolePermission::getRoleId, roleIds));
                if (CollectionUtils.isNotEmpty(rolePermissions)) {
                    // 提取权限ID列表
                    List<Integer> permissionIds = new ArrayList<>();
                    rolePermissions.stream().forEach(rolePermission -> {
                        permissionIds.add(rolePermission.getPermissionId());
                    });
                    // 根据权限ID列表查询权限信息
                    permissions = permissionService.list(new LambdaQueryWrapper<TPermission>().in(TPermission::getId, permissionIds));
                }
            }
        }
        return permissions;
    }

    public TUser register(TUser user) {
        TUser user1 = super.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, user.getUsername()));
        if (user1 != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword())); // 密码加密
        user.setCreationTime(new Date());
        super.save(user);
        user.setPassword(null);
        return user;
    }

    public TUser change(TUser user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        super.update(user, new LambdaUpdateWrapper<TUser>().eq(TUser::getUsername, user.getUsername()));
        return user;
    }

    public Map<String, String> login(UserLoginDTO userLoginDTO, HttpServletResponse response) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        TUser user = super.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        // HACK: 兼容初始创建的两个用户, 极大隐患
        if (user.getId().equals(1) || user.getId().equals(2)) {
            if (!user.getPassword().equals(password)) {
                throw new BusinessException("用户名或密码错误");
            }
        } else {
            if (!new BCryptPasswordEncoder().matches(password, user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }
        }

        String token = jwtUtil.generateToken(username);
        response.setHeader(JwtUtil.HEADER, token);
        response.setHeader("Access-control-Expost-Headers", JwtUtil.HEADER);
        Map<String, String> map = new HashMap<>();
        map.put("token", token);

        log.info("用户 {} 登录成功,更新时间", username);
        super.update(new LambdaUpdateWrapper<TUser>().set(TUser::getLastLoginTime, new Date()).eq(TUser::getUsername, username));
        return map;
    }

    public TUser getUserInfoByToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TUser user = super.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, auth.getName()));
        user.setPassword(null);
        return user;
    }

    //数据库事务 出现异常回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer userId) {
        TUser user = super.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getId().equals(1)) {
            throw new BusinessException("超级管理员不能删除");
        }
        userRoleService.remove(new LambdaQueryWrapper<TUserRole>().eq(TUserRole::getUserId, userId));
        return super.removeById(userId);
    }

    public Page<TUser> getUserPageList(String name, String username, Integer gender, Integer enabled, int currentPage, int pageSize) {
        Page<TUser> userPage = super.page(new Page<>(currentPage, pageSize),
                new LambdaQueryWrapper<TUser>()
                        .like(!name.isEmpty(), TUser::getName, name)
                        .like(!username.isEmpty(), TUser::getUsername, username)
                        .eq(gender != null, TUser::getGender, gender)
                        .eq(enabled != null, TUser::getEnabled, enabled)
        );
        for (TUser record : userPage.getRecords()) {
            record.setPassword(null);
        }
        return userPage;
    }
}
