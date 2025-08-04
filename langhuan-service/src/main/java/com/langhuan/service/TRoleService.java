package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.dao.TRoleDao;
import com.langhuan.model.domain.TRole;
import com.langhuan.model.domain.TRolePermission;
import com.langhuan.model.domain.TUserRole;
import com.langhuan.model.mapper.TRoleMapper;
import com.langhuan.utils.other.SecurityUtils;

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
public class TRoleService extends ServiceImpl<TRoleMapper, TRole> {
    private final TRolePermissionService TRolePermissionService;
    private final TUserRoleService TUserRoleService;
    private final TRoleDao TRoleDao;
    private final CacheService cacheService;

    public TRoleService(TRolePermissionService TRolePermissionService, TUserRoleService TUserRoleService, TRoleDao TRoleDao, CacheService cacheService) {
        this.TRolePermissionService = TRolePermissionService;
        this.TUserRoleService = TUserRoleService;
        this.TRoleDao = TRoleDao;
        this.cacheService = cacheService;
    }

    public Boolean add(TRole role) {
        return super.save(role);
    }

    //数据库事务 出现异常回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer roleId) {
        TRolePermissionService.remove(new LambdaQueryWrapper<TRolePermission>()
                .eq(TRolePermission::getRoleId, roleId));
        TUserRoleService.remove(new LambdaQueryWrapper<TUserRole>()
                .eq(TUserRole::getRoleId, roleId));
        boolean b = super.removeById(roleId);
        if (b) {
            cacheService.clearPermissionCache();
            return true;
        }
        return false;
    }

    public Boolean change(TRole role) {
        boolean b = super.update(role, new LambdaQueryWrapper<TRole>().eq(TRole::getId, role.getId()));
        if (b) {
            cacheService.clearPermissionCache();
            return true;
        }
        return false;
    }

    public Page<TRole> getPageList(String name, String remark, int pageNum, int pageSize) {
        return super.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TRole>()
                        .like(!name.isEmpty(), TRole::getName, name)
                        .like(!remark.isEmpty(), TRole::getRemark, remark)
        );
    }

    public List<Map<String, Object>> getRolePermission(Integer roleId) {
        return TRoleDao.getRolePermission(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void relevancyPermissions(Integer roleId, List<Integer> permissionIds) {
        TRolePermissionService.remove(new LambdaQueryWrapper<TRolePermission>().eq(TRolePermission::getRoleId, roleId));
        List<TRolePermission> rolePermissions = new ArrayList<>();
        permissionIds.forEach(permissionId -> {
            TRolePermission rolePermission = new TRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissions.add(rolePermission);
        });
        TRolePermissionService.saveBatch(rolePermissions);
        cacheService.clearPermissionCache();
    }
}




