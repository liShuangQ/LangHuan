package com.langhuan.controller;

import com.langhuan.common.Result;
import com.langhuan.model.domain.TRole;
import com.langhuan.service.TRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/role")
public class RoleController {
    private final TRoleService TRoleService;

    public RoleController(TRoleService TRoleService) {
        this.TRoleService = TRoleService;
    }


    @PostMapping("/add")
    public Result add(@RequestBody TRole role) {
        return Result.success(TRoleService.add(role));
    }

    @PreAuthorize("hasRole('/user/manager')")
    @PostMapping("/delete")
    public Result delete(@RequestParam(name = "id", required = true) Integer id) throws AuthorizationDeniedException {
        Boolean delete = TRoleService.delete(id);
        if (!delete) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @PreAuthorize("hasRole('/user/manager')")
    @PostMapping("/change")
    public Result change(@RequestBody TRole role) throws AuthorizationDeniedException {
        return Result.success(TRoleService.change(role));
    }

    @PostMapping("/getPageList")
    public Result getPageList(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "remark", required = false, defaultValue = "") String remark,
            @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        return Result.success(TRoleService.getPageList(name, remark, currentPage, pageSize));
    }

    @PostMapping("/getRolePermission")
    public Result getRolePermission(@RequestParam(name = "id", required = false) Integer id) {
        return Result.success(TRoleService.getRolePermission(id));
    }

    @PostMapping("/relevancyRoles")
    public Result relevancyRoles(
            @RequestParam(name = "id", required = true) Integer id,
            @RequestParam(name = "permissionIds", required = true) String permissionIds
    ) {
        try {
            if (permissionIds.isEmpty()) {
                TRoleService.relevancyPermissions(id, new ArrayList<>());
            } else {
                String[] strings = permissionIds.split(",");
                TRoleService.relevancyPermissions(id, Arrays.stream(strings).map(Integer::parseInt).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            return Result.error("权限id格式错误");
        }
        return Result.success("操作成功");
    }
}
