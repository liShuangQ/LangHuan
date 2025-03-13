package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TPermission;
import com.langhuan.model.domain.TRolePermission;
import com.langhuan.model.mapper.TPermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lishuangqi
 * @description 针对表【t_permission(系统权限表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
public class PermissionService extends ServiceImpl<TPermissionMapper, TPermission> {

    private final RolePermissionService rolePermissionService;

    public PermissionService(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    public Boolean add(TPermission Permission) {
        return super.save(Permission);
    }

    //数据库事务 出现异常回滚
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer permissionId) {
        rolePermissionService.remove(new LambdaQueryWrapper<TRolePermission>()
                .eq(TRolePermission::getPermissionId, permissionId)
        );
        return super.removeById(permissionId);
    }

    public Boolean change(TPermission Permission) {
        return super.update(Permission, new LambdaQueryWrapper<TPermission>().eq(TPermission::getId, Permission.getId()));
    }

    public Page<TPermission> getPageList(String name, String url, Integer parentId, int currentPage, int pageSize) {
        return super.page(new Page<>(currentPage, pageSize),
                new LambdaQueryWrapper<TPermission>()
                        .like(!name.isEmpty(), TPermission::getName, name)
                        .like(!url.isEmpty(), TPermission::getUrl, url)
                        .eq(parentId != null, TPermission::getParentId, parentId)
        );
    }
}




