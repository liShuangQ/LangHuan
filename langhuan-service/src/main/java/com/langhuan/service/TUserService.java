package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.model.domain.TPermission;
import com.langhuan.model.domain.TRolePermission;
import com.langhuan.model.domain.TUser;
import com.langhuan.model.domain.TUserRole;
import com.langhuan.model.dto.UserLoginDTO;
import com.langhuan.model.mapper.TUserMapper;
import com.langhuan.utils.other.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class TUserService extends ServiceImpl<TUserMapper, TUser> {
    // 用户角色服务，用于处理用户和角色之间的关系
    private final TUserRoleService TUserRoleService;
    // 角色权限服务，用于处理角色和权限之间的关系
    private final TRolePermissionService TRolePermissionService;
    // 权限服务，用于执行权限相关的数据库操作
    private final TPermissionService TPermissionService;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate dao;
    private final CacheService cacheService;

    // 构造方法，注入必要的服务和映射器
    public TUserService(TUserRoleService TUserRoleService, TRolePermissionService TRolePermissionService,
            TPermissionService TPermissionService, JwtUtil jwtUtil, JdbcTemplate dao, CacheService cacheService) {
        this.TUserRoleService = TUserRoleService;
        this.TRolePermissionService = TRolePermissionService;
        this.TPermissionService = TPermissionService;
        this.jwtUtil = jwtUtil;
        this.dao = dao;
        this.cacheService = cacheService;
    }

    /**
     * 根据用户名获取权限列表
     * 首先通过用户名查询用户信息，然后调用getPermissionByUser方法获取权限列表
     */
    @Cacheable(value = "permission")
    // 这里注意缓存配置，在操作用户后要清空缓存
    public List<TPermission> getPermissionByUsername(String username) {
        log.info("setPermissionCache: {}", username);
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
            List<TUserRole> userRoles = TUserRoleService
                    .list(new LambdaQueryWrapper<TUserRole>().eq(TUserRole::getUserId, user.getId()));
            if (CollectionUtils.isNotEmpty(userRoles)) {
                // 提取角色ID列表
                List<Integer> roleIds = new ArrayList<>();
                userRoles.stream().forEach(userRole -> {
                    roleIds.add(userRole.getRoleId());
                });
                // 获取角色对应的权限列表
                List<TRolePermission> rolePermissions = TRolePermissionService
                        .list(new LambdaQueryWrapper<TRolePermission>().in(TRolePermission::getRoleId, roleIds));
                if (CollectionUtils.isNotEmpty(rolePermissions)) {
                    // 提取权限ID列表
                    List<Integer> permissionIds = new ArrayList<>();
                    rolePermissions.stream().forEach(rolePermission -> {
                        permissionIds.add(rolePermission.getPermissionId());
                    });
                    // 根据权限ID列表查询权限信息
                    permissions = TPermissionService
                            .list(new LambdaQueryWrapper<TPermission>().in(TPermission::getId, permissionIds));
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
        cacheService.clearPermissionCache();
        return user;
    }

    public Map<String, String> login(UserLoginDTO userLoginDTO, HttpServletResponse response) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        TUser user = super.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        // HACK: 针对admin单独处理
        if (user.getUsername().equals("admin")) {
            if (!password.equals(Constant.ADMIN_PASSWORD)) {
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
        super.update(new LambdaUpdateWrapper<TUser>().set(TUser::getLastLoginTime, new Date()).eq(TUser::getUsername,
                username));
        return map;
    }

    public Map<String, Object> getUserInfoByToken() {
        Map<String, Object> map = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TUser user = super.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, auth.getName()));
        user.setPassword(null);
        map.put("user", user);
        map.put("permission", auth.getAuthorities());
        map.put("role", dao.queryForList("""
                select r.* from t_user u
                left join t_user_role ur on u.id = ur.user_id
                left join t_role r on ur.role_id = r.id
                where u.id = ?
                """, List.of(user.getId()).toArray()));
        return map;
    }

    // 数据库事务 出现异常回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer userId) {
        TUser user = super.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getId().equals(1)) {
            throw new BusinessException("超级管理员不能删除");
        }
        TUserRoleService.remove(new LambdaQueryWrapper<TUserRole>().eq(TUserRole::getUserId, userId));
        cacheService.clearPermissionCache();
        return super.removeById(userId);
    }

    public Page<TUser> getUserPageList(String name, String username, Integer gender, Integer enabled, int pageNum,
            int pageSize) {
        Page<TUser> userPage = super.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TUser>()
                        .like(!name.isEmpty(), TUser::getName, name)
                        .like(!username.isEmpty(), TUser::getUsername, username)
                        .eq(gender != null, TUser::getGender, gender)
                        .eq(enabled != null, TUser::getEnabled, enabled));
        for (TUser record : userPage.getRecords()) {
            record.setPassword(null);
        }
        return userPage;
    }

    public List<Map<String, Object>> getUserRoles(Integer userId) {
        StringBuilder sql = new StringBuilder();
        if (userId != null) {
            sql.append("""
                    select
                        r.id as role_id,
                        r.name as role_name
                    from t_user_role ur
                             left join t_user u on ur.user_id = u.id
                             left join t_role r on ur.role_id = r.id
                    where 1 = 1
                    and u.id = ?
                    """);
            return dao.queryForList(sql.toString(), List.of(userId).toArray());
        } else {
            sql.append("""
                        select r.id as role_id, r.name as role_name
                        from t_role r;
                    """);
            return dao.queryForList(sql.toString());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void relevancyRoles(Integer userId, List<Integer> roleIds) {
        if (userId.equals(1) && !roleIds.contains(1)) {
            throw new BusinessException("超级管理员不能修改");
        }
        TUserRoleService.remove(new LambdaQueryWrapper<TUserRole>().eq(TUserRole::getUserId, userId));
        List<TUserRole> userRoles = new ArrayList<>();
        roleIds.forEach(roleId -> {
            TUserRole userRole = new TUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        });
        TUserRoleService.saveBatch(userRoles);
        cacheService.clearPermissionCache();
    }
}
