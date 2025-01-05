package com.shuangqi.aiagent7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuangqi.aiagent7.model.domain.TRole;
import com.shuangqi.aiagent7.model.domain.TRolePermission;
import com.shuangqi.aiagent7.model.domain.TUserRole;
import com.shuangqi.aiagent7.model.mapper.TRoleMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lishuangqi
 * @description 针对表【t_role(系统角色表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
public class RoleService extends ServiceImpl<TRoleMapper, TRole> {
    private final RolePermissionService rolePermissionService;
    private final UserRoleService userRoleService;
    private final JdbcTemplate dao;

    public RoleService(RolePermissionService rolePermissionService, UserRoleService userRoleService, JdbcTemplate dao) {
        this.rolePermissionService = rolePermissionService;
        this.userRoleService = userRoleService;
        this.dao = dao;
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

    public List<Map<String, Object>> getRolePermission(Integer roleId) {
        StringBuilder sql = new StringBuilder();
        if (roleId != null) {
            sql.append("""
                    select
                        p.id as permission_id,
                        p.name as permission_name
                    from t_role_permission rp
                             left join t_role r on rp.role_id = r.id
                             left join t_permission p on rp.permission_id = p.id
                    where 1 = 1
                    and r.id = ?
                    """);
            return dao.queryForList(sql.toString(), List.of(roleId).toArray());
        } else {
            sql.append("""
                        select  p.id as permission_id,
                                p.name as permission_name
                        from t_permission p
                    """);
            return dao.queryForList(sql.toString());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void relevancyPermissions(Integer roleId, List<Integer> permissionIds) {
        rolePermissionService.remove(new LambdaQueryWrapper<TRolePermission>().eq(TRolePermission::getRoleId, roleId));
        List<TRolePermission> rolePermissions = new ArrayList<>();
        permissionIds.forEach(permissionId -> {
            TRolePermission rolePermission = new TRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissions.add(rolePermission);
        });
        rolePermissionService.saveBatch(rolePermissions);
    }
}




