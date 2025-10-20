package com.langhuan.controller;

import com.langhuan.common.Result;
import com.langhuan.model.domain.TRole;
import com.langhuan.service.TRoleService;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping(path = "/role")
public class RoleController {
    private final TRoleService TRoleService;

    public RoleController(TRoleService TRoleService) {
        this.TRoleService = TRoleService;
    }

    @PreAuthorize("hasAuthority('/role/add')")
    @PostMapping("/add")
    public Result add(@RequestBody TRole role) {
        return Result.success(TRoleService.add(role));
    }

    @PreAuthorize("hasAuthority('/role/delete')")
    @PostMapping("/delete")
    public Result delete(@RequestParam(name = "id", required = true) Integer id) throws AuthorizationDeniedException {
        if (id == 1) {
            return Result.error("管理员角色不能删除");
        }
        Boolean delete = TRoleService.delete(id);
        if (!delete) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @PreAuthorize("hasAuthority('/role/change')")
    @PostMapping("/change")
    public Result change(@RequestBody TRole role) throws AuthorizationDeniedException {
        return Result.success(TRoleService.change(role));
    }

    // @PreAuthorize("hasAuthority('/role/list')")
    @PostMapping("/getPageList")
    public Result getPageList(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "remark", required = false, defaultValue = "") String remark,
            @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return Result.success(TRoleService.getPageList(name, remark, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('/role/permission/view')")
    @PostMapping("/getRolePermission")
    public Result getRolePermission(@RequestParam(name = "id", required = false) Integer id) {
        return Result.success(TRoleService.getRolePermission(id));
    }

    @PreAuthorize("hasAuthority('/role/permission/edit')")
    @PostMapping("/relevancyRoles")
    public Result relevancyRoles(
            @RequestParam(name = "id", required = true) Integer id,
            @RequestParam(name = "permissionIds", required = true) String permissionIds) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<Integer> permissionList = new ArrayList<>();
        try {
            if (permissionIds.isEmpty()) {
                return Result.error("权限id不能为空");
            } else {
                String[] strings = permissionIds.split(",");
                permissionList = Arrays.stream(strings).map(Integer::parseInt).collect(Collectors.toList());
            }
        } catch (Exception e) {
            return Result.error("权限id格式错误");
        }
        if (id == 1) {
            List<Map<String, Object>> nowRolePermission = TRoleService.getRolePermission(id);
            if (nowRolePermission.size() >= permissionList.size()) {
                return Result.error("管理员角色不能降低权限");
            }
        }
        TRoleService.relevancyPermissions(id, permissionList);
        return Result.success("操作成功");
    }
}
